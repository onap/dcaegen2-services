# ============LICENSE_START===================================================
#  Copyright (C) 2019-2022 Nordix Foundation.
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
from unittest.mock import patch, MagicMock

from mod import db
from mod.api.services.measurement_group_service import MgNfState, AdministrativeState
from mod.api.services import measurement_group_service
from mod.network_function import NetworkFunction
from mod.policy_response_handler import PolicyResponseHandler, policy_response_handle_functions
from tests.base_setup import BaseClassSetup, create_subscription_data


class PolicyResponseHandlerTest(BaseClassSetup):

    @patch('mod.create_app')
    def setUp(self, mock_app):
        super().setUp()
        self.nf = NetworkFunction(nf_name='nf1')
        self.policy_response_handler = PolicyResponseHandler(mock_app)

    @patch('mod.network_function.NetworkFunction.delete')
    def test_handle_response_locked_success(self, mock_delete):
        with patch.dict(policy_response_handle_functions,
                        {AdministrativeState.LOCKED.value: {'success': mock_delete}}):
            self.policy_response_handler._handle_response(
                'msr_grp_name',
                AdministrativeState.LOCKED.value,
                self.nf.nf_name, 'success')
            mock_delete.assert_called()

    @patch('mod.api.services.measurement_group_service.update_measurement_group_nf_status')
    def test_handle_response_locked_failed(self, mock_update_sub_nf):
        with patch.dict(policy_response_handle_functions,
                        {AdministrativeState.LOCKED.value: {'failed': mock_update_sub_nf}}):
            self.policy_response_handler._handle_response(
                'msr_grp_name',
                AdministrativeState.LOCKED.value,
                self.nf.nf_name, 'failed')
            mock_update_sub_nf.assert_called_with(
                measurement_group_name='msr_grp_name',
                status=MgNfState.DELETE_FAILED.value, nf_name=self.nf.nf_name)

    @patch('mod.network_function.NetworkFunction.delete')
    def test_handle_response_locking_success(self, mock_delete):
        with patch.dict(policy_response_handle_functions,
                        {AdministrativeState.LOCKING.value: {'success': mock_delete}}):
            self.policy_response_handler._handle_response(
                'msr_grp_name',
                AdministrativeState.LOCKING.value,
                self.nf.nf_name, 'success')
            mock_delete.assert_called()

    @patch('mod.api.services.measurement_group_service.update_measurement_group_nf_status')
    def test_handle_response_locking_failed(self, mock_update_sub_nf):
        with patch.dict(policy_response_handle_functions,
                        {AdministrativeState.LOCKING.value: {'failed': mock_update_sub_nf}}):
            self.policy_response_handler._handle_response(
                'msr_grp_name',
                AdministrativeState.LOCKING.value,
                self.nf.nf_name, 'failed')
            mock_update_sub_nf.assert_called_with(
                measurement_group_name='msr_grp_name',
                status=MgNfState.DELETE_FAILED.value, nf_name=self.nf.nf_name)

    @patch('mod.api.services.measurement_group_service.update_measurement_group_nf_status')
    def test_handle_response_unlocked_success(self, mock_update_sub_nf):
        with patch.dict(policy_response_handle_functions,
                        {AdministrativeState.UNLOCKED.value: {'success': mock_update_sub_nf}}):
            self.policy_response_handler._handle_response(
                'msr_grp_name',
                AdministrativeState.UNLOCKED.value,
                self.nf.nf_name, 'success')
            mock_update_sub_nf.assert_called_with(
                measurement_group_name='msr_grp_name',
                status=MgNfState.CREATED.value, nf_name=self.nf.nf_name)

    @patch('mod.api.services.measurement_group_service.update_measurement_group_nf_status')
    def test_handle_response_unlocked_success_filtering(self, mock_update_sub_nf):
        with patch.dict(policy_response_handle_functions,
                        {AdministrativeState.UNLOCKED.value: {'success': mock_update_sub_nf}}):
            sub = create_subscription_data('sub')
            db.session.add(sub)
            measurement_group_service.apply_nf_status_to_measurement_group(
                self.nf.nf_name, "MG2", MgNfState.PENDING_CREATE.value)
            db.session.commit()
            self.policy_response_handler._handle_response(
                'MG2',
                AdministrativeState.FILTERING.value,
                self.nf.nf_name, 'success')
            mock_update_sub_nf.assert_called_with(
                measurement_group_name='MG2',
                status=MgNfState.CREATED.value, nf_name=self.nf.nf_name)

    @patch('mod.api.services.measurement_group_service.update_measurement_group_nf_status')
    def test_handle_response_locking_success_filtering(self, mock_update_sub_nf):
        with patch.dict(policy_response_handle_functions,
                        {AdministrativeState.LOCKING.value: {'success': mock_update_sub_nf}}):
            sub = create_subscription_data('sub')
            db.session.add(sub)
            measurement_group_service.apply_nf_status_to_measurement_group(
                self.nf.nf_name, "MG2", MgNfState.PENDING_DELETE.value)
            db.session.commit()
            self.policy_response_handler._handle_response(
                'MG2',
                AdministrativeState.FILTERING.value,
                self.nf.nf_name, 'success')
            mock_update_sub_nf.assert_called_with(
                measurement_group_name='MG2',
                status=MgNfState.DELETED.value, nf_name=self.nf.nf_name)

    @patch('mod.api.services.measurement_group_service.update_measurement_group_nf_status')
    def test_handle_response_unlocked_failed(self, mock_update_sub_nf):
        with patch.dict(policy_response_handle_functions,
                        {AdministrativeState.UNLOCKED.value: {'failed': mock_update_sub_nf}}):
            self.policy_response_handler._handle_response(
                'msr_grp_name',
                AdministrativeState.UNLOCKED.value,
                self.nf.nf_name, 'failed')
            mock_update_sub_nf.assert_called_with(
                measurement_group_name='msr_grp_name',
                status=MgNfState.CREATE_FAILED.value, nf_name=self.nf.nf_name)

    @patch('mod.api.services.measurement_group_service.update_measurement_group_nf_status')
    def test_handle_response_create_failed_filtering(self, mock_update_sub_nf):
        with patch.dict(policy_response_handle_functions,
                        {AdministrativeState.UNLOCKED.value: {'failed': mock_update_sub_nf}}):
            sub = create_subscription_data('sub')
            db.session.add(sub)
            measurement_group_service.apply_nf_status_to_measurement_group(
                self.nf.nf_name, "MG2", MgNfState.PENDING_CREATE.value)
            db.session.commit()
            self.policy_response_handler._handle_response(
                'MG2',
                AdministrativeState.FILTERING.value,
                self.nf.nf_name, 'failed')
            mock_update_sub_nf.assert_called_with(
                measurement_group_name='MG2',
                status=MgNfState.CREATE_FAILED.value, nf_name=self.nf.nf_name)

    @patch('mod.api.services.measurement_group_service.update_measurement_group_nf_status')
    def test_handle_response_delete_failed_filtering(self, mock_update_sub_nf):
        with patch.dict(policy_response_handle_functions,
                        {AdministrativeState.LOCKING.value: {'failed': mock_update_sub_nf}}):
            sub = create_subscription_data('sub')
            db.session.add(sub)
            measurement_group_service.apply_nf_status_to_measurement_group(
                self.nf.nf_name, "MG2", MgNfState.PENDING_DELETE.value)
            db.session.commit()
            self.policy_response_handler._handle_response(
                'MG2',
                AdministrativeState.FILTERING.value,
                self.nf.nf_name, 'failed')
            mock_update_sub_nf.assert_called_with(
                measurement_group_name='MG2',
                status=MgNfState.DELETE_FAILED.value, nf_name=self.nf.nf_name)

    def test_handle_response_exception(self):
        self.assertRaises(Exception, self.policy_response_handler._handle_response, 'sub1',
                          'wrong_state', 'nf1', 'wrong_message')

    @patch('mod.policy_response_handler.PolicyResponseHandler._handle_response')
    @patch('mod.pmsh_config.AppConfig.get_from_topic')
    def test_poll_policy_topic_calls_methods_correct_mg(self, mock_policy_mr_sub,
                                                        mock_handle_response):
        response_data = ['{"name": "ResponseEvent","status": { "subscriptionName": '
                         '"ExtraPM-All-gNB-R2B2", "nfName": "pnf300", "message": "success", '
                         '"measurementGroupName":"MG1"} }']
        mock_policy_mr_sub.return_value = response_data
        sub_model = create_subscription_data('ExtraPM-All-gNB-R2B2')
        db.session.add(sub_model)
        db.session.commit()
        self.policy_response_handler.poll_policy_topic()
        mock_handle_response.assert_called_with("MG1",
                                                AdministrativeState.UNLOCKED.value, 'pnf300',
                                                'success')

    @patch('mod.policy_response_handler.PolicyResponseHandler._handle_response')
    @patch('mod.pmsh_config.AppConfig.get_from_topic')
    def test_poll_policy_topic_no_method_calls_unavailable_mg(self, mock_policy_mr_sub,
                                                              mock_handle_response):
        response_data = ['{"name": "ResponseEvent","status": { "subscriptionName": '
                         '"Different_Subscription", "nfName": "pnf300", "message": "success",'
                         '"measurementGroupName":"msr_grp_name" } }']
        mock_policy_mr_sub.return_value = response_data
        self.policy_response_handler.poll_policy_topic()
        mock_handle_response.assert_not_called()

    @patch('mod.pmsh_config.AppConfig.get_from_topic', MagicMock(return_value='wrong_return'))
    @patch('mod.logger.error')
    def test_poll_policy_topic_exception(self, mock_logger):
        self.policy_response_handler.poll_policy_topic()
        mock_logger.assert_called()
