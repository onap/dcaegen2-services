# ============LICENSE_START===================================================
#  Copyright (C) 2020-2021 Nordix Foundation.
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
from mod import aai_client
from tests.base_setup import BaseClassSetup
from mod.api.services import subscription_service, nf_service, measurement_group_service


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
    def test_capture_filtered_nfs(self, mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        subscription = json.loads(self.subscription_request)['subscription']
        filtered_nfs = nf_service.capture_filtered_nfs(subscription["nfFilter"])
        self.assertEqual(len(filtered_nfs), 2)
        self.assertEqual(filtered_nfs[0].nf_name, 'pnf201')
        self.assertEqual(filtered_nfs[1].nf_name, 'pnf_33_ericsson')

    def test_validate_measurement_group(self):
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-new2', 'msrmt_grp_name-new2')
        subscription = json.loads(subscription)['subscription']
        measurement1 = subscription['measurementGroups'][0]
        msg = measurement_group_service.validate_measurement_group(
            measurement1['measurementGroup'], subscription["subscriptionName"])
        self.assertEqual(len(msg), 0)

    @patch.object(nf_service, 'save_nf_filter')
    def test_validate_measurement_group_invalid(self, mock_save_filter):
        mock_save_filter.return_value = None
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-new2', 'msrmt_grp_name-new2')
        subscription = json.loads(subscription)['subscription']
        subscription_service.save_subscription_request(subscription)
        measurement1 = subscription['measurementGroups'][0]
        msg = measurement_group_service.validate_measurement_group(
            measurement1['measurementGroup'], subscription["subscriptionName"])
        self.assertEqual(msg[0], 'Measurement Group: msrmt_grp_name-new2  for '
                                 'Subscription: xtraPM-All-gNB-R2B-new2 already exists.')
