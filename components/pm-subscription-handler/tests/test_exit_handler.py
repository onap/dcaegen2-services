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
from signal import SIGTERM, signal
from unittest.mock import patch, Mock

from mod.exit_handler import ExitHandler
from mod.subscription import Subscription
from tests.base_setup import BaseClassSetup


class ExitHandlerTests(BaseClassSetup):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    @patch('mod.pmsh_utils.PeriodicTask')
    def setUp(self, mock_periodic_task):
        super().setUp()
        self.mock_aai_event_thread = mock_periodic_task
        self.sub = self.app_conf.subscription

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

    @patch.object(Subscription, 'update_sub_nf_status')
    @patch.object(Subscription, 'update_subscription_status')
    def test_terminate_signal_successful(self, mock_upd_sub_status,
                                         mock_upd_subnf_status):
        handler = ExitHandler(periodic_tasks=[self.mock_aai_event_thread],
                              app_conf=self.app_conf,
                              subscription_handler=Mock())
        signal(SIGTERM, handler)
        os.kill(os.getpid(), SIGTERM)
        self.assertTrue(ExitHandler.shutdown_signal_received)
