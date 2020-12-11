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
from unittest.mock import patch, Mock

from mod.aai_event_handler import process_aai_events
from tests.base_setup import BaseClassSetup


class AAIEventHandlerTest(BaseClassSetup):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    def setUp(self):
        super().setUp()
        with open(path.join(path.dirname(__file__), 'data/mr_aai_events.json'), 'r') as data:
            self.mr_aai_events = json.load(data)["mr_response"]
        self.mock_mr_sub = Mock(get_from_topic=Mock(return_value=self.mr_aai_events))
        self.mock_mr_pub = Mock()
        self.mock_app = Mock()

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

    @patch('mod.network_function.NetworkFunction.set_nf_model_params')
    @patch('mod.subscription.Subscription.create_subscription_on_nfs')
    @patch('mod.aai_event_handler.NetworkFunction.delete')
    def test_process_aai_update_and_delete_events(self, mock_nf_delete, mock_activate_sub,
                                                  mock_set_sdnc_params):
        mock_set_sdnc_params.return_value = True
        process_aai_events(self.mock_mr_sub, self.mock_mr_pub, self.mock_app, self.app_conf)
        self.assertEqual(mock_activate_sub.call_count, 2)
        mock_nf_delete.assert_called_once()
