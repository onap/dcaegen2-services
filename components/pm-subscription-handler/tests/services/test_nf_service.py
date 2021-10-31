# ============LICENSE_START===================================================
#  Copyright (C) 2021 Nordix Foundation.
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
from flask import current_app
from mod.api.db_models import NetworkFunctionModel
from mod import aai_client
from tests.base_setup import BaseClassSetup
from mod.api.services import nf_service
from mod.network_function import NetworkFunctionFilter


class NetworkFunctionServiceTestCase(BaseClassSetup):
    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    def setUp(self):
        super().setUp()
        current_app.config['app_config'] = self.app_conf
        with open(os.path.join(os.path.dirname(__file__),
                               '../data/create_subscription_request.json'), 'r') as data:
            self.subscription_request = data.read()
        with open(os.path.join(os.path.dirname(__file__), '../data/aai_xnfs.json'), 'r') as data:
            self.aai_response_data = data.read()
        with open(os.path.join(os.path.dirname(__file__), '../data/aai_model_info.json'),
                  'r') as data:
            self.good_model_info = data.read()

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

    def create_test_subs(self, new_sub_name, new_msrmt_grp_name):
        subscription = self.subscription_request.replace('ExtraPM-All-gNB-R2B', new_sub_name)
        subscription = subscription.replace('msrmt_grp_name', new_msrmt_grp_name)
        return subscription

    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_capture_filtered_nfs(self, mock_filter_call, mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        subscription = json.loads(self.subscription_request)['subscription']
        mock_filter_call.return_value = NetworkFunctionFilter(**subscription["nfFilter"])
        filtered_nfs = nf_service.capture_filtered_nfs(subscription["subscriptionName"])
        self.assertEqual(len(filtered_nfs), 2)
        self.assertEqual(filtered_nfs[0].nf_name, 'pnf201')
        self.assertEqual(filtered_nfs[1].nf_name, 'pnf_33_ericsson')

    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_create_nf_event_body(self, mock_filter_call, mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        subscription = json.loads(self.subscription_request)['subscription']
        mock_filter_call.return_value = NetworkFunctionFilter(**subscription["nfFilter"])
        nf = nf_service.capture_filtered_nfs(subscription["subscriptionName"])[0]
        operational_policy_name = subscription['operationalPolicyName']
        control_loop_name = subscription['controlLoopName']
        event_body = nf_service.create_nf_event_body(nf, 'CREATE',
                                                     operational_policy_name,
                                                     control_loop_name)
        self.assertEqual(event_body['nfName'], nf.nf_name)
        self.assertEqual(event_body['ipAddress'], nf.ipv6_address)
        self.assertEqual(event_body['blueprintName'], nf.sdnc_model_name)
        self.assertEqual(event_body['blueprintVersion'], nf.sdnc_model_version)
        self.assertEqual(event_body['operationalPolicyName'],
                         operational_policy_name)
        self.assertEqual(event_body['changeType'], 'CREATE')
        self.assertEqual(event_body['controlLoopName'],
                         control_loop_name)

    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_save_nf_new_nf(self, mock_filter_call, mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        subscription = json.loads(self.subscription_request)['subscription']
        mock_filter_call.return_value = NetworkFunctionFilter(**subscription["nfFilter"])
        nf = nf_service.capture_filtered_nfs(subscription["subscriptionName"])[0]
        nf.nf_name = 'newnf1'
        nf_service.save_nf(nf)
        network_function = NetworkFunctionModel.query.filter(
            NetworkFunctionModel.nf_name == nf.nf_name).one_or_none()
        self.assertIsNotNone(network_function)
