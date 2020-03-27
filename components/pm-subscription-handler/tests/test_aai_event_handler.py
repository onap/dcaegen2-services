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
from os import path
from unittest import TestCase
from unittest.mock import patch, Mock

from mod.aai_event_handler import process_aai_events
from mod.network_function import NetworkFunction, OrchestrationStatus


class AAIEventHandlerTest(TestCase):

    def setUp(self):
        with open(path.join(path.dirname(__file__), 'data/cbs_data_1.json'), 'r') as data:
            self.cbs_data_1 = json.load(data)
        with open(path.join(path.dirname(__file__), 'data/mr_aai_events.json'), 'r') as data:
            self.mr_aai_events = json.load(data)["mr_response"]
        self.mock_sub = Mock(nfFilter={'swVersions': ['1.0.0', '1.0.1'],
                                       'nfNames': ['^pnf.*', '^vnf.*']})
        self.mock_mr_sub = Mock(get_from_topic=Mock(return_value=self.mr_aai_events))
        self.mock_mr_pub = Mock()
        self.mock_app = Mock()

    @patch('mod.aai_event_handler.NetworkFunction.delete')
    @patch('mod.aai_event_handler.NetworkFunction.get')
    @patch('pmsh_service_main.AppConfig')
    def test_process_aai_update_and_delete_events(self, mock_app_conf, mock_nf_get, mock_nf_delete):
        pnf_already_active = NetworkFunction(nf_name='pnf_already_active',
                                             orchestration_status=OrchestrationStatus.ACTIVE.value)
        mock_nf_get.side_effect = [None, pnf_already_active]
        expected_nf_for_processing = NetworkFunction(
            nf_name='pnf_newly_discovered', orchestration_status=OrchestrationStatus.ACTIVE.value)

        process_aai_events(self.mock_mr_sub, self.mock_sub,
                           self.mock_mr_pub, self.mock_app, mock_app_conf)

        self.mock_sub.process_subscription.assert_called_once_with([expected_nf_for_processing],
                                                                   self.mock_mr_pub, mock_app_conf)
        mock_nf_delete.assert_called_once_with(nf_name='pnf_to_be_deleted')
