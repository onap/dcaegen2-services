# ============LICENSE_START===================================================
#  Copyright (C) 2019-2021 Nordix Foundation.
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
from os import getenv
from threading import Timer

import requests
from onap_dcae_cbs_docker_client.client import get_all
from onaplogging.mdcContext import MDC
from requests.auth import HTTPBasicAuth
from schema import Schema, And, Or, SchemaError
from tenacity import wait_fixed, stop_after_attempt, retry, retry_if_exception_type

import mod.network_function
from mod import logger
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


class MySingleton(object):
    instances = {}

    def __new__(cls, clz=None):
        if clz is None:
            if cls.__name__ not in MySingleton.instances:
                MySingleton.instances[cls.__name__] = \
                    object.__new__(cls)
            return MySingleton.instances[cls.__name__]
        MySingleton.instances[clz.__name__] = clz()
        MySingleton.first = clz
        return type(clz.__name__, (MySingleton,), dict(clz.__dict__))


class AppConfig:
    INSTANCE = None

    def __init__(self):
        try:
            conf = self._get_pmsh_config()
        except Exception:
            raise
        self.aaf_creds = {'aaf_id': conf['config'].get('aaf_identity'),
                          'aaf_pass': conf['config'].get('aaf_password')}
        self.enable_tls = conf['config'].get('enable_tls')
        self.ca_cert_path = conf['config'].get('ca_cert_path')
        self.cert_path = conf['config'].get('cert_path')
        self.key_path = conf['config'].get('key_path')
        self.streams_subscribes = conf['config'].get('streams_subscribes')
        self.streams_publishes = conf['config'].get('streams_publishes')
        self.operational_policy_name = conf['config'].get('operational_policy_name')
        self.control_loop_name = conf['config'].get('control_loop_name')
        self.subscription = Subscription(**conf['policy']['subscription'])
        self.nf_filter = mod.network_function.NetworkFunctionFilter(**self.subscription.nfFilter)

    def __new__(cls, *args, **kwargs):
        if AppConfig.INSTANCE is None:
            AppConfig.INSTANCE = super().__new__(cls, *args, **kwargs)
        return AppConfig.INSTANCE

    @mdc_handler
    @retry(wait=wait_fixed(5), stop=stop_after_attempt(5), retry=retry_if_exception_type(Exception))
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
            self.validate_config_subscription(config)
            return config
        except Exception as err:
            logger.error(f'Failed to get config from CBS: {err}', exc_info=True)
            raise Exception

    def refresh_config(self):
        """
        Update the relevant attributes of the AppConfig object.

        Raises:
            Exception: if cbs request fails.
        """
        try:
            app_conf = self._get_pmsh_config()
            self.subscription.administrativeState = app_conf['policy']['subscription'][
                'administrativeState']
            logger.info("AppConfig data has been refreshed")
        except ValueError or Exception as e:
            logger.error(f'Failed to refresh AppConfig: {e}', exc_info=True)

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
            return _MrSub(sub_name, self.aaf_creds, self.ca_cert_path,
                          self.enable_tls, self.cert_params, **self.streams_subscribes[sub_name])
        except KeyError as e:
            logger.error(f'Failed to get MrSub {sub_name}: {e}', exc_info=True)
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
            return _MrPub(pub_name, self.aaf_creds, self.ca_cert_path,
                          self.enable_tls, self.cert_params, **self.streams_publishes[pub_name])
        except KeyError as e:
            logger.error(f'Failed to get MrPub {pub_name}: {e}', exc_info=True)
            raise

    @property
    def cert_params(self):
        """
        Returns the tls artifact paths.

        Returns:
            cert_path, key_path (tuple): the path to tls cert and key.
        """
        return self.cert_path, self.key_path

    def validate_config_subscription(self, config):
        """
        Returns true if valid config is provided, false otherwise

        Args:
            config (dict): Dictionary representation of the the service configuration

        Returns:
            valid (bool): true if config subscription is valid, false otherwise

        Raises:
            SchemaError: if the config subscription is invalid
        """
        schema = Schema({
            'subscriptionName': str,
            'administrativeState': And(str, Or('UNLOCKED', "LOCKED")),
            'fileBasedGP': int,
            'fileLocation': str,
            'nfFilter': {
                'nfNames': [str],
                'modelInvariantIDs': [str],
                'modelVersionIDs': [str],
                "modelName": [str]
            },
            'measurementGroups':
                And(lambda n: len(n) > 0,
                    [
                        {'measurementGroup':
                            {
                                'measurementTypes': And(
                                    lambda n: len(n) > 0,
                                    [
                                        {'measurementType': str}
                                    ],
                                    error="Format of measurementTypes is invalid."
                                ),
                                'managedObjectDNsBasic': And(
                                    lambda n: len(n) > 0,
                                    [
                                        {'DN': str}
                                    ],
                                    error="Format of managedObjectDNsBasic is invalid."
                                )
                            }}],
                    error="Format of measurementGroups is invalid.")
        })
        try:
            data = config["policy"]["subscription"]
            schema.validate(data)
            nf_Filters = data["nfFilter"]
            for filter in nf_Filters:
                if len(nf_Filters[filter]) > 0:
                    valid = True
                    break
            else:
                raise SchemaError("Filters within nfFilter are empty")
        except SchemaError as e:
            valid = False
            logger.error(f"Invalid subscription object found: \n{e}", exc_info=True)

        return valid


class _DmaapMrClient:
    def __init__(self, aaf_creds, ca_cert_path, enable_tls, cert_params, **kwargs):
        """
        A DMaaP Message Router utility class.
        Sub classes should be invoked via the AppConfig.get_mr_{pub|sub} only.
        Args:
            aaf_creds (dict): a dict of aaf secure credentials.
            ca_cert_path (str): path to the ca certificate.
            enable_tls (bool): TLS if True, else False
            cert_params (tuple): client side (cert, key) tuple.
            **kwargs: a dict of streams_{subscribes|publishes} data.
        """
        self.topic_url = kwargs.get('dmaap_info').get('topic_url')
        self.aaf_id = aaf_creds.get('aaf_id')
        self.aaf_pass = aaf_creds.get('aaf_pass')
        self.ca_cert_path = ca_cert_path
        self.enable_tls = enable_tls
        self.cert_params = cert_params


class _MrPub(_DmaapMrClient):
    def __init__(self, pub_name, aaf_creds, ca_cert_path, enable_tls, cert_params, **kwargs):
        self.pub_name = pub_name
        super().__init__(aaf_creds, ca_cert_path, enable_tls, cert_params, **kwargs)

    @mdc_handler
    def publish_to_topic(self, event_json, **kwargs):
        """
        Publish the event to the DMaaP Message Router topic.

        Args:
            event_json (dict): the json data to be published.

        Raises:
            Exception: if post request fails.
        """
        try:
            session = requests.Session()
            headers = {'content-type': 'application/json', 'x-transactionid': kwargs['request_id'],
                       'InvocationID': kwargs['invocation_id'],
                       'RequestID': kwargs['request_id']
                       }
            logger.info(f'Publishing event to {self.topic_url}')
            response = session.post(self.topic_url, headers=headers,
                                    auth=HTTPBasicAuth(self.aaf_id, self.aaf_pass), json=event_json,
                                    verify=(self.ca_cert_path if self.enable_tls else False))
            response.raise_for_status()
        except Exception as e:
            raise e

    def publish_subscription_event_data(self, subscription, nf, app_conf):
        """
        Update the Subscription dict with xnf and policy name then publish to DMaaP MR topic.

        Args:
            subscription (Subscription): the `Subscription` <Subscription> object.
            nf (NetworkFunction): the NetworkFunction to include in the event.
            app_conf (AppConfig): the application configuration.
        """
        try:
            subscription_event = subscription.prepare_subscription_event(nf, app_conf)
            self.publish_to_topic(subscription_event)
        except Exception as e:
            logger.error(f'Failed to publish to topic {self.topic_url}: {e}', exc_info=True)


class _MrSub(_DmaapMrClient):
    def __init__(self, sub_name, aaf_creds, ca_cert_path, enable_tls, cert_params, **kwargs):
        self.sub_name = sub_name
        super().__init__(aaf_creds, ca_cert_path, enable_tls, cert_params, **kwargs)

    @mdc_handler
    def get_from_topic(self, consumer_id, consumer_group='dcae_pmsh_cg', timeout=1000, **kwargs):
        """
        Returns the json data from the MrTopic.

        Args:
            consumer_id (str): Within your subscribers group, a name that uniquely
            identifies your subscribers process.
            consumer_group (str): A name that uniquely identifies your subscribers.
            timeout (int): The request timeout value in mSec.

        Returns:
            list[str]: the json response from DMaaP Message Router topic.
        """
        try:
            session = requests.Session()
            headers = {'accept': 'application/json', 'content-type': 'application/json',
                       'InvocationID': kwargs['invocation_id'],
                       'RequestID': kwargs['request_id']}
            logger.info(f'Fetching messages from MR topic: {self.topic_url}')
            response = session.get(f'{self.topic_url}/{consumer_group}/{consumer_id}'
                                   f'?timeout={timeout}',
                                   auth=HTTPBasicAuth(self.aaf_id, self.aaf_pass), headers=headers,
                                   verify=(self.ca_cert_path if self.enable_tls else False))
            if response.status_code == 503:
                logger.error(f'MR Service is unavailable at present: {response.content}')
                pass
            response.raise_for_status()
            if response.ok:
                return response.json()
        except Exception as e:
            logger.error(f'Failed to fetch message from MR: {e}', exc_info=True)
            raise


class PeriodicTask(Timer):
    """
    See :class:`Timer`.
    """

    def run(self):
        self.function(*self.args, **self.kwargs)
        while not self.finished.wait(self.interval):
            try:
                self.function(*self.args, **self.kwargs)
            except Exception as e:
                logger.error(f'Exception in thread: {self.name}: {e}', exc_info=True)
