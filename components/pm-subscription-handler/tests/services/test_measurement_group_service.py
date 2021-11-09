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
from mod.api.services import measurement_group_service, nf_service
from mod.api.db_models import MeasurementGroupModel, NfMeasureGroupRelationalModel, \
    SubscriptionModel, NetworkFunctionModel
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

    def test_apply_nf_to_measurement_group_status(self):
        measurement_group_service.apply_nf_status_to_measurement_group(
            "pnf_test", "measure_grp_name", SubNfState.PENDING_CREATE.value)
        db.session.commit()
        measurement_grp_rel = (NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == 'measure_grp_name',
            NfMeasureGroupRelationalModel.nf_name == 'pnf_test').one_or_none())
        self.assertIsNotNone(measurement_grp_rel)
        self.assertEqual(measurement_grp_rel.nf_measure_grp_status,
                         SubNfState.PENDING_CREATE.value)

    def test_update_measurement_group_nf_status(self):
        measurement_group_service.apply_nf_status_to_measurement_group(
            "pnf_test", "measure_grp_name", SubNfState.PENDING_CREATE.value)
        measurement_group_service.update_measurement_group_nf_status(
            "measure_grp_name", SubNfState.CREATED.value, "pnf_test")
        db.session.commit()
        measurement_grp_rel = (NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == 'measure_grp_name',
            NfMeasureGroupRelationalModel.nf_name == 'pnf_test').one_or_none())
        self.assertIsNotNone(measurement_grp_rel)
        self.assertEqual(measurement_grp_rel.nf_measure_grp_status,
                         SubNfState.CREATED.value)

    def test_delete_nf_to_measurement_group_without_nf_delete(self):
        nf = NetworkFunction(nf_name='pnf_test1')
        nf_service.save_nf(nf)
        db.session.commit()
        measurement_group_service.apply_nf_status_to_measurement_group(
            "pnf_test1", "measure_grp_name1", SubNfState.PENDING_CREATE.value)
        measurement_group_service.apply_nf_status_to_measurement_group(
            "pnf_test1", "measure_grp_name2", SubNfState.PENDING_CREATE.value)
        measurement_group_service.delete_nf_to_measurement_group(
            "pnf_test1", "measure_grp_name1", SubNfState.DELETED.value)
        measurement_grp_rel = (NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == 'measure_grp_name1',
            NfMeasureGroupRelationalModel.nf_name == 'pnf_test1').one_or_none())
        self.assertIsNone(measurement_grp_rel)
        network_function = (NetworkFunctionModel.query.filter(
            NetworkFunctionModel.nf_name == 'pnf_test1').one_or_none())
        self.assertIsNotNone(network_function)

    def test_delete_nf_to_measurement_group_with_nf_delete(self):
        nf = NetworkFunction(nf_name='pnf_test2')
        nf_service.save_nf(nf)
        db.session.commit()
        measurement_group_service.apply_nf_status_to_measurement_group(
            "pnf_test2", "measure_grp_name2", SubNfState.PENDING_CREATE.value)
        measurement_group_service.delete_nf_to_measurement_group(
            "pnf_test2", "measure_grp_name2", SubNfState.DELETED.value)
        measurement_grp_rel = (NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == 'measure_grp_name2',
            NfMeasureGroupRelationalModel.nf_name == 'pnf_test2').one_or_none())
        self.assertIsNone(measurement_grp_rel)
        network_function = (NetworkFunctionModel.query.filter(
            NetworkFunctionModel.nf_name == 'pnf_test2').one_or_none())
        self.assertIsNone(network_function)

    @patch.object(NetworkFunction, 'delete')
    @patch('mod.logger.error')
    def test_delete_nf_to_measurement_group_failure(self, mock_logger, nf_delete_func):
        nf = NetworkFunction(nf_name='pnf_test2')
        nf_service.save_nf(nf)
        db.session.commit()
        measurement_group_service.apply_nf_status_to_measurement_group(
            "pnf_test2", "measure_grp_name2", SubNfState.PENDING_CREATE.value)
        nf_delete_func.side_effect = Exception('delete failed')
        measurement_group_service.delete_nf_to_measurement_group(
            "pnf_test2", "measure_grp_name2", SubNfState.DELETED.value)
        measurement_grp_rel = (NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == 'measure_grp_name2',
            NfMeasureGroupRelationalModel.nf_name == 'pnf_test2').one_or_none())
        self.assertIsNone(measurement_grp_rel)
        network_function = (NetworkFunctionModel.query.filter(
            NetworkFunctionModel.nf_name == 'pnf_test2').one_or_none())
        self.assertIsNotNone(network_function)
        mock_logger.assert_called_with('Failed to delete nf: pnf_test2 for '
                                       'measurement group: measure_grp_name2 due to: delete failed')

    @patch.object(db.session, 'commit')
    @patch('mod.logger.error')
    def test_update_nf_to_measurement_group_failure(self, mock_logger, db_commit_call):
        db_commit_call.side_effect = Exception('update failed')
        measurement_group_service.update_measurement_group_nf_status(
            "measure_grp_name2", SubNfState.CREATE_FAILED.value, "pnf_test2")
        mock_logger.assert_called_with('Failed to update nf: pnf_test2 for '
                                       'measurement group: measure_grp_name2 due to: update failed')

    def create_test_subs(self, new_sub_name, new_msrmt_grp_name):
        subscription = self.subscription_request.replace('ExtraPM-All-gNB-R2B', new_sub_name)
        subscription = subscription.replace('msrmt_grp_name', new_msrmt_grp_name)
        return subscription
