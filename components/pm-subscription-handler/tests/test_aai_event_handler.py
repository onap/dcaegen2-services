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
from os import path
from test.support import EnvironmentVarGuard
from unittest import TestCase
from unittest.mock import patch, Mock

from mod import create_app, db
from mod.aai_event_handler import process_aai_events
from mod.network_function import NetworkFunction
from mod.pmsh_utils import AppConfig


class AAIEventHandlerTest(TestCase):

    @patch('mod.get_db_connection_url')
    @patch('mod.update_logging_config')
    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    def setUp(self, mock_get_pmsh_config, mock_update_config, mock_get_db_url):
        mock_get_db_url.return_value = 'sqlite://'
        with open(path.join(path.dirname(__file__), 'data/cbs_data_1.json'), 'r') as data:
            self.cbs_data = json.load(data)
        mock_get_pmsh_config.return_value = self.cbs_data
        self.app_conf = AppConfig()
        with open(path.join(path.dirname(__file__), 'data/mr_aai_events.json'), 'r') as data:
            self.mr_aai_events = json.load(data)["mr_response"]
        self.env = EnvironmentVarGuard()
        self.env.set('LOGGER_CONFIG', os.path.join(os.path.dirname(__file__), 'log_config.yaml'))
        self.mock_mr_sub = Mock(get_from_topic=Mock(return_value=self.mr_aai_events))
        self.mock_mr_pub = Mock()
        self.mock_app = Mock()
        self.app = create_app()
        self.app_context = self.app.app_context()
        self.app_context.push()
        db.create_all()

    def tearDown(self):
        db.session.remove()
        db.drop_all()
        self.app_context.pop()

    @patch('mod.subscription.Subscription.activate_subscription')
    @patch('mod.aai_event_handler.NetworkFunction.delete')
    @patch('mod.aai_event_handler.NetworkFunction.get')
    def test_process_aai_update_and_delete_events(self, mock_nf_get, mock_nf_delete,
                                                  mock_activate_sub):
        pnf_already_active = NetworkFunction(nf_name='pnf_already_active',
                                             orchestration_status='Active')
        mock_nf_get.side_effect = [None, pnf_already_active]
        expected_nf_for_processing = NetworkFunction(
            nf_name='pnf_newly_discovered', orchestration_status='Active')

        process_aai_events(self.mock_mr_sub, self.mock_mr_pub, self.mock_app, self.app_conf)

        mock_activate_sub.assert_called_once_with([expected_nf_for_processing],
                                                  self.mock_mr_pub, self.app_conf)
        mock_nf_delete.assert_called_once_with(nf_name='pnf_to_be_deleted')
