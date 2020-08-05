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
from signal import SIGTERM, signal
from test.support import EnvironmentVarGuard
from unittest import TestCase
from unittest.mock import patch, Mock

from mod.api.db_models import NetworkFunctionModel
from mod.exit_handler import ExitHandler
from mod.pmsh_utils import AppConfig
from mod.subscription import Subscription


class ExitHandlerTests(TestCase):
    @patch('mod.subscription.Subscription.create')
    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    @patch('mod.pmsh_utils.PeriodicTask')
    def setUp(self, mock_periodic_task, mock_get_pmsh_config, mock_sub_create):
        self.env = EnvironmentVarGuard()
        self.env.set('LOGGER_CONFIG', os.path.join(os.path.dirname(__file__), 'log_config.yaml'))
        with open(os.path.join(os.path.dirname(__file__), 'data/cbs_data_1.json'), 'r') as data:
            self.cbs_data = json.load(data)
        mock_get_pmsh_config.return_value = self.cbs_data
        self.mock_aai_event_thread = mock_periodic_task
        self.app_conf = AppConfig()
        self.sub = self.app_conf.subscription

    @patch('mod.logger.debug')
    @patch.object(Subscription, 'update_sub_nf_status')
    @patch.object(Subscription, 'update_subscription_status')
    @patch.object(Subscription, '_get_nf_models',
                  return_value=[NetworkFunctionModel('pnf1', 'ACTIVE')])
    def test_terminate_signal_successful(self, mock_sub_get_nf_models, mock_upd_sub_status,
                                         mock_upd_subnf_status, mock_logger):
        handler = ExitHandler(periodic_tasks=[self.mock_aai_event_thread],
                              app_conf=self.app_conf,
                              subscription_handler=Mock())
        signal(SIGTERM, handler)
        os.kill(os.getpid(), SIGTERM)
        self.assertTrue(ExitHandler.shutdown_signal_received)
