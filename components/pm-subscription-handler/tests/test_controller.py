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
import os
from unittest.mock import patch

import responses
from requests import Session

from mod import aai_client
from mod.api.controller import status, get_all_sub_to_nf_relations
from tests.base_setup import BaseClassSetup


class ControllerTestCase(BaseClassSetup):

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

    def test_status_response_healthy(self):
        self.assertEqual(status()['status'], 'healthy')

    @patch.object(Session, 'get')
    @patch.object(Session, 'put')
    def test_get_all_sub_to_nf_relations(self, mock_put_session, mock_get_session):
        mock_put_session.return_value.status_code = 200
        mock_put_session.return_value.text = self.aai_response_data
        mock_get_session.return_value.status_code = 200
        mock_get_session.return_value.text = self.good_model_info
        responses.add(responses.GET,
                      'https://aai:8443/aai/v20/service-design-and-creation/models/model/'
                      '7129e420-d396-4efb-af02-6b83499b12f8/model-vers/model-ver/'
                      'e80a6ae3-cafd-4d24-850d-e14c084a5ca9',
                      json=json.loads(self.good_model_info), status=200)
        self.xnfs = aai_client.get_pmsh_nfs_from_aai(self.app_conf)
        sub_model = self.app_conf.subscription.get()
        for nf in self.xnfs:
            self.app_conf.subscription.add_network_function_to_subscription(nf, sub_model)
        all_subs = get_all_sub_to_nf_relations()
        self.assertEqual(len(all_subs[0]['network_functions']), 3)
        self.assertEqual(all_subs[0]['subscription_name'], 'ExtraPM-All-gNB-R2B')
