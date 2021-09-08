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
from unittest.mock import patch, MagicMock
from mod.api.db_models import SubscriptionModel, MeasurementGroupModel, \
    NfMeasureGroupRelationalModel, NetworkFunctionModel, NfSubRelationalModel
from mod.network_function import NetworkFunctionFilter
from mod.subscription import SubNfState
from mod import aai_client
from mod.api.custom_exception import DuplicateDataException, InvalidDataException
from mod.pmsh_config import AppConfig
from tests.base_setup import BaseClassSetup
from mod.api.services import subscription_service, nf_service, measurement_group_service


class SubscriptionServiceTestCase(BaseClassSetup):
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

    def create_test_subs(self, new_sub_name, new_msrmt_grp_name):
        subscription = self.subscription_request.replace('ExtraPM-All-gNB-R2B', new_sub_name)
        subscription = subscription.replace('msrmt_grp_name', new_msrmt_grp_name)
        return subscription

    @patch('mod.api.services.subscription_service.save_nf_filter', MagicMock(return_value=None))
    @patch('mod.pmsh_config.AppConfig.publish_to_topic', MagicMock(return_value=None))
    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_create_subscription(self, mock_filter_call, mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-new', 'msrmt_grp_name-new')
        subscription = json.loads(subscription)['subscription']
        mock_filter_call.return_value = NetworkFunctionFilter(**subscription["nfFilter"])
        subscription_service.create_subscription(subscription)
        existing_subscription = (SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == 'xtraPM-All-gNB-R2B-new').one_or_none())
        self.assertIsNotNone(existing_subscription)
        existing_measurement_grp = (MeasurementGroupModel.query.filter(
            MeasurementGroupModel.measurement_group_name == 'msrmt_grp_name-new',
            MeasurementGroupModel.subscription_name == 'xtraPM-All-gNB-R2B-new').one_or_none())
        self.assertIsNotNone(existing_measurement_grp)
        msr_grp_nf_rel = (NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == 'msrmt_grp_name-new')).all()
        for pubslished_event in msr_grp_nf_rel:
            self.assertEqual(pubslished_event.nf_measure_grp_status,
                             SubNfState.PENDING_CREATE.value)

    @patch('mod.api.services.subscription_service.save_nf_filter', MagicMock(return_value=None))
    @patch.object(AppConfig, 'publish_to_topic')
    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_create_subscription_service_failed_rollback(self, mock_filter_call, mock_model_aai,
                                                         mock_aai, mock_publish):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        mock_publish.side_effect = InvalidDataException(["publish failed"])
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-fail1', 'msrmt_grp_name-fail1')
        subscription = json.loads(subscription)['subscription']
        mock_filter_call.return_value = NetworkFunctionFilter(**subscription["nfFilter"])
        try:
            subscription_service.create_subscription(subscription)
        except InvalidDataException as exception:
            self.assertEqual(exception.invalid_message, ["AAI call failed"])

        # Checking Rollback on publish failure with subscription and nfs captured
        existing_subscription = (SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == 'xtraPM-All-gNB-R2B-fail1').one_or_none())
        self.assertIsNotNone(existing_subscription)
        saved_nf_sub_rel = (NfSubRelationalModel.query.filter(
            NfSubRelationalModel.subscription_name == 'xtraPM-All-gNB-R2B-fail1'))
        self.assertIsNotNone(saved_nf_sub_rel)

    @patch('mod.api.services.subscription_service.save_nf_filter', MagicMock(return_value=None))
    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_create_subscription_service_on_aai_failed(self, mock_filter_call, mock_aai):
        mock_aai.side_effect = InvalidDataException(["AAI call failed"])
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-fail', 'msrmt_grp_name-fail')
        subscription = json.loads(subscription)['subscription']
        mock_filter_call.return_value = NetworkFunctionFilter(**subscription["nfFilter"])
        try:
            subscription_service.create_subscription(subscription)
        except InvalidDataException as exception:
            self.assertEqual(exception.invalid_message, ["AAI call failed"])

        # Checking Rollback on AAI failure with subscription request saved
        existing_subscription = (SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == 'xtraPM-All-gNB-R2B-fail').one_or_none())
        self.assertIsNotNone(existing_subscription)

    def test_perform_validation_existing_sub(self):
        try:
            subscription_service.create_subscription(json.loads(self.subscription_request)
                                                     ['subscription'])
        except DuplicateDataException as exception:
            self.assertEqual(exception.duplicate_field_info,
                             "subscription Name: ExtraPM-All-gNB-R2B already exists.")

    def test_missing_measurement_grp_name(self):
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-fail', '')
        try:
            subscription_service.create_subscription(json.loads(subscription)['subscription'])
        except InvalidDataException as exception:
            self.assertEqual(exception.invalid_message,
                             "No value provided for measurement group name")

    def test_missing_administrative_state(self):
        subscription = json.loads(self.create_test_subs('sub-fail', 'measurement_grp_name-fail'))
        mes_grp = subscription['subscription']['measurementGroups'][0]['measurementGroup']
        mes_grp['administrativeState'] = ''
        try:
            subscription_service.create_subscription(subscription['subscription'])
        except InvalidDataException as exception:
            self.assertEqual(exception.invalid_message,
                             "No value provided for administrative state")

    @patch.object(subscription_service, 'save_nf_filter')
    def test_save_subscription_request(self, mock_save_filter):
        mock_save_filter.return_value = None
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-new1', 'msrmt_grp_name-new1')
        subscription_service.save_subscription_request(json.loads(subscription)['subscription'])
        existing_subscription = (SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == 'xtraPM-All-gNB-R2B-new1').one_or_none())
        self.assertIsNotNone(existing_subscription)
        self.assertTrue(mock_save_filter.called)
        existing_measurement_grp = (MeasurementGroupModel.query.filter(
            MeasurementGroupModel.measurement_group_name == 'msrmt_grp_name-new1',
            MeasurementGroupModel.subscription_name == 'xtraPM-All-gNB-R2B-new1').one_or_none())
        self.assertIsNotNone(existing_measurement_grp)

    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(measurement_group_service, 'apply_nf_to_measgroup')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_apply_measurement_grp_to_nfs(self, mock_filter_call, mock_apply_nf,
                                          mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        mock_apply_nf.return_value = None
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-new2', 'msrmt_grp_name-new2')
        subscription = json.loads(subscription)['subscription']
        measurement_grp = MeasurementGroupModel('subscription_name_1',
                                                'msrmt_grp_name', 'UNLOCKED',
                                                15, 'pm.xml', [], [])
        measurement2 = self.create_measurement_grp(measurement_grp, 'meas2', 'UNLOCKED')
        measurement3 = self.create_measurement_grp(measurement_grp, 'meas3', 'LOCKED')
        measurement_grps = [measurement_grp, measurement2, measurement3]
        mock_filter_call.return_value = NetworkFunctionFilter(**subscription["nfFilter"])
        filtered_nfs = nf_service.capture_filtered_nfs(subscription["subscriptionName"])
        subscription_service.apply_measurement_grp_to_nfs(filtered_nfs, measurement_grps)
        # 2 measurement group with 2 nfs each contribute 4 calls
        self.assertEqual(mock_apply_nf.call_count, 4)

    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(AppConfig, 'publish_to_topic')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_publish_measurement_grp_to_nfs(self, mock_filter_call, mock_publish,
                                            mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        mock_publish.return_value = None
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-new2', 'msrmt_grp_name-new2')
        subscription = json.loads(subscription)['subscription']
        measurement_grp = MeasurementGroupModel('subscription_name_1',
                                                'msrmt_grp_name', 'UNLOCKED',
                                                15, 'pm.xml', [], [])
        measurement2 = self.create_measurement_grp(measurement_grp, 'meas2', 'UNLOCKED')
        measurement3 = self.create_measurement_grp(measurement_grp, 'meas3', 'UNLOCKED')
        measurement_grps = [measurement_grp, measurement2, measurement3]
        mock_filter_call.return_value = NetworkFunctionFilter(**subscription["nfFilter"])
        filtered_nfs = nf_service.capture_filtered_nfs(subscription["subscriptionName"])
        subscription_service.publish_measurement_grp_to_nfs(
            subscription["subscriptionName"], filtered_nfs, measurement_grps)
        # Two unlocked measurement Group published
        self.assertEqual(mock_publish.call_count, 6)

    patch.object(aai_client, 'get_aai_model_data')

    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(AppConfig, 'publish_to_topic')
    @patch('mod.logger.error')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_publish_measurement_grp_to_nfs_failed(self, mock_filter_call, mock_logger,
                                                   mock_publish, mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        mock_publish.side_effect = Exception('Publish failed')
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-new2', 'msrmt_grp_name-new2')
        subscription = json.loads(subscription)['subscription']
        measurement_grp = MeasurementGroupModel('subscription_name_1',
                                                'msrmt_grp_name', 'UNLOCKED',
                                                15, 'pm.xml', [], [])
        measurement2 = self.create_measurement_grp(measurement_grp, 'meas2', 'UNLOCKED')
        measurement3 = self.create_measurement_grp(measurement_grp, 'meas3', 'LOCKED')
        measurement_grps = [measurement_grp, measurement2, measurement3]
        mock_filter_call.return_value = NetworkFunctionFilter(**subscription["nfFilter"])
        filtered_nfs = nf_service.capture_filtered_nfs(subscription["subscriptionName"])
        subscription_service.publish_measurement_grp_to_nfs(
            subscription["subscriptionName"], filtered_nfs, measurement_grps)
        mock_logger.assert_called_with('Publish event failed for nf name, measure_grp_name, '
                                       'sub_name: pnf_33_ericsson,meas2, xtraPM-All-gNB-R2B-new2 '
                                       'with error: Publish failed')

    @patch('mod.api.services.subscription_service.save_nf_filter', MagicMock(return_value=None))
    @patch('mod.pmsh_config.AppConfig.publish_to_topic', MagicMock(return_value=None))
    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    @patch('mod.logger.error')
    def test_create_subscription_all_locked_msg_grp(self, mock_logger, mock_filter_call,
                                                    mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-new2', 'msrmt_grp_name-new2')
        subscription = subscription.replace('UNLOCKED', 'LOCKED')
        subscription = json.loads(subscription)['subscription']
        mock_filter_call.return_value = NetworkFunctionFilter(**subscription["nfFilter"])
        subscription_service.create_subscription(subscription)
        mock_logger.assert_called_with('All measurement groups are locked for subscription: '
                                       'xtraPM-All-gNB-R2B-new2, please verify/check'
                                       ' measurement groups.')

    def create_measurement_grp(self, measurement, measurement_name, admin_status):
        new_measurement = copy.deepcopy(measurement)
        measurement.measurement_group_name = measurement_name
        new_measurement.administrative_state = admin_status
        return new_measurement

    @patch('mod.api.services.subscription_service.save_nf_filter', MagicMock(return_value=None))
    @patch('mod.pmsh_config.AppConfig.publish_to_topic', MagicMock(return_value=None))
    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_save_filtered_nfs(self, mock_filter_call, mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-new', 'msrmt_grp_name-new')
        subscription = json.loads(subscription)['subscription']
        mock_filter_call.return_value = NetworkFunctionFilter(**subscription["nfFilter"])
        filtered_nfs = nf_service.capture_filtered_nfs(subscription["subscriptionName"])
        subscription_service.save_filtered_nfs(filtered_nfs)

        for nf in filtered_nfs:
            saved_nf = (NetworkFunctionModel.query.filter(
                NetworkFunctionModel.nf_name == nf.nf_name).one_or_none())
            self.assertIsNotNone(saved_nf)

    @patch('mod.api.services.subscription_service.save_nf_filter', MagicMock(return_value=None))
    @patch('mod.pmsh_config.AppConfig.publish_to_topic', MagicMock(return_value=None))
    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_apply_subscription_to_nfs(self, mock_filter_call, mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        subscription = json.loads(self.subscription_request)['subscription']
        mock_filter_call.return_value = NetworkFunctionFilter(**subscription["nfFilter"])
        filtered_nfs = nf_service.capture_filtered_nfs(subscription["subscriptionName"])
        subscription_service.apply_subscription_to_nfs(filtered_nfs, 'xtraPM-All-gNB-R2B')

        for nf in filtered_nfs:
            saved_nf_sub_rel = (NfSubRelationalModel.query.filter(
                NfSubRelationalModel.subscription_name == 'xtraPM-All-gNB-R2B',
                NfSubRelationalModel.nf_name == nf.nf_name).one_or_none())
            self.assertIsNotNone(saved_nf_sub_rel)

    def test_check_missing_data_sub_name_missing(self):
        subscription = self.create_test_subs('', 'msrmt_grp_name-new')
        subscription = json.loads(subscription)['subscription']
        try:
            subscription_service.check_missing_data(subscription)
        except InvalidDataException as invalidEx:
            self.assertEqual(invalidEx.invalid_message, "No value provided in subscription name")

    def test_check_missing_data_admin_status_missing(self):
        subscription = self.subscription_request.replace(
            'UNLOCKED', '')
        subscription = json.loads(subscription)['subscription']
        try:
            subscription_service.check_missing_data(subscription)
        except InvalidDataException as invalidEx:
            self.assertEqual(invalidEx.invalid_message,
                             "No value provided for administrative state")

    def test_check_missing_data_msr_grp_name(self):
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-new', '')
        subscription = json.loads(subscription)['subscription']
        try:
            subscription_service.check_missing_data(subscription)
        except InvalidDataException as invalidEx:
            self.assertEqual(invalidEx.invalid_message,
                             "No value provided for measurement group name")

    def test_validate_nf_filter_with_no_filter_values(self):
        nfFilter = '{"nfNames": [],"modelInvariantIDs": [], ' \
                   '"modelVersionIDs": [],"modelNames": []}'
        try:
            subscription_service.validate_nf_filter(json.loads(nfFilter))
        except InvalidDataException as invalidEx:
            self.assertEqual(invalidEx.invalid_message,
                             "At least one filter within nfFilter must not be empty")
