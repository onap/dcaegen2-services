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
import json
from unittest import mock, TestCase
from unittest.mock import patch

import pmsh_service_main as pmsh_service
from mod.network_function import NetworkFunction


class PMSHServiceTest(TestCase):
    def setUp(self):
        with open(os.path.join(os.path.dirname(__file__), 'data/cbs_data_1.json'), 'r') as data:
            self.cbs_data_1 = json.load(data)
        self.mock_mr_pub = mock.Mock()
        self.mock_config_handler = mock.Mock()
        self.mock_app = mock.Mock()
        self.mock_sub = mock.Mock()
        self.nf_1 = NetworkFunction(nf_name='pnf_1')
        self.nf_2 = NetworkFunction(nf_name='pnf_2')
        self.nfs = [self.nf_1, self.nf_2]

    @patch('threading.Timer')
    @patch('mod.aai_client.get_pmsh_subscription_data')
    def test_subscription_processor_changed_state(self, mock_get_aai, mock_thread):
        self.mock_config_handler.get_config.return_value = self.cbs_data_1
        mock_get_aai.return_value = self.mock_sub, self.nfs
        mock_thread.start.return_value = 1

        pmsh_service.subscription_processor(self.mock_config_handler, 'LOCKED', self.mock_mr_pub,
                                            self.mock_app)

        self.mock_sub.process_subscription.assert_called_with(self.nfs, self.mock_mr_pub)

    @patch('threading.Timer')
    @patch('mod.pmsh_logging.debug')
    @patch('mod.aai_client.get_pmsh_subscription_data')
    def test_subscription_processor_unchanged_state(self, mock_get_aai, mock_logger, mock_thread):
        self.mock_config_handler.get_config.return_value = self.cbs_data_1
        mock_get_aai.return_value = self.mock_sub, self.nfs
        mock_thread.start.return_value = 1

        pmsh_service.subscription_processor(self.mock_config_handler, 'UNLOCKED', self.mock_mr_pub,
                                            self.mock_app)

        mock_logger.assert_called_with('Administrative State did not change in the Config')

    @patch('threading.Timer')
    @patch('mod.pmsh_logging.debug')
    @patch('mod.aai_client.get_pmsh_subscription_data')
    def test_subscription_processor_exception(self, mock_get_aai, mock_logger, mock_thread):
        self.mock_config_handler.get_config.return_value = self.cbs_data_1
        mock_get_aai.return_value = self.mock_sub, self.nfs
        mock_thread.start.return_value = 1
        self.mock_sub.process_subscription.side_effect = Exception

        pmsh_service.subscription_processor(self.mock_config_handler, 'LOCKED', self.mock_mr_pub,
                                            self.mock_app)
        mock_logger.assert_called_with(f'Error occurred during the '
                                       f'activation/deactivation process ')
