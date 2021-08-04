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
import copy
import json
import os
from unittest.mock import patch, MagicMock
from flask import current_app
from mod.api.db_models import SubscriptionModel, MeasurementGroupModel, \
    NfMeasureGroupRelationalModel
from mod.subscription import SubNfState
from mod import aai_client
from mod.api.custom_exception import InvalidDataException
from mod.pmsh_utils import _MrPub
from tests.base_setup import BaseClassSetup
from mod.api.services import subscription_service, nf_service, measurement_group_service


class SubscriptionServiceTestCase(BaseClassSetup):
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

    @patch('mod.api.services.nf_service.save_nf_filter', MagicMock(return_value=None))
    @patch('mod.pmsh_utils._MrPub.publish_to_topic', MagicMock(return_value=None))
    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    def test_create_subscription(self, mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-new', 'msrmt_grp_name-new')
        subscription_service.create_subscription(json.loads(subscription)['subscription'])
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

    @patch('mod.api.services.nf_service.save_nf_filter', MagicMock(return_value=None))
    @patch.object(aai_client, '_get_all_aai_nf_data')
    def test_create_subscription_service_failed_rollback(self, mock_aai):
        mock_aai.side_effect = InvalidDataException("AAI call failed")
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-fail', 'msrmt_grp_name-fail')
        try:
            subscription_service.create_subscription(json.loads(subscription)['subscription'])
        except InvalidDataException as exception:
            self.assertEqual(exception.invalidMessages, "AAI call failed")

        # Checking Rollback
        existing_subscription = (SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == 'xtraPM-All-gNB-R2B-fail').one_or_none())
        self.assertIsNone(existing_subscription)

    def test_perform_validation_existing_sub(self):
        try:
            subscription_service.create_subscription(json.loads(self.subscription_request)
                                                     ['subscription'])
        except InvalidDataException as exception:
            self.assertEqual(exception.invalidMessages[0],
                             "subscription Name: ExtraPM-All-gNB-R2B already exists.")

    @patch.object(nf_service, 'save_nf_filter')
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

    @patch.object(nf_service, 'save_nf_filter')
    def test_save_subscription_request_no_measure_grp(self, mock_save_filter):
        mock_save_filter.return_value = None
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-new1', 'msrmt_grp_name-new1')
        subscription = json.loads(subscription)['subscription']
        del subscription['measurementGroups']
        subscription_service.save_subscription_request(subscription)
        existing_subscription = (SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == 'xtraPM-All-gNB-R2B-new1').one_or_none())
        self.assertIsNotNone(existing_subscription)
        self.assertTrue(mock_save_filter.called)
        existing_measurement_grp = (MeasurementGroupModel.query.filter(
            MeasurementGroupModel.measurement_group_name == 'msrmt_grp_name-new1',
            MeasurementGroupModel.subscription_name == 'xtraPM-All-gNB-R2B-new1').one_or_none())
        self.assertIsNone(existing_measurement_grp)

    @patch.object(measurement_group_service, 'apply_nf')
    @patch.object(_MrPub, 'publish_to_topic')
    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    def test_apply_measurement_grp_to_nfs(self, mock_model_aai, mock_aai,
                                          mock_publish, mock_apply_nf):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        mock_publish.return_value = None
        mock_apply_nf.return_value = None
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-new2', 'msrmt_grp_name-new2')
        subscription = json.loads(subscription)['subscription']
        measurement1 = subscription['measurementGroups'][0]
        measurement2 = self.create_measurement_grp(measurement1, 'meas2', 'UNLOCKED')
        measurement3 = self.create_measurement_grp(measurement1, 'meas3', 'LOCKED')
        subscription['measurementGroups'].extend([measurement2, measurement3])
        filtered_nfs = nf_service.capture_filtered_nfs(subscription["nfFilter"])
        subscription_service.apply_measurement_grp_to_nfs(
            subscription["subscriptionName"], filtered_nfs, subscription.get('measurementGroups'))
        # Two unlocked measurement Group published
        self.assertEqual(mock_publish.call_count, 2)
        # 2 measurement group with 2 nfs each contribute 4 calls
        self.assertEqual(mock_apply_nf.call_count, 4)

    def create_measurement_grp(self, measurement, measurement_name, admin_status):
        new_measurement = copy.deepcopy(measurement)
        new_measurement['measurementGroup']['measurementGroupName'] = measurement_name
        new_measurement['measurementGroup']['administrativeState'] = admin_status
        return new_measurement
