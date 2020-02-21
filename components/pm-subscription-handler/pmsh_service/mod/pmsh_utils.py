# ============LICENSE_START===================================================
#  Copyright (C) 2019-2020 Nordix Foundation.
# ============================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=====================================================
import json
import threading
import uuid

import requests
from requests.auth import HTTPBasicAuth
from tenacity import retry, wait_fixed, retry_if_exception_type

import mod.pmsh_logging as logger
from mod.subscription import Subscription, SubNfState, AdministrativeState
from mod.network_function import NetworkFunction


class AppConfig:
    def __init__(self, **kwargs):
        self.aaf_creds = {'aaf_id': kwargs.get('aaf_identity'),
                          'aaf_pass': kwargs.get('aaf_password')}
        self.cert_path = kwargs.get('cert_path')
        self.key_path = kwargs.get('key_path')
        self.streams_subscribes = kwargs.get('streams_subscribes')
        self.streams_publishes = kwargs.get('streams_publishes')

    def get_mr_sub(self, sub_name):
        """
        Returns the MrSub object requested.

        Args:
            sub_name: the key of the subscriber object.

        Returns:
            MrSub: an Instance of an `MrSub` <MrSub> Object.

        Raises:
            KeyError: if the sub_name is not found.
        """
        try:
            return _MrSub(sub_name, self.aaf_creds, **self.streams_subscribes[sub_name])
        except KeyError as e:
            logger.debug(e)
            raise

    def get_mr_pub(self, pub_name):
        """
        Returns the MrPub object requested.

        Args:
            pub_name: the key of the publisher object.

        Returns:
            MrPub: an Instance of an `MrPub` <MrPub> Object.

        Raises:
            KeyError: if the sub_name is not found.
        """
        try:
            return _MrPub(pub_name, self.aaf_creds, **self.streams_publishes[pub_name])
        except KeyError as e:
            logger.debug(e)
            raise

    @property
    def cert_params(self):
        """
        Returns the tls artifact paths.

        Returns:
            cert_path, key_path: the path to tls cert and key.
        """
        return self.cert_path, self.key_path


class _DmaapMrClient:
    def __init__(self, aaf_creds, **kwargs):
        """
        A DMaaP Message Router utility class.
        Sub classes should be invoked via the AppConfig.get_mr_{pub|sub} only.
        Args:
            aaf_creds: a dict of aaf secure credentials.
            **kwargs: a dict of streams_{subscribes|publishes} data.
        """
        self.topic_url = kwargs.get('dmaap_info').get('topic_url')
        self.aaf_id = aaf_creds.get('aaf_id')
        self.aaf_pass = aaf_creds.get('aaf_pass')


class _MrPub(_DmaapMrClient):
    def __init__(self, pub_name, aaf_creds, **kwargs):
        self.pub_name = pub_name
        super().__init__(aaf_creds, **kwargs)

    def publish_to_topic(self, event_json):
        """
        Publish the event to the DMaaP Message Router topic.

        Args:
            event_json: the json data to be published.

        Raises:
            Exception: if post request fails.
        """
        try:
            session = requests.Session()
            headers = {'content-type': 'application/json', 'x-transactionId': str(uuid.uuid1())}
            response = session.post(self.topic_url, headers=headers,
                                    auth=HTTPBasicAuth(self.aaf_id, self.aaf_pass), json=event_json,
                                    verify=False)
            response.raise_for_status()
        except Exception as e:
            logger.debug(e)
            raise

    def publish_subscription_event_data(self, subscription, xnf_name):
        """
        Update the Subscription dict with xnf and policy name then publish to DMaaP MR topic.

        Args:
            subscription: the `Subscription` <Subscription> object.
            xnf_name: the xnf to include in the event.
        """
        try:
            subscription_event = subscription.prepare_subscription_event(xnf_name)
            self.publish_to_topic(subscription_event)
        except Exception as e:
            logger.debug(f'pmsh_utils.publish_subscription_event_data : {e}')


class _MrSub(_DmaapMrClient):
    def __init__(self, sub_name, aaf_creds, **kwargs):
        self.sub_name = sub_name
        super().__init__(aaf_creds, **kwargs)

    def get_from_topic(self, consumer_id, consumer_group='dcae_pmsh_cg', timeout=1000):
        """
        Returns the json data from the MrTopic.

        Args:
            consumer_id: Within your subscribers group, a name that uniquely
            identifies your subscribers process.
            consumer_group: A name that uniquely identifies your subscribers.
            timeout: The request timeout value in mSec.

        Returns:
            list[str]: the json response from DMaaP Message Router topic, else None.
        """
        topic_data = None
        try:
            session = requests.Session()
            headers = {'accept': 'application/json', 'content-type': 'application/json'}
            response = session.get(f'{self.topic_url}/{consumer_group}/{consumer_id}'
                                   f'?timeout={timeout}',
                                   auth=HTTPBasicAuth(self.aaf_id, self.aaf_pass), headers=headers,
                                   verify=False)
            response.raise_for_status()
            if response.ok:
                topic_data = response.json()
        except Exception as e:
            logger.debug(e)
        return topic_data

    @staticmethod
    def _handle_response(subscription_name, administrative_state, nf_name, response_message):
        """
        Handles the response from Policy, updating the DB

        Args:
            subscription_name (str): The subscription name
            administrative_state (str): The administrative state of the subscription
            nf_name (str): The network function name
            response_message (str): The message in the response regarding the state (success|failed)
        """
        logger.debug(f'Response from MR: Sub: {subscription_name} for '
                     f'NF: {nf_name} received, updating the DB')
        try:
            sub_nf_status = subscription_nf_states[administrative_state][response_message].value
            policy_response_handle_functions[administrative_state][response_message](
                subscription_name=subscription_name, status=sub_nf_status, nf_name=nf_name)
        except Exception as err:
            raise Exception(f'Error changing nf_sub status in the DB: {err}')

    @retry(wait=wait_fixed(5), retry=retry_if_exception_type(Exception))
    def poll_policy_topic(self, subscription_name, app):
        """
        This method polls MR for response from policy. It checks whether the message is for the
        relevant subscription and then handles the response

        Args:
            subscription_name (str): The subscription name
            app (app): Needed to push context for the db
        """
        app.app_context().push()
        administrative_state = Subscription.get(subscription_name).status
        try:
            response_data = self.get_from_topic('policy_response_consumer')
            for data in response_data:
                data = json.loads(data)
                if data['status']['subscriptionName'] == subscription_name:
                    nf_name = data['status']['nfName']
                    response_message = data['status']['message']
                    self._handle_response(subscription_name, administrative_state,
                                          nf_name, response_message)
            threading.Timer(5, self.poll_policy_topic, [subscription_name, app]).start()
        except Exception as err:
            raise Exception(f'Error trying to poll MR: {err}')


subscription_nf_states = {
    AdministrativeState.LOCKED.value: {
        'success': SubNfState.CREATED,
        'failed': SubNfState.DELETE_FAILED
    },
    AdministrativeState.UNLOCKED.value: {
        'success': SubNfState.CREATED,
        'failed': SubNfState.CREATE_FAILED
    }
}

policy_response_handle_functions = {
    AdministrativeState.LOCKED.value: {
        'success': NetworkFunction.delete,
        'failed': Subscription.update_sub_nf_status
    },
    AdministrativeState.UNLOCKED.value: {
        'success': Subscription.update_sub_nf_status,
        'failed': Subscription.update_sub_nf_status
    }
}
