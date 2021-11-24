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
from http import HTTPStatus

from mod import aai_client
from mod.api.controller import status, post_subscription, get_subscription_by_name,\
    get_subscriptions
from tests.base_setup import BaseClassSetup
from mod.api.db_models import SubscriptionModel, NfMeasureGroupRelationalModel
from mod.subscription import SubNfState
from mod.network_function import NetworkFunctionFilter
from tests.base_setup import get_subscription_data, get_subscriptions_data


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

    @patch('mod.api.services.subscription_service.query_subscription_by_name',
           MagicMock(return_value=get_subscription_data('sub_demo')))
    def test_get_subscription_by_name_api(self):
        sub, status_code = get_subscription_by_name('sub_demo')
        self.assertEqual(status_code, HTTPStatus.OK)
        self.assertEqual(sub['subscription']['subscriptionName'], 'sub_demo')
        self.assertEqual(sub['subscription']['nfFilter']['nfNames'],
                         ['^pnf.*', '^vnf.*'])
        self.assertEqual(sub['subscription']['controlLoopName'],
                         'pmsh_control_loop_name')
        self.assertEqual(len(sub['subscription']['measurementGroups']), 2)
        self.assertEqual(sub['subscription']['operationalPolicyName'],
                         'pmsh_operational_policy')

    @patch('mod.api.services.subscription_service.query_subscription_by_name',
           MagicMock(return_value=None))
    def test_get_subscription_by_name_api_none(self):
        sub, status_code = get_subscription_by_name('sub_demo')
        self.assertEqual(status_code, HTTPStatus.NOT_FOUND)
        self.assertEqual(sub['error'],
                         'Subscription was not defined with the name : sub_demo')

    @patch('mod.api.services.subscription_service.query_subscription_by_name',
           MagicMock(side_effect=Exception('something failed')))
    def test_get_subscription_by_name_api_exception(self):
        sub, status_code = get_subscription_by_name('sub_demo')
        self.assertEqual(status_code, HTTPStatus.INTERNAL_SERVER_ERROR)

    @patch('mod.api.services.subscription_service.query_all_subscriptions',
           MagicMock(return_value=get_subscriptions_data(['sub_demo_one', 'sub_demo_two'])))
    def test_get_subscriptions_api(self):
        subs, status_code = get_subscriptions()
        self.assertEqual(status_code, HTTPStatus.OK)
        self.assertEqual(subs[0]['subscription']['subscriptionName'], 'sub_demo_one')
        self.assertEqual(subs[1]['subscription']['subscriptionName'], 'sub_demo_two')
        self.assertEqual(subs[1]['subscription']['measurementGroups'][0]['measurementGroup']
                         ['measurementGroupName'], 'MG1')
        self.assertEqual(len(subs[1]['subscription']['measurementGroups']), 2)
        self.assertEqual(len(subs), 2)

    @patch('mod.api.services.subscription_service.query_all_subscriptions',
           MagicMock(return_value=None))
    def test_get_subscriptions_api_none(self):
        subs, status_code = get_subscriptions()
        self.assertEqual(status_code, HTTPStatus.OK)
        self.assertEqual(subs, [])

    @patch('mod.api.services.subscription_service.query_all_subscriptions',
           MagicMock(side_effect=Exception('something failed')))
    def test_get_subscriptions_api_exception(self):
        subs, status_code = get_subscriptions()
        self.assertEqual(status_code, HTTPStatus.INTERNAL_SERVER_ERROR)
