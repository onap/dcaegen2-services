# ============LICENSE_START===================================================
#  Copyright (C) 2020-2022 Nordix Foundation.
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
from signal import SIGTERM, signal
from unittest.mock import patch

from mod.exit_handler import ExitHandler
from tests.base_setup import BaseClassSetup


class ExitHandlerTests(BaseClassSetup):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    @patch('mod.pmsh_utils.PeriodicTask')
    @patch('mod.pmsh_utils.PeriodicTask')
    def setUp(self, mock_periodic_task_aai, mock_periodic_task_policy):
        super().setUp()
        self.mock_aai_event_thread = mock_periodic_task_aai
        self.mock_policy_resp_handler_thread = mock_periodic_task_policy

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

    def test_terminate_signal_successful(self):
        handler = ExitHandler(periodic_tasks=[self.mock_aai_event_thread,
                                              self.mock_policy_resp_handler_thread])
        signal(SIGTERM, handler)
        os.kill(os.getpid(), SIGTERM)
        self.assertTrue(ExitHandler.shutdown_signal_received)
