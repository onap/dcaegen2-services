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
import unittest
import requests
import responses
from os import environ
from os import path
from unittest.mock import patch
from tenacity import wait_none
from pmsh_service.mod.config_handler import ConfigHandler


class ConfigHandlerTest(unittest.TestCase):

    def setUp(self):
        self.env_vars = {'CONFIG_BINDING_SERVICE_SERVICE_HOST': 'cbs_hostname',
                         'CONFIG_BINDING_SERVICE_SERVICE_PORT': '10000',
                         'HOSTNAME': 'hostname'}
        for key, value in self.env_vars.items():
            environ[key] = value
        self.cbs_url = 'http://cbs_hostname:10000/service_component_all/hostname'
        self.expected_config = self._get_expected_config()

    def test_missing_environment_variable(self):
        for key, value in self.env_vars.items():
            with self.assertRaises(KeyError):
                environ.pop(key)
                test_value = globals()[value]
                test_value()
            environ[key] = value

    @responses.activate
    def test_get_config_success(self):
        responses.add(responses.GET, self.cbs_url, json=json.dumps(self.expected_config),
                      status=200)

        config = ConfigHandler()
        config._get_config_from_cbs.retry.wait = wait_none()

        self.assertEqual(self.expected_config, config.get_config())

    def test_get_config_exists(self):
        config = ConfigHandler()
        config._config = {}

        with patch.object(ConfigHandler, '_get_config_from_cbs') as mock_get_config_from_cbs:
            config.get_config()
        self.assertEqual(0,  mock_get_config_from_cbs.call_count)

    @responses.activate
    def test_get_config_error(self):
        responses.add(responses.GET, self.cbs_url, status=404)
        config = ConfigHandler()
        config._get_config_from_cbs.retry.wait = wait_none()

        requests.get(self.cbs_url)

        with self.assertRaises(Exception):
            config.get_config()

    @responses.activate
    def test_get_config_max_retries_error(self):
        retry_limit = 5
        config = ConfigHandler()
        config._get_config_from_cbs.retry.wait = wait_none()

        for __ in range(retry_limit):
            responses.add(responses.GET, self.cbs_url, status=500)

        with self.assertRaises(Exception):
            config.get_config()
        self.assertEqual(retry_limit, len(responses.calls))

    @responses.activate
    def test_get_config_less_than_5_retries_success(self):
        retry_attempts = 4
        responses.add(responses.GET, self.cbs_url, status=500)
        responses.add(responses.GET, self.cbs_url, status=400)
        responses.add(responses.GET, self.cbs_url, status=300)
        responses.add(responses.GET, self.cbs_url, json=json.dumps(self.expected_config),
                      status=200)

        config = ConfigHandler()
        config._get_config_from_cbs.retry.wait = wait_none()
        config.get_config()

        self.assertEqual(retry_attempts, len(responses.calls))

    @staticmethod
    def _get_expected_config():
        with open(path.join(path.dirname(__file__), 'expected_config.json'))as json_file:
            return json.load(json_file)
