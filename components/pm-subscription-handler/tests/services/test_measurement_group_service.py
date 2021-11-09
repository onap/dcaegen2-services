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
import copy
import json
import os
from unittest.mock import patch
from mod.network_function import NetworkFunction
from mod.pmsh_config import AppConfig
from mod import db
from tests.base_setup import BaseClassSetup
from mod.api.services import measurement_group_service
from mod.api.db_models import MeasurementGroupModel, NfMeasureGroupRelationalModel, \
    SubscriptionModel, NetworkFunctionFilterModel
from mod.subscription import SubNfState


class MeasurementGroupServiceTestCase(BaseClassSetup):
    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    def subscription_data(self, subscription_name):
        nf_filter = NetworkFunctionFilterModel(subscription_name, '{^pnf.*,^vnf.*}',
                                               '{}', '{}', '{}')
        mg_first = MeasurementGroupModel(subscription_name, 'MG1', 'UNLOCKED', 15, '/pm/pm.xml',
                                         "[{ \"measurementType\": \"countera\" },\
                                         { \"measurementType\": \"counterb\" }]",
                                         "[{ \"DN\":\"dna\"},{\"DN\":\"dnb\"}]")
        mg_second = copy.deepcopy(mg_first)
        mg_second.measurement_group_name = 'MG2'
        mg_second.administrative_state = 'LOCKED'
        nf_filter_list = [nf_filter]
        mg_list = [mg_first, mg_second]
        subscription_model = SubscriptionModel(subscription_name, 'pmsh_operational_policy',
                                               'pmsh_control_loop_name', 'LOCKED')
        subscription_model.network_filter = nf_filter_list
        subscription_model.measurement_groups = mg_list
        return subscription_model

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
        sub_model = SubscriptionModel('sub_publish', 'pmsh-operational-policy',
                                      'pmsh-control-loop', 'LOCKED')
        measurement_group_service.publish_measurement_group(
            sub_model, measurement_grp, nf_1)
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

    def test_measurement_group_encoder(self):
        self.mg_encode = measurement_group_service.measurement_group_encoder(
            self.subscription_data('nf_filter_encode').measurement_groups[0]
        )
        self.assertEqual(self.mg_encode['measurementGroup']['measurementGroupName'], 'MG1')
        self.assertEqual(self.mg_encode['measurementGroup']['managedObjectDNsBasic'],
                         '[{ \"DN\":\"dna\"},{\"DN\":\"dnb\"}]')
        self.assertEqual(self.mg_encode['measurementGroup']['administrativeState'], 'UNLOCKED')
        self.assertEqual(self.mg_encode['measurementGroup']['fileBasedGP'], 15)
        self.assertEqual(self.mg_encode['measurementGroup']['fileLocation'], '/pm/pm.xml')
        self.assertEqual(self.mg_encode['measurementGroup']['measurementTypes'],
                         "[{ \"measurementType\": \"countera\" },\
                                         { \"measurementType\": \"counterb\" }]")
        self.assertEqual(self.mg_encode['measurementGroup']['fileLocation'], '/pm/pm.xml')
        self.assertEqual(self.mg_encode['measurementGroup']['fileLocation'], '/pm/pm.xml')
