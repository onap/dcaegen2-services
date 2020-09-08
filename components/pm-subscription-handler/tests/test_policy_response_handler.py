# ============LICENSE_START===================================================
#  Copyright (C) 2019-2020 Nordix Foundation.
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
from unittest.mock import patch

from mod.api.db_models import SubscriptionModel
from mod.network_function import NetworkFunction
from mod.policy_response_handler import PolicyResponseHandler, policy_response_handle_functions
from mod.subscription import AdministrativeState, SubNfState
from tests.base_setup import BaseClassSetup


class PolicyResponseHandlerTest(BaseClassSetup):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    @patch('mod.create_app')
    @patch('mod.pmsh_utils._MrSub')
    def setUp(self, mock_mr_sub, mock_app):
        super().setUp()
        self.mock_policy_mr_sub = mock_mr_sub
        self.nf = NetworkFunction(nf_name='nf1')
        self.policy_response_handler = PolicyResponseHandler(self.mock_policy_mr_sub,
                                                             self.app_conf,
                                                             mock_app)

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

    @patch('mod.network_function.NetworkFunction.delete')
    def test_handle_response_locked_success(self, mock_delete):
        with patch.dict(policy_response_handle_functions,
                        {AdministrativeState.LOCKED.value: {'success': mock_delete}}):
            self.policy_response_handler._handle_response(
                self.app_conf.subscription.subscriptionName,
                AdministrativeState.LOCKED.value,
                self.nf.nf_name, 'success')

            mock_delete.assert_called()

    @patch('mod.subscription.Subscription.update_sub_nf_status')
    def test_handle_response_locked_failed(self, mock_update_sub_nf):
        with patch.dict(policy_response_handle_functions,
                        {AdministrativeState.LOCKED.value: {'failed': mock_update_sub_nf}}):
            self.policy_response_handler._handle_response(
                self.app_conf.subscription.subscriptionName,
                AdministrativeState.LOCKED.value,
                self.nf.nf_name, 'failed')
            mock_update_sub_nf.assert_called_with(
                subscription_name=self.app_conf.subscription.subscriptionName,
                status=SubNfState.DELETE_FAILED.value, nf_name=self.nf.nf_name)

    @patch('mod.subscription.Subscription.update_sub_nf_status')
    def test_handle_response_unlocked_success(self, mock_update_sub_nf):
        with patch.dict(policy_response_handle_functions,
                        {AdministrativeState.UNLOCKED.value: {'success': mock_update_sub_nf}}):
            self.policy_response_handler._handle_response(
                self.app_conf.subscription.subscriptionName,
                AdministrativeState.UNLOCKED.value,
                self.nf.nf_name, 'success')
            mock_update_sub_nf.assert_called_with(
                subscription_name=self.app_conf.subscription.subscriptionName,
                status=SubNfState.CREATED.value, nf_name=self.nf.nf_name)

    @patch('mod.subscription.Subscription.update_sub_nf_status')
    def test_handle_response_unlocked_failed(self, mock_update_sub_nf):
        with patch.dict(policy_response_handle_functions,
                        {AdministrativeState.UNLOCKED.value: {'failed': mock_update_sub_nf}}):
            self.policy_response_handler._handle_response(
                self.app_conf.subscription.subscriptionName,
                AdministrativeState.UNLOCKED.value,
                self.nf.nf_name, 'failed')
            mock_update_sub_nf.assert_called_with(
                subscription_name=self.app_conf.subscription.subscriptionName,
                status=SubNfState.CREATE_FAILED.value, nf_name=self.nf.nf_name)

    def test_handle_response_exception(self):
        self.assertRaises(Exception, self.policy_response_handler._handle_response, 'sub1',
                          'wrong_state', 'nf1', 'wrong_message')

    @patch('mod.policy_response_handler.PolicyResponseHandler._handle_response')
    @patch('mod.subscription.Subscription.get')
    def test_poll_policy_topic_calls_methods_correct_sub(self, mock_get_sub, mock_handle_response):
        response_data = ['{"name": "ResponseEvent","status": { "subscriptionName": '
                         '"ExtraPM-All-gNB-R2B", "nfName": "pnf300", "message": "success" } }']
        self.mock_policy_mr_sub.get_from_topic.return_value = response_data
        mock_get_sub.return_value = SubscriptionModel(subscription_name='ExtraPM-All-gNB-R2B',
                                                      status=AdministrativeState.UNLOCKED.value)
        self.policy_response_handler.poll_policy_topic()
        self.mock_policy_mr_sub.get_from_topic.assert_called()
        mock_handle_response.assert_called_with(self.app_conf.subscription.subscriptionName,
                                                AdministrativeState.UNLOCKED.value, 'pnf300',
                                                'success')

    @patch('mod.policy_response_handler.PolicyResponseHandler._handle_response')
    @patch('mod.subscription.Subscription.get')
    def test_poll_policy_topic_no_method_calls_incorrect_sub(self, mock_get_sub,
                                                             mock_handle_response):
        response_data = ['{"name": "ResponseEvent","status": { "subscriptionName": '
                         '"Different_Subscription", "nfName": "pnf300", "message": "success" } }']
        self.mock_policy_mr_sub.get_from_topic.return_value = response_data
        mock_get_sub.return_value = SubscriptionModel(subscription_name='ExtraPM-All-gNB-R2B',
                                                      status=AdministrativeState.UNLOCKED.value)
        self.policy_response_handler.poll_policy_topic()

        self.mock_policy_mr_sub.get_from_topic.assert_called()

        mock_handle_response.assert_not_called()

    @patch('mod.logger.error')
    @patch('mod.subscription.Subscription.get')
    def test_poll_policy_topic_exception(self, mock_get_sub, mock_logger):
        self.mock_policy_mr_sub.get_from_topic.return_value = 'wrong_return'
        mock_get_sub.return_value = SubscriptionModel(subscription_name='ExtraPM-All-gNB-R2B',
                                                      status=AdministrativeState.UNLOCKED.value)
        self.policy_response_handler.poll_policy_topic()
        mock_logger.assert_called()
