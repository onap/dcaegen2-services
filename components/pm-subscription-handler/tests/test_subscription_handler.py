# ============LICENSE_START===================================================
#  Copyright (C) 2020 Nordix Foundation.
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
from unittest.mock import patch, Mock

from mod.network_function import NetworkFunction
from mod.subscription import AdministrativeState
from mod.subscription_handler import SubscriptionHandler
from tests.base_setup import BaseClassSetup


class SubscriptionHandlerTest(BaseClassSetup):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    @patch('mod.pmsh_utils._MrPub')
    def setUp(self, mock_mr_pub):
        super().setUp()
        self.nfs = [NetworkFunction(nf_name='pnf_1',
                                    model_invariant_id='some-id',
                                    model_version_id='some-id'),
                    NetworkFunction(nf_name='pnf_2',
                                    model_invariant_id='some-id',
                                    model_version_id='some-id')]
        self.mock_mr_pub = mock_mr_pub
        self.mock_aai_event_thread = Mock()
        self.mock_policy_event_thread = Mock()

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

    @patch('mod.subscription.Subscription.get_local_sub_admin_state')
    @patch('mod.logger.info')
    @patch('mod.aai_client.get_pmsh_nfs_from_aai')
    def test_execute_no_change_of_state(self, mock_get_aai, mock_logger, mock_get_sub_status):
        mock_get_sub_status.return_value = AdministrativeState.UNLOCKED.value
        mock_get_aai.return_value = self.nfs
        sub_handler = SubscriptionHandler(self.mock_mr_pub,
                                          self.app, self.app_conf,
                                          self.mock_aai_event_thread,
                                          self.mock_policy_event_thread)
        sub_handler.execute()
        mock_logger.assert_called_with('Administrative State did not change in the Config')

    @patch('mod.subscription.Subscription.get_local_sub_admin_state')
    @patch('mod.subscription.Subscription.activate_subscription')
    @patch('mod.aai_client.get_pmsh_nfs_from_aai')
    def test_execute_change_of_state_to_unlocked(self, mock_get_aai, mock_activate_sub,
                                                 mock_get_sub_status):
        mock_get_aai.return_value = self.nfs
        mock_get_sub_status.return_value = AdministrativeState.LOCKED.value
        self.mock_aai_event_thread.return_value.start.return_value = 'start_method'
        sub_handler = SubscriptionHandler(self.mock_mr_pub,
                                          self.app, self.app_conf,
                                          self.mock_aai_event_thread.return_value,
                                          self.mock_policy_event_thread)
        sub_handler.execute()
        self.assertEqual(AdministrativeState.UNLOCKED.value,
                         self.app_conf.subscription.administrativeState)
        mock_activate_sub.assert_called_with(self.nfs, self.mock_mr_pub, self.app_conf)
        self.mock_aai_event_thread.return_value.start.assert_called()

    @patch('mod.subscription.Subscription.get_local_sub_admin_state')
    @patch('mod.subscription.Subscription.deactivate_subscription')
    @patch('mod.aai_client.get_pmsh_nfs_from_aai')
    def test_execute_change_of_state_to_locked(self, mock_get_aai, mock_deactivate_sub,
                                               mock_get_sub_status):
        mock_get_sub_status.return_value = AdministrativeState.UNLOCKED.value
        self.app_conf.subscription.administrativeState = AdministrativeState.LOCKED.value
        self.app_conf.subscription.update_subscription_status()
        mock_get_aai.return_value = self.nfs
        self.mock_aai_event_thread.return_value.cancel.return_value = 'cancel_method'
        sub_handler = SubscriptionHandler(self.mock_mr_pub,
                                          self.app, self.app_conf,
                                          self.mock_aai_event_thread.return_value,
                                          self.mock_policy_event_thread)
        sub_handler.execute()
        mock_deactivate_sub.assert_called_with(self.mock_mr_pub, self.app_conf)
        self.mock_aai_event_thread.return_value.cancel.assert_called()

    @patch('mod.subscription.Subscription.activate_subscription')
    @patch('mod.logger.error')
    @patch('mod.aai_client.get_pmsh_nfs_from_aai')
    def test_execute_exception(self, mock_get_aai, mock_logger, mock_activate_sub):
        mock_get_aai.return_value = self.nfs
        mock_activate_sub.side_effect = Exception
        sub_handler = SubscriptionHandler(self.mock_mr_pub,
                                          self.app, self.app_conf,
                                          self.mock_aai_event_thread,
                                          self.mock_policy_event_thread)
        sub_handler.execute()
        mock_logger.assert_called_with('Error occurred during the activation/deactivation process ',
                                       exc_info=True)
