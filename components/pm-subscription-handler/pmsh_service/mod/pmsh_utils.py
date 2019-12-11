# ============LICENSE_START===================================================
#  Copyright (C) 2019 Nordix Foundation.
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
import uuid

import requests
from requests.auth import HTTPBasicAuth

import mod.pmsh_logging as logger


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
            MrSub: the `MrSub` <MrSub> Object requested.

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
            MrPub: the `MrPub` <MrPub> Object requested.

        Raises:
            KeyError: if the sub_name is not found.
        """
        try:
            return _MrPub(pub_name, self.aaf_creds, **self.streams_publishes[pub_name])
        except KeyError as e:
            logger.debug(e)
            raise

    @property
    def get_cert_params(self):
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
            session.verify = False
            headers = {'content-type': 'application/json', 'x-transactionId': str(uuid.uuid1())}
            response = session.post(self.topic_url, headers=headers,
                                    auth=HTTPBasicAuth(self.aaf_id, self.aaf_pass), json=event_json)
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
        new_sub = {k: v for k, v in subscription.__dict__.items() if k != 'nfFilter'}
        new_sub.update({'nfName': xnf_name, 'policyName': f'CP-{subscription.subscriptionName}'})
        self.publish_to_topic(new_sub)
        new_sub.pop('nfName')


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
            json: the json response from DMaaP Message Router topic, else None.
        """
        topic_data = None
        try:
            session = requests.Session()
            session.verify = False
            headers = {'accept': 'application/json', 'content-type': 'application/json'}
            response = session.get(f'{self.topic_url}/{consumer_group}/{consumer_id}'
                                   f'?timeout={timeout}',
                                   auth=HTTPBasicAuth(self.aaf_id, self.aaf_pass), headers=headers)
            response.raise_for_status()
            if response.ok:
                topic_data = json.loads(response.text)
        except Exception as e:
            logger.debug(e)
        return topic_data
