# ============LICENSE_START===================================================
#  Copyright (C) 2021-2022 Nordix Foundation.
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

"""This module represents PMSH application configuration
   Singleton instance of configuration is created and stored,
   Enum representation is used for Message Router topics.
"""

from enum import Enum, unique

import requests
from onap_dcae_cbs_docker_client.client import get_all
from requests.auth import HTTPBasicAuth
from tenacity import wait_fixed, stop_after_attempt, retry, retry_if_exception_type

from mod import logger
from mod.pmsh_utils import mdc_handler


@unique
class MRTopic(Enum):
    """ Enum used to represent Message Router Topic"""
    AAI_SUBSCRIBER = 'aai_subscriber'
    POLICY_PM_PUBLISHER = 'policy_pm_publisher'
    POLICY_PM_SUBSCRIBER = 'policy_pm_subscriber'


class MetaSingleton(type):
    """ Metaclass used to create singleton object by overriding __call__() method """
    _instances = {}

    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super().__call__(*args, **kwargs)
        return cls._instances[cls]

    @classmethod
    def get_cls_instance(mcs, cls_name):
        return mcs._instances[cls_name]


class AppConfig(metaclass=MetaSingleton):
    """ Object representation of the PMSH Application config. """

    def __init__(self):
        app_config = self._get_config()
        self.key_path = app_config['config'].get('key_path')
        self.cert_path = app_config['config'].get('cert_path')
        self.ca_cert_path = app_config['config'].get('ca_cert_path')
        self.enable_tls = app_config['config'].get('enable_tls')
        self.aaf_id = app_config['config'].get('aaf_identity')
        self.aaf_pass = app_config['config'].get('aaf_password')
        self.streams_publishes = app_config['config'].get('streams_publishes')
        self.streams_subscribes = app_config['config'].get('streams_subscribes')

    @staticmethod
    def get_instance():
        return AppConfig.get_cls_instance(AppConfig)

    @retry(wait=wait_fixed(5), stop=stop_after_attempt(5),
           retry=retry_if_exception_type(ValueError))
    def _get_config(self):

        """ Retrieves PMSH's configuration from Config binding service. If a non-2xx response
        is received, it retries after 2 seconds for 5 times before raising an exception.

        Returns:
            dict: Dictionary representation of the the service configuration

        Raises:
            Exception: If any error occurred pulling configuration from Config binding service.
        """
        try:
            logger.info('Attempting to fetch PMSH Configuration from CBS.')
            config = get_all()
            logger.info(f'Successfully fetched PMSH config from CBS: {config}')
            return config
        except Exception as e:
            logger.error(f'Failed to get config from CBS: {e}', exc_info=True)
            raise ValueError(e) from e

    @mdc_handler
    def publish_to_topic(self, mr_topic, event_json, **kwargs):
        """
        Publish the event to the DMaaP Message Router topic.

        Args:
            mr_topic (enum) : Message Router topic to publish.
            event_json (dict): the json data to be published.

        Raises:
            Exception: if post request fails.
        """
        try:
            session = requests.Session()
            topic_url = self.streams_publishes[mr_topic].get('dmaap_info').get('topic_url')
            headers = {'content-type': 'application/json', 'x-transactionid': kwargs['request_id'],
                       'InvocationID': kwargs['invocation_id'], 'RequestID': kwargs['request_id']}
            logger.info(f'Publishing event to MR topic: {topic_url}')
            response = session.post(topic_url, headers=headers,
                                    auth=HTTPBasicAuth(self.aaf_id, self.aaf_pass), json=event_json,
                                    verify=(self.ca_cert_path if self.enable_tls else False))
            response.raise_for_status()
        except Exception as e:
            logger.error(f'Failed to publish event due to exception: {e}', exc_info=True)
            raise e

    @mdc_handler
    def get_from_topic(self, mr_topic, consumer_id, consumer_group='dcae_pmsh_cg', timeout=5000,
                       **kwargs):
        """
        Returns the json data from the MrTopic.

        Args:
            mr_topic (enum) : Message Router topic to subscribe.
            consumer_id (str): Within your subscribers group, a name that uniquely
            identifies your subscribers process.
            consumer_group (str): A name that uniquely identifies your subscribers.
            timeout (int): The request timeout value in mSec.

        Returns:
            list[str]: the json response from DMaaP Message Router topic.
        """
        try:
            session = requests.Session()
            topic_url = self.streams_subscribes[mr_topic].get('dmaap_info').get('topic_url')
            headers = {'accept': 'application/json', 'content-type': 'application/json',
                       'InvocationID': kwargs['invocation_id'], 'RequestID': kwargs['request_id']}
            logger.info(f'Fetching messages from MR topic: {topic_url}')
            response = session.get(f'{topic_url}/{consumer_group}/{consumer_id}'
                                   f'?timeout={timeout}',
                                   auth=HTTPBasicAuth(self.aaf_id, self.aaf_pass), headers=headers,
                                   verify=(self.ca_cert_path if self.enable_tls else False))
            if response.status_code == 503:
                logger.error(f'MR Service is unavailable at present: {response.content}')
            response.raise_for_status()
            if response.ok:
                return response.json()
        except Exception as e:
            logger.error(f'Failed to fetch message from MR: {e}', exc_info=True)
            raise
