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
from unittest.mock import patch, Mock, MagicMock

from mod.api.db_models import NetworkFunctionModel
from mod.network_function import NetworkFunction
from mod.subscription import AdministrativeState
from mod.subscription_handler import SubscriptionHandler
from tests.base_setup import BaseClassSetup, get_pmsh_config


class SubscriptionHandlerTest(BaseClassSetup):
    nfs = [
        NetworkFunction(nf_name='pnf_1', model_invariant_id='some-id', model_version_id='some-id'),
        NetworkFunction(nf_name='pnf_2', model_invariant_id='some-id', model_version_id='some-id')]

    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    @patch('mod.pmsh_utils._MrSub')
    @patch('mod.pmsh_utils._MrPub')
    def setUp(self, mock_mr_pub, mock_mr_sub):
        super().setUp()
        self.mock_mr_pub = mock_mr_pub
        self.mock_mr_sub = mock_mr_sub
        self.mock_policy_event_thread = Mock()

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

    @patch('mod.pmsh_utils.AppConfig.refresh_config', MagicMock(return_value=get_pmsh_config()))
    @patch('mod.subscription.Subscription.get_local_sub_admin_state')
    @patch('mod.logger.info')
    @patch('mod.aai_client.get_pmsh_nfs_from_aai')
    def test_execute_no_change_of_state(self, mock_get_aai, mock_logger, mock_get_sub_status):
        mock_get_sub_status.return_value = AdministrativeState.UNLOCKED.value
        mock_get_aai.return_value = self.nfs
        sub_handler = SubscriptionHandler(self.mock_mr_pub,
                                          self.mock_mr_sub, self.app, self.app_conf)
        sub_handler.execute()
        mock_logger.assert_called_with('Administrative State did not change '
                                       'in the app config: UNLOCKED')

    @patch('mod.subscription_handler.SubscriptionHandler._start_aai_event_thread',
           MagicMock())
    @patch('mod.pmsh_utils.AppConfig.refresh_config', MagicMock(return_value=get_pmsh_config()))
    @patch('mod.subscription.Subscription.get_local_sub_admin_state')
    @patch('mod.subscription.Subscription.create_subscription_on_nfs')
    @patch('mod.aai_client.get_pmsh_nfs_from_aai')
    def test_execute_change_of_state_to_unlocked(self, mock_get_aai, mock_activate_sub,
                                                 mock_get_sub_status):
        mock_get_aai.return_value = self.nfs
        mock_get_sub_status.return_value = AdministrativeState.LOCKED.value
        sub_handler = SubscriptionHandler(self.mock_mr_pub, self.mock_mr_sub, self.app,
                                          self.app_conf)
        sub_handler.execute()
        self.assertEqual(AdministrativeState.UNLOCKED.value,
                         self.app_conf.subscription.administrativeState)
        mock_activate_sub.assert_called_with(self.nfs, self.mock_mr_pub, self.app_conf)

    @patch('mod.subscription.Subscription.get_network_functions', MagicMock(return_value=nfs))
    @patch('mod.pmsh_utils.AppConfig.refresh_config', MagicMock(return_value=get_pmsh_config()))
    @patch('mod.subscription.Subscription.get_local_sub_admin_state')
    @patch('mod.subscription.Subscription.delete_subscription_from_nfs')
    def test_execute_change_of_state_to_locked(self, mock_deactivate_sub, mock_get_sub_status):
        mock_get_sub_status.return_value = AdministrativeState.UNLOCKED.value
        self.app_conf.subscription.administrativeState = AdministrativeState.LOCKED.value
        self.app_conf.subscription.update_subscription_status()
        sub_handler = SubscriptionHandler(self.mock_mr_pub, self.mock_mr_sub, self.app,
                                          self.app_conf)
        sub_handler.execute()
        mock_deactivate_sub.assert_called_with(self.nfs, self.mock_mr_pub, self.app_conf)

    @patch('mod.subscription_handler.SubscriptionHandler._start_aai_event_thread', MagicMock())
    @patch('mod.pmsh_utils.AppConfig.refresh_config', MagicMock(return_value=get_pmsh_config()))
    @patch('mod.subscription.Subscription.create_subscription_on_nfs')
    @patch('mod.logger.error')
    @patch('mod.aai_client.get_pmsh_nfs_from_aai')
    def test_execute_exception(self, mock_get_aai, mock_logger, mock_activate_sub):
        mock_get_aai.return_value = self.nfs
        mock_activate_sub.side_effect = Exception
        sub_handler = SubscriptionHandler(self.mock_mr_pub, self.mock_mr_sub, self.app,
                                          self.app_conf)
        sub_handler.execute()
        mock_logger.assert_called_with('Error occurred during the activation/deactivation process ',
                                       exc_info=True)

    @patch('mod.network_function.NetworkFunction.get',
           MagicMock(return_value=NetworkFunctionModel(nf_name='pnf_1',
                                                       model_invariant_id='some-id',
                                                       model_version_id='some-id',
                                                       ip_address='ip_address',
                                                       model_name='model_name',
                                                       sdnc_model_name='sdnc_model_name',
                                                       sdnc_model_version='sdnc_model_version')))
    @patch('mod.subscription.Subscription.get_delete_failed_nfs', MagicMock(return_value=nfs))
    @patch('mod.subscription.Subscription.get_network_functions', MagicMock(return_value=nfs))
    @patch('mod.pmsh_utils.AppConfig.refresh_config', MagicMock(return_value=get_pmsh_config()))
    @patch('mod.subscription.Subscription.get_local_sub_admin_state')
    @patch('mod.subscription.Subscription.delete_subscription_from_nfs')
    @patch('mod.network_function.NetworkFunction.increment_retry_count')
    def test_execute_change_of_state_to_locking_retry_delete(self, mock_retry_inc, mock_delete_sub,
                                                             mock_get_sub_status):
        mock_get_sub_status.return_value = AdministrativeState.LOCKING.value
        sub_handler = SubscriptionHandler(self.mock_mr_pub, self.mock_mr_sub, self.app,
                                          self.app_conf)
        sub_handler.execute()
        self.assertEqual(mock_delete_sub.call_count, 2)
        self.assertEqual(mock_retry_inc.call_count, 2)

    @patch('mod.subscription.Subscription.get_delete_failed_nfs', MagicMock(return_value=[]))
    @patch('mod.subscription.Subscription.get_network_functions', MagicMock(return_value=nfs))
    @patch('mod.pmsh_utils.AppConfig.refresh_config', MagicMock(return_value=get_pmsh_config()))
    @patch('mod.subscription.Subscription.get_local_sub_admin_state')
    @patch('mod.subscription.Subscription.update_subscription_status')
    def test_execute_change_of_state_to_locking_success(self, mock_update_sub,
                                                        mock_get_sub_status):
        mock_get_sub_status.return_value = AdministrativeState.LOCKING.value
        sub_handler = SubscriptionHandler(self.mock_mr_pub, self.mock_mr_sub, self.app,
                                          self.app_conf)
        sub_handler.execute()
        mock_update_sub.assert_called_once()

    @patch('mod.network_function.NetworkFunction.get',
           MagicMock(return_value=NetworkFunctionModel(nf_name='pnf_1',
                                                       model_invariant_id='some-id',
                                                       model_version_id='some-id',
                                                       ip_address='ip_address',
                                                       model_name='model_name',
                                                       sdnc_model_name='sdnc_model_name',
                                                       sdnc_model_version='sdnc_model_version',
                                                       retry_count=3)))
    @patch('mod.subscription.Subscription.get_delete_failed_nfs', MagicMock(return_value=[nfs[0]]))
    @patch('mod.subscription.Subscription.get_network_functions', MagicMock(return_value=nfs[0]))
    @patch('mod.pmsh_utils.AppConfig.refresh_config', MagicMock(return_value=get_pmsh_config()))
    @patch('mod.subscription.Subscription.get_local_sub_admin_state')
    @patch('mod.network_function.NetworkFunction.delete')
    def test_execute_change_of_state_to_locking_retry_failed(self, mock_nf_del,
                                                             mock_get_sub_status):
        mock_get_sub_status.return_value = AdministrativeState.LOCKING.value
        sub_handler = SubscriptionHandler(self.mock_mr_pub, self.mock_mr_sub, self.app,
                                          self.app_conf)
        sub_handler.execute()
        mock_nf_del.assert_called_once()
