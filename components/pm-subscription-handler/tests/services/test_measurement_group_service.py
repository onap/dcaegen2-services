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
from mod.network_function import NetworkFunctionFilter
from mod.pmsh_config import AppConfig, MRTopic
from mod import aai_client, db
from tests.base_setup import BaseClassSetup
from mod.api.services import nf_service, measurement_group_service
from mod.api.db_models import MeasurementGroupModel, NfMeasureGroupRelationalModel
from mod.subscription import SubNfState


class MeasurementGroupServiceTestCase(BaseClassSetup):
    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    def setUp(self):
        super().setUp()
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

    @patch.object(AppConfig, 'publish_to_topic')
    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_publish_measurement_group(self, mock_filter_call, mock_model_aai, mock_aai, mock_mr):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        subscription = self.create_test_subs('sub_publish', 'msg_publish')
        subscription = json.loads(subscription)['subscription']
        mock_filter_call.return_value = NetworkFunctionFilter(**subscription["nfFilter"])
        filtered_nfs = nf_service.capture_filtered_nfs(subscription["subscriptionName"])
        measurement_grp = MeasurementGroupModel('sub_publish',
                                                'msg_publish', 'UNLOCKED',
                                                15, 'pm.xml', [], [])
        measurement_group_service.publish_measurement_group(
            'sub_publish', measurement_grp,
            filtered_nfs[1])
        event_body = mock_mr.call_args._get_call_arguments()[0][1]
        self.assertEqual(mock_mr.call_args._get_call_arguments()[0][0],
                         MRTopic.POLICY_PM_PUBLISHER.value)
        self.assertEqual(event_body['subscription']['subscriptionName'], 'sub_publish')
        self.assertEqual(event_body['subscription']['administrativeState'], 'UNLOCKED')
        self.assertEqual(event_body['subscription']['fileBasedGP'], 15)
        self.assertEqual(event_body['subscription']['fileLocation'], 'pm.xml')
        self.assertEqual(event_body['subscription']['measurementGroup']
                         ['measurementGroupName'], 'msg_publish')
        self.assertEqual(event_body['nfName'], 'pnf_33_ericsson')
        self.assertEqual(event_body['changeType'], 'CREATE')

    def test_save_measurement_group(self):
        subscription = json.loads(self.subscription_request)['subscription']
        mes_grp = subscription['measurementGroups'][0]['measurementGroup']
        measurement_group_service.save_measurement_group(mes_grp, "ExtraPM-All-gNB-R2B")
        db.session.commit()
        measurement_grp = (MeasurementGroupModel.query.filter(
            MeasurementGroupModel.measurement_group_name == mes_grp['measurementGroupName'],
            MeasurementGroupModel.subscription_name == 'ExtraPM-All-gNB-R2B').one_or_none())
        self.assertIsNotNone(measurement_grp)

    def test_apply_nf_to_measgroup(self):
        measurement_group_service.apply_nf_to_measgroup("pnf_test", "measure_grp_name")
        db.session.commit()
        measurement_grp_rel = (NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == 'measure_grp_name',
            NfMeasureGroupRelationalModel.nf_name == 'pnf_test').one_or_none())
        db.session.commit()
        self.assertIsNotNone(measurement_grp_rel)
        self.assertEqual(measurement_grp_rel.nf_measure_grp_status,
                         SubNfState.PENDING_CREATE.value)

    def create_test_subs(self, new_sub_name, new_msrmt_grp_name):
        subscription = self.subscription_request.replace('ExtraPM-All-gNB-R2B', new_sub_name)
        subscription = subscription.replace('msrmt_grp_name', new_msrmt_grp_name)
        return subscription
