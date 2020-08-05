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
from test.support import EnvironmentVarGuard
from unittest import TestCase
from unittest.mock import patch

from mod import create_app, db
from mod.network_function import NetworkFunction
from mod.pmsh_utils import AppConfig
from mod.subscription import AdministrativeState
from mod.subscription_handler import SubscriptionHandler


class SubscriptionHandlerTest(TestCase):

    @classmethod
    def setUpClass(cls):
        cls.env = EnvironmentVarGuard()
        cls.env.set('AAI_SERVICE_PORT', '8443')
        cls.env.set('LOGGER_CONFIG', os.path.join(os.path.dirname(__file__), 'log_config.yaml'))
        cls.nfs = [NetworkFunction(nf_name='pnf_1'), NetworkFunction(nf_name='pnf_2')]

    @patch('mod.get_db_connection_url')
    @patch('mod.update_logging_config')
    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    @patch('mod.pmsh_utils._MrPub')
    @patch('mod.pmsh_utils.PeriodicTask')
    def setUp(self, mock_periodic_task, mock_mr_pub, mock_get_pmsh_config, mock_update_config,
              mock_get_db_url):
        mock_get_db_url.return_value = 'sqlite://'
        with open(os.path.join(os.path.dirname(__file__), 'data/cbs_data_1.json'), 'r') as data:
            self.cbs_data = json.load(data)
        mock_get_pmsh_config.return_value = self.cbs_data
        self.mock_mr_pub = mock_mr_pub
        self.mock_aai_event_thread = mock_periodic_task
        self.mock_policy_event_thread = mock_periodic_task
        self.app = create_app()
        self.app.app_context().push()
        db.create_all()
        self.app_conf = AppConfig()

    def tearDown(self):
        db.drop_all()
        db.session.remove()

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
