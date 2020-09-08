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
from unittest import mock
from unittest.mock import patch

import responses
from requests import Session, HTTPError

import mod.aai_client as aai_client
from tests.base_setup import BaseClassSetup


class AaiClientTestCase(BaseClassSetup):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    def setUp(self):
        super().setUp()
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_xnfs.json'), 'r') as data:
            self.aai_response_data = data.read()
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_model_info.json'), 'r') as data:
            self.good_model_info = data.read()

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

    @patch('mod.network_function.NetworkFunction.set_sdnc_params')
    @patch.object(Session, 'get')
    @patch.object(Session, 'put')
    def test_aai_client_get_pm_sub_data_success(self, mock_put_session, mock_get_session,
                                                mock_get_sdnc_params):
        mock_put_session.return_value.status_code = 200
        mock_put_session.return_value.text = self.aai_response_data
        mock_get_session.return_value.status_code = 200
        mock_get_session.return_value.text = self.good_model_info
        mock_get_sdnc_params.return_value = True
        xnfs = aai_client.get_pmsh_nfs_from_aai(self.app_conf)
        self.assertEqual(self.app_conf.subscription.subscriptionName, 'ExtraPM-All-gNB-R2B')
        self.assertEqual(self.app_conf.subscription.administrativeState, 'UNLOCKED')
        self.assertEqual(len(xnfs), 3)

    @patch.object(Session, 'put')
    def test_aai_client_get_pm_sub_data_fail(self, mock_session):
        mock_session.return_value.status_code = 404
        with mock.patch('mod.aai_client._get_all_aai_nf_data', return_value=None):
            with self.assertRaises(RuntimeError):
                aai_client.get_pmsh_nfs_from_aai(self.app_conf)

    @responses.activate
    def test_aai_client_get_all_aai_xnf_data_not_found(self):
        responses.add(responses.PUT,
                      'https://1.2.3.4:8443/aai/v20/query?format=simple&nodesOnly=true',
                      json={'error': 'not found'}, status=404)
        self.assertIsNone(aai_client._get_all_aai_nf_data(self.app_conf))

    @responses.activate
    def test_aai_client_get_all_aai_xnf_data_success(self):
        responses.add(responses.PUT,
                      'https://aai:8443/aai/v20/query?format=simple&nodesOnly=true',
                      json={'dummy_data': 'blah_blah'}, status=200)
        self.assertIsNotNone(aai_client._get_all_aai_nf_data(self.app_conf))

    @responses.activate
    def test_aai_client_get_sdnc_params_success(self):
        responses.add(responses.GET,
                      'https://aai:8443/aai/v20/service-design-and-creation/models/model/'
                      '6fb9f466-7a79-4109-a2a3-72b340aca53d/model-vers/model-ver/'
                      '6d25b637-8bca-47e2-af1a-61258424183d',
                      json=json.loads(self.good_model_info), status=200)
        self.assertIsNotNone(aai_client.get_aai_model_data(self.app_conf,
                                                           '6fb9f466-7a79-4109-a2a3-72b340aca53d',
                                                           '6d25b637-8bca-47e2-af1a-61258424183d',
                                                           'pnf_1'))

    @responses.activate
    def test_aai_client_get_sdnc_params_fail(self):
        responses.add(responses.GET,
                      'https://aai:8443/aai/v20/service-design-and-creation/models/model/'
                      '9fb9f466-7a79-4109-a2a3-72b340aca53d/model-vers/model-ver/'
                      'b7469cc5-be51-41cc-b37f-361537656771', status=404)
        with self.assertRaises(HTTPError):
            aai_client.get_aai_model_data(self.app_conf, '9fb9f466-7a79-4109-a2a3-72b340aca53d',
                                          'b7469cc5-be51-41cc-b37f-361537656771', 'pnf_2')

    def test_aai_client_get_aai_service_url_fail(self):
        os.environ.clear()
        with self.assertRaises(KeyError):
            aai_client._get_aai_service_url()

    def test_aai_client_get_aai_service_url_success(self):
        self.assertEqual('https://aai:8443/aai/v20', aai_client._get_aai_service_url())
