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
import os
import signal
import threading
import time
from unittest import TestCase
from unittest.mock import patch, Mock, MagicMock

import pmsh_service_main
from mod.exit_handler import ExitHandler
from mod.pmsh_utils import PeriodicTask
from mod.subscription import AdministrativeState


class ExitHandlerTests(TestCase):

    @patch('mod.exit_handler.NetworkFunction.get_nf_model_objects_from_relationship')
    @patch('mod.exit_handler.NetworkFunction.get_nf_objects_from_nf_model_objects')
    @patch('pmsh_service_main.ConfigHandler')
    @patch('pmsh_service_main.create_app')
    @patch('pmsh_service_main.db')
    @patch('pmsh_service_main.aai.get_pmsh_subscription_data')
    @patch('pmsh_service_main.AppConfig')
    @patch('pmsh_service_main.Subscription')
    @patch('pmsh_service_main.launch_api_server')
    @patch('pmsh_service_main.SubscriptionHandler')
    @patch.object(PeriodicTask, 'start')
    @patch.object(PeriodicTask, 'cancel')
    def test_terminate_signal_success(self, mock_task_cancel, mock_task_start, mock_sub_handler,
                                      mock_launch_api_server, mock_sub, mock_app_conf, mock_aai,
                                      mock_db, mock_app, mock_config_handler, mock_f1, mock_f2):
        pid = os.getpid()
        mock_aai.return_value = [Mock(), Mock()]
        mock_db.get_app.return_value = Mock()

        mock_sub.administrativeState = AdministrativeState.UNLOCKED.value
        mock_sub.process_subscription = Mock()
        mock_sub_handler_instance = MagicMock(execute=Mock(), current_sub=mock_sub)
        mock_sub_handler.side_effect = [mock_sub_handler_instance]

        def mock_api_server_run(param):
            while mock_sub.administrativeState == AdministrativeState.UNLOCKED.value:
                time.sleep(1)

        mock_launch_api_server.side_effect = mock_api_server_run

        def trigger_signal():
            time.sleep(1)
            os.kill(pid, signal.SIGTERM)

        thread = threading.Thread(target=trigger_signal)
        thread.start()

        pmsh_service_main.main()

        self.assertEqual(3, mock_task_cancel.call_count)
        self.assertTrue(ExitHandler.shutdown_signal_received)
        self.assertEqual(1, mock_sub.process_subscription.call_count)
        self.assertEqual(mock_sub.administrativeState, AdministrativeState.LOCKED.value)
