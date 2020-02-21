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
import unittest
from os import environ
from os import path

import responses
from tenacity import wait_none

from mod.config_handler import ConfigHandler


class ConfigHandlerTestCase(unittest.TestCase):

    def setUp(self):
        self.env_vars = {'CONFIG_BINDING_SERVICE_SERVICE_HOST': 'cbs_hostname',
                         'CONFIG_BINDING_SERVICE_SERVICE_PORT': '10000',
                         'HOSTNAME': 'hostname'}
        for key, value in self.env_vars.items():
            environ[key] = value
        self.cbs_url = 'http://cbs_hostname:10000/service_component_all/hostname'
        with open(path.join(path.dirname(__file__), 'data/cbs_data_2.json'))as json_file:
            self.expected_config = json.load(json_file)

    def test_missing_environment_variable(self):
        for key, value in self.env_vars.items():
            with self.assertRaises(KeyError):
                environ.pop(key)
                test_value = globals()[value]
                test_value()
            environ[key] = value

    @responses.activate
    def test_get_config_success(self):
        responses.add(responses.GET, self.cbs_url, json=self.expected_config,
                      status=200)

        config_handler = ConfigHandler()
        config_handler.get_config.retry.wait = wait_none()

        self.assertEqual(self.expected_config, config_handler.get_config())

    @responses.activate
    def test_get_config_error(self):
        responses.add(responses.GET, self.cbs_url, status=404)
        config_handler = ConfigHandler()
        config_handler.get_config.retry.wait = wait_none()

        with self.assertRaises(Exception):
            config_handler.get_config()

    @responses.activate
    def test_get_config_max_retries_error(self):
        retry_limit = 5
        config_handler = ConfigHandler()
        config_handler.get_config.retry.wait = wait_none()

        for __ in range(retry_limit):
            responses.add(responses.GET, self.cbs_url, status=500)

        with self.assertRaises(Exception):
            config_handler.get_config()
        self.assertEqual(retry_limit, len(responses.calls))

    @responses.activate
    def test_get_config_less_than_5_retries_success(self):
        retry_attempts = 4
        responses.add(responses.GET, self.cbs_url, status=500)
        responses.add(responses.GET, self.cbs_url, status=400)
        responses.add(responses.GET, self.cbs_url, status=300)
        responses.add(responses.GET, self.cbs_url, json=json.dumps(self.expected_config),
                      status=200)

        config_handler = ConfigHandler()
        config_handler.get_config.retry.wait = wait_none()
        config_handler.get_config()

        self.assertEqual(retry_attempts, len(responses.calls))
