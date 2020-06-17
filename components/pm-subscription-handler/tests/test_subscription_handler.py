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
import json
import os
from unittest import TestCase
from unittest.mock import patch

from mod.network_function import NetworkFunction
from mod.pmsh_utils import AppConfig
from mod.subscription import AdministrativeState
from mod.subscription_handler import SubscriptionHandler


class SubscriptionHandlerTest(TestCase):

    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    @patch('mod.create_app')
    @patch('mod.pmsh_utils._MrPub')
    @patch('mod.pmsh_utils.PeriodicTask')
    def setUp(self, mock_aai_event_thread, mock_mr_pub, mock_app, mock_get_pmsh_config):
        with open(os.path.join(os.path.dirname(__file__), 'data/cbs_data_1.json'), 'r') as data:
            self.cbs_data = json.load(data)
        mock_get_pmsh_config.return_value = self.cbs_data
        self.app_conf = AppConfig()
        self.mock_app = mock_app
        self.mock_mr_pub = mock_mr_pub
        self.mock_aai_event_thread = mock_aai_event_thread
        self.nf_1 = NetworkFunction(nf_name='pnf_1')
        self.nf_2 = NetworkFunction(nf_name='pnf_2')
        self.nfs = [self.nf_1, self.nf_2]

    def tearDown(self):
        pass

    @patch('mod.logger.info')
    @patch('mod.aai_client.get_pmsh_nfs_from_aai')
    def test_execute_no_change_of_state(self, mock_get_aai, mock_logger):
        mock_get_aai.return_value = self.nfs
        sub_handler = SubscriptionHandler(AdministrativeState.UNLOCKED.value, self.mock_mr_pub,
                                          self.mock_app, self.app_conf,
                                          self.mock_aai_event_thread)
        sub_handler.execute()
        mock_logger.assert_called_with('Administrative State did not change in the Config')

    @patch('mod.subscription.Subscription.process_subscription')
    @patch('mod.aai_client.get_pmsh_nfs_from_aai')
    def test_execute_change_of_state_unlocked(self, mock_get_aai, mock_process_sub):
        mock_get_aai.return_value = self.nfs
        self.mock_aai_event_thread.return_value.start.return_value = 'start_method'
        sub_handler = SubscriptionHandler(AdministrativeState.LOCKED.value, self.mock_mr_pub,
                                          self.mock_app, self.app_conf,
                                          self.mock_aai_event_thread.return_value)
        sub_handler.execute()
        self.assertEqual(AdministrativeState.UNLOCKED.value, sub_handler.administrative_state)
        mock_process_sub.assert_called_with(self.nfs, self.mock_mr_pub, self.app_conf)
        self.mock_aai_event_thread.return_value.start.assert_called()

    @patch('mod.subscription.Subscription.process_subscription')
    @patch('mod.aai_client.get_pmsh_nfs_from_aai')
    def test_execute_change_of_state_locked(self, mock_get_aai, mock_process_sub):
        mock_get_aai.return_value = self.nfs
        self.mock_aai_event_thread.return_value.cancel.return_value = 'cancel_method'
        self.app_conf.subscription.administrativeState = AdministrativeState.LOCKED.value
        sub_handler = SubscriptionHandler(AdministrativeState.UNLOCKED.value, self.mock_mr_pub,
                                          self.mock_app, self.app_conf,
                                          self.mock_aai_event_thread.return_value)
        sub_handler.execute()
        self.assertEqual(AdministrativeState.LOCKED.value, sub_handler.administrative_state)
        mock_process_sub.assert_called_with(self.nfs, self.mock_mr_pub, self.app_conf)
        self.mock_aai_event_thread.return_value.cancel.assert_called()
        self.app_conf.subscription.administrativeState = AdministrativeState.UNLOCKED.value

    @patch('mod.subscription.Subscription.process_subscription')
    @patch('mod.logger.error')
    @patch('mod.aai_client.get_pmsh_nfs_from_aai')
    def test_execute_exception(self, mock_get_aai, mock_logger, mock_process_sub):
        mock_get_aai.return_value = self.nfs
        mock_process_sub.side_effect = Exception
        sub_handler = SubscriptionHandler(AdministrativeState.LOCKED.value, self.mock_mr_pub,
                                          self.mock_app, self.app_conf,
                                          self.mock_aai_event_thread)
        sub_handler.execute()
        mock_logger.assert_called_with('Error occurred during the activation/deactivation process ')
