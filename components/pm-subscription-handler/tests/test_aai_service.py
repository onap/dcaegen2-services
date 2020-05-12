# ============LICENSE_START===================================================
#  Copyright (C) 2019-2020 Nordix Foundation.
# ============================================================================
# Licensed under the Apache License, Version 2.0 (the 'License');
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an 'AS IS' BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=====================================================
import json
import os
from test.support import EnvironmentVarGuard
from unittest import mock, TestCase
from unittest.mock import patch

import responses
from requests import Session

import mod.aai_client as aai_client
from mod import create_app


class AaiClientTestCase(TestCase):

    @patch('mod.get_db_connection_url')
    def setUp(self, mock_get_db_url):
        mock_get_db_url.return_value = 'sqlite://'
        self.env = EnvironmentVarGuard()
        self.env.set('HOSTNAME', 'myhost')
        self.env.set('AAI_SERVICE_HOST', '1.2.3.4')
        self.env.set('AAI_SERVICE_PORT', '8443')
        self.env.set('TESTING', 'True')
        self.env.set('LOGGER_CONFIG', os.path.join(os.path.dirname(__file__), 'log_config.yaml'))
        with open(os.path.join(os.path.dirname(__file__), 'data/cbs_data_1.json'), 'r') as data:
            self.cbs_data = json.load(data)
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_xnfs.json'), 'r') as data:
            self.aai_response_data = data.read()
        self.app = create_app()

    @patch.object(Session, 'put')
    def test_aai_client_get_pm_sub_data_success(self, mock_session):
        mock_session.return_value.status_code = 200
        mock_session.return_value.text = self.aai_response_data
        sub, xnfs = aai_client.get_pmsh_subscription_data(self.cbs_data)
        self.assertEqual(sub.subscriptionName, 'ExtraPM-All-gNB-R2B')
        self.assertEqual(sub.administrativeState, 'UNLOCKED')
        self.assertEqual(len(xnfs), 3)

    @patch.object(Session, 'put')
    def test_aai_client_get_pm_sub_data_fail(self, mock_session):
        mock_session.return_value.status_code = 404
        with mock.patch('mod.aai_client._get_all_aai_nf_data', return_value=None):
            with self.assertRaises(RuntimeError):
                aai_client.get_pmsh_subscription_data(self.cbs_data)

    @responses.activate
    def test_aai_client_get_all_aai_xnf_data_not_found(self):
        responses.add(responses.PUT,
                      'https://1.2.3.4:8443/aai/v16/query?format=simple&nodesOnly=true',
                      json={'error': 'not found'}, status=404)
        self.assertIsNone(aai_client._get_all_aai_nf_data())

    @responses.activate
    def test_aai_client_get_all_aai_xnf_data_success(self):
        responses.add(responses.PUT,
                      'https://1.2.3.4:8443/aai/v16/query?format=simple&nodesOnly=true',
                      json={'dummy_data': 'blah_blah'}, status=200)
        self.assertIsNotNone(aai_client._get_all_aai_nf_data())

    def test_aai_client_get_aai_service_url_fail(self):
        self.env.clear()
        with self.assertRaises(KeyError):
            aai_client._get_aai_service_url()

    def test_aai_client_get_aai_service_url_success(self):
        self.assertEqual('https://1.2.3.4:8443', aai_client._get_aai_service_url())
