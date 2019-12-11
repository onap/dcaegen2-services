# ============LICENSE_START===================================================
#  Copyright (C) 2019 Nordix Foundation.
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
import unittest
from test.support import EnvironmentVarGuard
from unittest import mock

import responses
from requests import Session

import mod.aai_client as aai_client


class AaiClientTestCase(unittest.TestCase):

    def setUp(self):
        self.env = EnvironmentVarGuard()
        self.env.set('AAI_SERVICE_HOST', '1.2.3.4')
        self.env.set('AAI_SERVICE_PORT_AAI_SSL', '8443')
        with open(os.path.join(os.path.dirname(__file__), 'data/cbs_data.json'), 'r') as data:
            self.cbs_data = json.load(data)
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_xnfs.json'), 'r') as data:
            self.aai_response_data = data.read()

    @mock.patch.object(Session, 'put')
    def test_aai_client_get_pm_sub_data_success(self, mock_session):
        mock_session.return_value.status_code = 200
        mock_session.return_value.text = self.aai_response_data
        sub, xnfs = aai_client.get_pmsh_subscription_data(self.cbs_data)
        self.assertEqual(sub.subscriptionName, 'ExtraPM-All-gNB-R2B')
        self.assertEqual(sub.administrativeState, 'UNLOCKED')
        self.assertEqual(len(xnfs), 6)

    @mock.patch.object(Session, 'put')
    def test_aai_client_get_pm_sub_data_fail(self, mock_session):
        mock_session.return_value.status_code = 404
        with mock.patch('aai_client._get_all_aai_xnf_data', return_value=None):
            with self.assertRaises(RuntimeError):
                aai_client.get_pmsh_subscription_data(self.cbs_data)

    @responses.activate
    def test_aai_client_get_all_aai_xnf_data_not_found(self):
        responses.add(responses.PUT,
                      'https://1.2.3.4:8443/aai/v16/query?format=simple&nodesOnly=true',
                      json={'error': 'not found'}, status=404)
        self.assertIsNone(aai_client._get_all_aai_xnf_data())

    @responses.activate
    def test_aai_client_get_all_aai_xnf_data_success(self):
        responses.add(responses.PUT,
                      'https://1.2.3.4:8443/aai/v16/query?format=simple&nodesOnly=true',
                      json={'dummy_data': 'blah_blah'}, status=200)
        self.assertIsNotNone(aai_client._get_all_aai_xnf_data())

    def test_aai_client_get_aai_service_url_fail(self):
        self.env.clear()
        with self.assertRaises(KeyError):
            aai_client._get_aai_service_url()

    def test_aai_client_get_aai_service_url_succses(self):
        self.assertEqual('https://1.2.3.4:8443', aai_client._get_aai_service_url())
