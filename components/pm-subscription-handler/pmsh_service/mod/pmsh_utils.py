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

import uuid
from threading import Timer

import requests
from onap_dcae_cbs_docker_client.client import get_all
from requests.auth import HTTPBasicAuth
from tenacity import wait_fixed, stop_after_attempt, retry, retry_if_exception_type

import mod.pmsh_logging as logger


class ConfigHandler:
    """ Handles retrieval of PMSH's configuration from Configbinding service."""
    @staticmethod
    @retry(wait=wait_fixed(2), stop=stop_after_attempt(5), retry=retry_if_exception_type(Exception))
    def get_pmsh_config():
        """ Retrieves PMSH's configuration from Config binding service. If a non-2xx response
        is received, it retries after 2 seconds for 5 times before raising an exception.

        Returns:
            dict: Dictionary representation of the the service configuration

        Raises:
            Exception: If any error occurred pulling configuration from Config binding service.
        """
        try:
            config = get_all()
            logger.debug(f'PMSH config from CBS: {config}')
            return config
        except Exception as err:
            logger.debug(f'Failed to get config from CBS: {err}')
            raise Exception


class AppConfig:
    def __init__(self, **kwargs):
        self.aaf_creds = {'aaf_id': kwargs.get('aaf_identity'),
                          'aaf_pass': kwargs.get('aaf_password')}
        self.cert_path = kwargs.get('cert_path')
        self.key_path = kwargs.get('key_path')
        self.streams_subscribes = kwargs.get('streams_subscribes')
        self.streams_publishes = kwargs.get('streams_publishes')
        self.operational_policy_name = kwargs.get('operational_policy_name')
        self.control_loop_name = kwargs.get('control_loop_name')

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

    def publish_subscription_event_data(self, subscription, xnf_name, app_conf):
        """
        Update the Subscription dict with xnf and policy name then publish to DMaaP MR topic.

        Args:
            subscription: the `Subscription` <Subscription> object.
            xnf_name: the xnf to include in the event.
            app_conf (AppConfig): the application configuration.
        """
        try:
            subscription_event = subscription.prepare_subscription_event(xnf_name, app_conf)
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
            logger.debug(f'Request sent to MR topic: {self.topic_url}')
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


class PeriodicTask(Timer):
    """
    See :class:`Timer`.
    """

    def run(self):
        self.function(*self.args, **self.kwargs)
        while not self.finished.wait(self.interval):
            self.function(*self.args, **self.kwargs)
