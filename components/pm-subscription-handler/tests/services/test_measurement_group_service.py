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
from mod.network_function import NetworkFunction
from mod.pmsh_config import AppConfig
from mod import db
from tests.base_setup import BaseClassSetup
from mod.api.services import measurement_group_service
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
    def test_publish_measurement_group(self, mock_mr):
        super().setUpAppConf()
        nf_1 = NetworkFunction(**{'nf_name': 'pnf_1',
                                  'ipv4_address': '204.120.0.15',
                                  'ipv6_address': '2001:db8:3333:4444:5555:6666:7777:8888',
                                  'model_invariant_id': 'some_id',
                                  'model_version_id': 'some_other_id'},
                               sdnc_model_name='blah',
                               sdnc_model_version=1.0, )
        measurement_grp = MeasurementGroupModel('sub_publish',
                                                'msg_publish', 'UNLOCKED',
                                                15, 'pm.xml', [{"measurementType": "counter_a"}],
                                                [{"DN": "string"}])
        measurement_group_service.publish_measurement_group(
            'sub_publish', measurement_grp, nf_1,
            'pmsh-operational-policy', 'pmsh-control-loop')
        mock_mr.assert_called_once_with('policy_pm_publisher',
                                        {'nfName': 'pnf_1',
                                         'ipAddress': '2001:db8:3333:4444:5555:6666:7777:8888',
                                         'blueprintName': 'blah',
                                         'blueprintVersion': 1.0,
                                         'operationalPolicyName': 'pmsh-operational-policy',
                                         'changeType': 'CREATE',
                                         'controlLoopName': 'pmsh-control-loop',
                                         'subscription':
                                             {'administrativeState': 'UNLOCKED',
                                              'subscriptionName': 'sub_publish',
                                              'fileBasedGP': 15,
                                              'fileLocation': 'pm.xml',
                                              'measurementGroup':
                                                  {'measurementGroupName': 'msg_publish',
                                                   'measurementTypes':
                                                       [{"measurementType": "counter_a"}],
                                                   'managedObjectDNsBasic': [{"DN": "string"}]}}})

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
