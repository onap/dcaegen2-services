# ============LICENSE_START===================================================
#  Copyright (C) 2021-2022 Nordix Foundation.
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

from mod.api.custom_exception import InvalidDataException, \
    DataConflictException, DuplicateDataException
from mod.network_function import NetworkFunction, NetworkFunctionFilter
from mod.pmsh_config import AppConfig
from mod import db, aai_client
from tests.base_setup import BaseClassSetup, create_subscription_data, \
    create_multiple_network_function_data
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
            sub_model, measurement_grp, nf_1, 'CREATE')
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

    @patch.object(AppConfig, 'publish_to_topic')
    def test_update_admin_status_to_locking(self, mock_mr):
        super().setUpAppConf()
        sub = create_subscription_data('sub')
        nf_list = create_multiple_network_function_data(['pnf_101', 'pnf_102'])
        db.session.add(sub)
        for nf in nf_list:
            nf_service.save_nf(nf)
            measurement_group_service. \
                apply_nf_status_to_measurement_group(nf.nf_name, sub.measurement_groups[0].
                                                     measurement_group_name,
                                                     SubNfState.CREATED.value)
        db.session.commit()
        measurement_group_service.update_admin_status(sub.measurement_groups[0], 'LOCKED')
        meas_grp = measurement_group_service.query_meas_group_by_name('sub', 'MG1')
        self.assertEqual(meas_grp.subscription_name, 'sub')
        self.assertEqual(meas_grp.measurement_group_name, 'MG1')
        self.assertEqual(meas_grp.administrative_state, 'LOCKING')
        meas_group_nfs = db.session.query(NfMeasureGroupRelationalModel).filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == meas_grp.measurement_group_name)\
            .all()
        for nf in meas_group_nfs:
            self.assertEqual(nf.nf_measure_grp_status, SubNfState.PENDING_DELETE.value)

    @patch.object(AppConfig, 'publish_to_topic')
    def test_update_admin_status_to_locked(self, mock_mr):
        super().setUpAppConf()
        sub = create_subscription_data('sub')
        db.session.add(sub)
        measurement_group_service.update_admin_status(sub.measurement_groups[0], 'LOCKED')
        meas_grp = measurement_group_service.query_meas_group_by_name('sub', 'MG1')
        self.assertEqual(meas_grp.subscription_name, 'sub')
        self.assertEqual(meas_grp.measurement_group_name, 'MG1')
        self.assertEqual(meas_grp.administrative_state, 'LOCKED')

    @patch.object(AppConfig, 'publish_to_topic')
    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_update_admin_status_to_unlocked_with_no_nfs(self, mock_filter_call,
                                                         mock_model_aai, mock_aai, mock_mr):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        super().setUpAppConf()
        sub = create_subscription_data('sub')
        sub.nfs = []
        db.session.add(sub)
        db.session.commit()
        mock_filter_call.return_value = NetworkFunctionFilter.get_network_function_filter('sub')
        measurement_group_service.update_admin_status(sub.measurement_groups[1], 'UNLOCKED')
        meas_grp = measurement_group_service.query_meas_group_by_name('sub', 'MG2')
        self.assertEqual(meas_grp.subscription_name, 'sub')
        self.assertEqual(meas_grp.measurement_group_name, 'MG2')
        self.assertEqual(meas_grp.administrative_state, 'UNLOCKED')
        meas_group_nfs = db.session.query(NfMeasureGroupRelationalModel).filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == meas_grp.measurement_group_name)\
            .all()
        for nf in meas_group_nfs:
            self.assertEqual(nf.nf_measure_grp_status, SubNfState.PENDING_CREATE.value)

    @patch.object(AppConfig, 'publish_to_topic')
    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_update_admin_status_to_unlocking(self, mock_filter_call,
                                              mock_model_aai, mock_aai, mock_mr):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        super().setUpAppConf()
        sub = create_subscription_data('sub')
        db.session.add(sub)
        db.session.commit()
        mock_filter_call.return_value = NetworkFunctionFilter.get_network_function_filter('sub')
        measurement_group_service.update_admin_status(sub.measurement_groups[1], 'UNLOCKED')
        meas_grp = measurement_group_service.query_meas_group_by_name('sub', 'MG2')
        self.assertEqual(meas_grp.subscription_name, 'sub')
        self.assertEqual(meas_grp.measurement_group_name, 'MG2')
        self.assertEqual(meas_grp.administrative_state, 'UNLOCKED')
        meas_group_nfs = db.session.query(NfMeasureGroupRelationalModel).filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == meas_grp.measurement_group_name)\
            .all()
        for nf in meas_group_nfs:
            self.assertEqual(nf.nf_measure_grp_status, SubNfState.PENDING_CREATE.value)

    def test_update_admin_status_for_missing_measurement_group(self):
        try:
            measurement_group_service.update_admin_status(None, 'UNLOCKED')
        except InvalidDataException as e:
            self.assertEqual(e.args[0], 'Requested measurement group not available '
                                        'for admin status update')

    def test_update_admin_status_for_data_conflict(self):
        super().setUpAppConf()
        sub = create_subscription_data('sub1')
        sub.measurement_groups[0].administrative_state = 'LOCKING'
        try:
            measurement_group_service.update_admin_status(sub.measurement_groups[0], 'LOCKED')
        except DataConflictException as e:
            self.assertEqual(e.args[0], 'Cannot update admin status as Locked request'
                                        ' is in progress for sub name: sub1  and '
                                        'meas group name: MG1')

    def test_update_admin_status_for_same_state(self):
        super().setUpAppConf()
        sub = create_subscription_data('sub1')
        try:
            measurement_group_service.update_admin_status(sub.measurement_groups[0], 'UNLOCKED')
        except InvalidDataException as e:
            self.assertEqual(e.args[0], 'Measurement group is already in UNLOCKED '
                                        'state for sub name: sub1  and meas group '
                                        'name: MG1')

    def test_lock_nf_to_meas_grp_for_locking_with_LOCKED_update(self):
        sub = create_subscription_data('sub')
        sub.measurement_groups[1].administrative_state = 'LOCKING'
        nf_list = create_multiple_network_function_data(['pnf_101'])
        db.session.add(sub)
        for nf in nf_list:
            nf_service.save_nf(nf)
            measurement_group_service. \
                apply_nf_status_to_measurement_group(nf.nf_name, sub.measurement_groups[1].
                                                     measurement_group_name,
                                                     SubNfState.PENDING_DELETE.value)
        db.session.commit()
        measurement_group_service.lock_nf_to_meas_grp(
            "pnf_101", "MG2", SubNfState.DELETED.value)
        measurement_grp_rel = (NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == 'MG2',
            NfMeasureGroupRelationalModel.nf_name == 'pnf_101').one_or_none())
        self.assertIsNone(measurement_grp_rel)

    def test_lock_nf_to_meas_grp_with_no_LOCKED_update(self):
        sub = create_subscription_data('sub')
        sub.measurement_groups[1].administrative_state = 'LOCKING'
        nf_list = create_multiple_network_function_data(['pnf_101', 'pnf_102'])
        db.session.add(sub)
        for nf in nf_list:
            nf_service.save_nf(nf)
            measurement_group_service. \
                apply_nf_status_to_measurement_group(nf.nf_name, sub.measurement_groups[1].
                                                     measurement_group_name,
                                                     SubNfState.PENDING_DELETE.value)
        db.session.commit()
        measurement_group_service.lock_nf_to_meas_grp(
            "pnf_101", "MG2", SubNfState.DELETED.value)
        measurement_grp_rel = (NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == 'MG2',
            NfMeasureGroupRelationalModel.nf_name == 'pnf_101').one_or_none())
        self.assertIsNone(measurement_grp_rel)

    def test_check_duplication_exception(self):
        sub = create_subscription_data('sub')
        db.session.add(sub)
        try:
            measurement_group_service.check_duplication('sub', 'MG1')
        except DuplicateDataException as e:
            self.assertEqual(e.args[0], 'Measurement Group Name: MG1 already exists.')

    def test_check_measurement_group_names_comply(self):
        MG = MeasurementGroupModel('sub', 'MG2', 'UNLOCKED', 15, '/pm/pm.xml',
                                   '[{ "measurementType": "countera" }, '
                                   '{ "measurementType": "counterb" }]',
                                   '[{ "DN":"dna"},{"DN":"dnb"}]')
        try:
            measurement_group_service.check_measurement_group_names_comply('MG1', MG)
        except InvalidDataException as e:
            self.assertEqual(e.args[0], 'Measurement Group Name in body does not match with URI')

    def test_filter_nf_to_meas_grp_for_delete(self):
        sub = create_subscription_data('sub')
        db.session.add(sub)
        nf = NetworkFunction(nf_name='pnf_test2')
        nf_service.save_nf(nf)
        measurement_group_service.apply_nf_status_to_measurement_group(
            "pnf_test2", "MG2", SubNfState.PENDING_DELETE.value)
        db.session.commit()
        measurement_group_service.filter_nf_to_meas_grp("pnf_test2", "MG2",
                                                        SubNfState.DELETED.value)
        measurement_grp_rel = (NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == 'MG2',
            NfMeasureGroupRelationalModel.nf_name == 'pnf_test2').one_or_none())
        self.assertIsNone(measurement_grp_rel)

    def test_filter_nf_to_meas_grp_for_create(self):
        sub = create_subscription_data('sub')
        db.session.add(sub)
        nf = NetworkFunction(nf_name='pnf_test2')
        nf_service.save_nf(nf)
        measurement_group_service.apply_nf_status_to_measurement_group(
            "pnf_test2", "MG2", SubNfState.PENDING_CREATE.value)
        db.session.commit()
        measurement_group_service.filter_nf_to_meas_grp(
            "pnf_test2", "MG2", SubNfState.CREATED.value)
        measurement_grp_rel = (NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == 'MG2',
            NfMeasureGroupRelationalModel.nf_name == 'pnf_test2').one_or_none())
        self.assertIsNotNone(measurement_grp_rel)
        self.assertEqual(measurement_grp_rel.nf_measure_grp_status, 'CREATED')
        network_function = (NetworkFunctionModel.query.filter(
            NetworkFunctionModel.nf_name == 'pnf_test2').one_or_none())
        self.assertIsNotNone(network_function)
        meas_grp = measurement_group_service.query_meas_group_by_name('sub', 'MG2')
        self.assertEqual(meas_grp.subscription_name, 'sub')
        self.assertEqual(meas_grp.measurement_group_name, 'MG2')
        self.assertEqual(meas_grp.administrative_state, 'UNLOCKED')
