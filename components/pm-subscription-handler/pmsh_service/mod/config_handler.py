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
from os import environ

import requests
from tenacity import retry, wait_fixed, stop_after_attempt, retry_if_exception_type

import mod.pmsh_logging as logger


class ConfigHandler:
    """ Handles retrieval of PMSH's configuration from Configbinding service."""

    def __init__(self):
        self.cbs_url = f'http://{self.cbs_hostname}:{str(self.cbs_port)}/' \
            f'service_component_all/{self.hostname}'
        self._config = None

    @property
    def cbs_hostname(self):
        return _get_environment_variable('CONFIG_BINDING_SERVICE_SERVICE_HOST')

    @property
    def cbs_port(self):
        return _get_environment_variable('CONFIG_BINDING_SERVICE_SERVICE_PORT')

    @property
    def hostname(self):
        return _get_environment_variable('HOSTNAME')

    @retry(wait=wait_fixed(2), stop=stop_after_attempt(5), retry=retry_if_exception_type(Exception))
    def get_config(self):
        """ Retrieves PMSH's configuration from Configbinding service. If a non-2xx response
        is received, it retries after 2 seconds for 5 times before raising an exception.

        Returns:
            dict: Dictionary representation of the the service configuration

        Raises:
            Exception: If any error occurred pulling configuration from Configbinding service.
        """

        try:
            response = requests.get(self.cbs_url)
            response.raise_for_status()
            self._config = response.json()
            logger.debug(f'PMSH Configuration from Configbinding Service: {self._config}')
            return self._config
        except Exception as err:
            raise Exception(f'Error retrieving configuration from CBS: {err}')


def _get_environment_variable(env_var_key):
    try:
        env_var = environ[env_var_key]
    except KeyError as error:
        raise KeyError(f'Environment variable {env_var_key} must be set. {error}')
    return env_var
