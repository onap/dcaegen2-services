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
import threading
import uuid
from os import getenv
from threading import Timer

import requests
from onap_dcae_cbs_docker_client.client import get_all
from onaplogging.mdcContext import MDC
from requests.auth import HTTPBasicAuth
from tenacity import wait_fixed, stop_after_attempt, retry, retry_if_exception_type

from mod import logger
from mod.network_function import NetworkFunctionFilter
from mod.subscription import Subscription


def mdc_handler(function):
    def decorator(*args, **kwargs):
        request_id = str(uuid.uuid1())
        invocation_id = str(uuid.uuid1())
        MDC.put('ServiceName', getenv('HOSTNAME'))
        MDC.put('RequestID', request_id)
        MDC.put('InvocationID', invocation_id)

        kwargs['request_id'] = request_id
        kwargs['invocation_id'] = invocation_id
        return function(*args, **kwargs)
    return decorator


class ThreadSafeSingleton(type):
    _instances = {}
    _singleton_lock = threading.Lock()

    def __call__(cls, *args, **kwargs):
        # double-checked locking pattern (https://en.wikipedia.org/wiki/Double-checked_locking)
        if cls not in cls._instances:
            with cls._singleton_lock:
                if cls not in cls._instances:
                    cls._instances[cls] = super(ThreadSafeSingleton, cls).__call__(*args, **kwargs)
        return cls._instances[cls]


class AppConfig(metaclass=ThreadSafeSingleton):

    def __init__(self):
        try:
            conf = self._get_pmsh_config()
        except Exception:
            raise
        self.aaf_creds = {'aaf_id': conf['config'].get('aaf_identity'),
                          'aaf_pass': conf['config'].get('aaf_password')}
        self.cert_path = conf['config'].get('cert_path')
        self.key_path = conf['config'].get('key_path')
        self.streams_subscribes = conf['config'].get('streams_subscribes')
        self.streams_publishes = conf['config'].get('streams_publishes')
        self.operational_policy_name = conf['config'].get('operational_policy_name')
        self.control_loop_name = conf['config'].get('control_loop_name')
        self.subscription = Subscription(**conf['policy']['subscription'])
        self.nf_filter = NetworkFunctionFilter(**self.subscription.nfFilter)

    @mdc_handler
    @retry(wait=wait_fixed(2), stop=stop_after_attempt(5), retry=retry_if_exception_type(Exception))
    def _get_pmsh_config(self, **kwargs):
        """ Retrieves PMSH's configuration from Config binding service. If a non-2xx response
        is received, it retries after 2 seconds for 5 times before raising an exception.

        Returns:
            dict: Dictionary representation of the the service configuration

        Raises:
            Exception: If any error occurred pulling configuration from Config binding service.
        """
        try:
            logger.info('Fetching PMSH Configuration from CBS.')
            config = get_all()
            logger.info(f'Successfully fetched PMSH config from CBS: {config}')
            return config
        except Exception as err:
            logger.error(f'Failed to get config from CBS: {err}')
            raise Exception

    def refresh_config(self):
        """
        Update the relevant attributes of the AppConfig object.

        Raises:
            Exception: if cbs request fails.
        """
        try:
            app_conf = self._get_pmsh_config()
        except Exception:
            logger.debug("Failed to refresh AppConfig data")
            raise
        self.subscription.administrativeState = \
            app_conf['policy']['subscription']['administrativeState']
        self.nf_filter.nf_names = app_conf['policy']['subscription']['nfFilter']['nfNames']
        self.nf_filter.nf_sw_version = app_conf['policy']['subscription']['nfFilter']['swVersions']
        logger.info("AppConfig data has been refreshed")

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

    @mdc_handler
    def publish_to_topic(self, event_json, **kwargs):
        """
        Publish the event to the DMaaP Message Router topic.

        Args:
            event_json: the json data to be published.

        Raises:
            Exception: if post request fails.
        """
        try:
            session = requests.Session()
            headers = {'content-type': 'application/json', 'x-transactionid': kwargs['request_id'],
                       'InvocationID': kwargs['invocation_id'],
                       'RequestID': kwargs['request_id']
                       }
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

    @mdc_handler
    def get_from_topic(self, consumer_id, consumer_group='dcae_pmsh_cg', timeout=1000, **kwargs):
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
            headers = {'accept': 'application/json', 'content-type': 'application/json',
                       'InvocationID': kwargs['invocation_id'],
                       'RequestID': kwargs['request_id']}
            logger.debug(f'Fetching messages from MR topic: {self.topic_url}')
            response = session.get(f'{self.topic_url}/{consumer_group}/{consumer_id}'
                                   f'?timeout={timeout}',
                                   auth=HTTPBasicAuth(self.aaf_id, self.aaf_pass), headers=headers,
                                   verify=False)
            response.raise_for_status()
            if response.ok:
                topic_data = response.json()
        except Exception as e:
            logger.error(f'Failed to fetch message from MR: {e}')
        return topic_data


class PeriodicTask(Timer):
    """
    See :class:`Timer`.
    """

    def run(self):
        self.function(*self.args, **self.kwargs)
        while not self.finished.wait(self.interval):
            self.function(*self.args, **self.kwargs)
