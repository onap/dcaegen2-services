# ============LICENSE_START===================================================
#  Copyright (C) 2019-2021 Nordix Foundation.
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
from unittest.mock import patch, MagicMock
import responses
from requests import Session
from mod import aai_client
from mod.api.controller import status, get_all_sub_to_nf_relations, post_subscription
from tests.base_setup import BaseClassSetup
from mod.api.db_models import SubscriptionModel, NfMeasureGroupRelationalModel
from mod.subscription import SubNfState
from mod.network_function import NetworkFunctionFilter


class ControllerTestCase(BaseClassSetup):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    def setUp(self):
        super().setUp()
        super().setUpAppConf()
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_xnfs.json'), 'r') as data:
            self.aai_response_data = data.read()
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_model_info.json'), 'r') as data:
            self.good_model_info = data.read()
        with open(os.path.join(os.path.dirname(__file__),
                               'data/create_subscription_request.json'), 'r') as data:
            self.subscription_request = data.read()

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
        self.xnfs = aai_client.get_pmsh_nfs_from_aai(self.app_conf, self.app_conf.nf_filter)
        sub_model = self.app_conf.subscription.get()
        for nf in self.xnfs:
            self.app_conf.subscription.add_network_function_to_subscription(nf, sub_model)
        all_subs = get_all_sub_to_nf_relations()
        self.assertEqual(len(all_subs[0]['network_functions']), 3)
        self.assertEqual(all_subs[0]['subscription_name'], 'ExtraPM-All-gNB-R2B')

    def create_test_subs(self, new_sub_name, new_msrmt_grp_name):
        subscription = self.subscription_request.replace('ExtraPM-All-gNB-R2B', new_sub_name)
        subscription = subscription.replace('msrmt_grp_name', new_msrmt_grp_name)
        return subscription

    @patch('mod.api.services.subscription_service.save_nf_filter', MagicMock(return_value=None))
    @patch('mod.pmsh_config.AppConfig.publish_to_topic', MagicMock(return_value=None))
    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_post_subscription(self, mock_filter_call, mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-post', 'msrmt_grp_name-post')
        subscription = json.loads(subscription)
        mock_filter_call.return_value = NetworkFunctionFilter(
            **subscription['subscription']["nfFilter"])
        sub_name = subscription['subscription']['subscriptionName']
        mes_grp = subscription['subscription']['measurementGroups'][0]['measurementGroup']
        mes_grp_name = mes_grp['measurementGroupName']
        response = post_subscription(subscription)
        subscription = (SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == sub_name).one_or_none())
        self.assertIsNotNone(subscription)
        msr_grp_nf_rel = (NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == mes_grp_name)).all()
        for published_event in msr_grp_nf_rel:
            self.assertEqual(published_event.nf_measure_grp_status,
                             SubNfState.PENDING_CREATE.value)
        self.assertEqual(response[1], 201)

    def test_post_subscription_duplicate_sub(self):
        # Posting the same subscription request stored in previous test to get duplicate response
        response = post_subscription(json.loads(self.subscription_request))
        self.assertEqual(response[1], 409)
        self.assertEqual(response[0], 'subscription Name: ExtraPM-All-gNB-R2B already exists.')

    def test_post_subscription_invalid_filter(self):
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-invalid', 'msrmt_grp_name-invalid')
        subscription = json.loads(subscription)
        subscription['subscription']['nfFilter']['nfNames'] = []
        subscription['subscription']['nfFilter']['modelInvariantIDs'] = []
        subscription['subscription']['nfFilter']['modelVersionIDs'] = []
        subscription['subscription']['nfFilter']['modelNames'] = []
        response = post_subscription(subscription)
        self.assertEqual(response[1], 400)
        self.assertEqual(response[0], 'At least one filter within nfFilter must not be empty')

    def test_post_subscription_missing(self):
        subscription = json.loads(self.subscription_request)
        subscription['subscription']['subscriptionName'] = ''
        response = post_subscription(subscription)
        self.assertEqual(response[1], 400)
        self.assertEqual(response[0], 'No value provided in subscription name')
