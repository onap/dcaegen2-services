# ============LICENSE_START===================================================
#  Copyright (C) 2019-2020 Nordix Foundation.
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
import unittest
from test.support import EnvironmentVarGuard
from unittest.mock import patch

from requests import Session

from mod import aai_client, create_app, db
from mod.api.controller import status, get_all_sub_to_nf_relations
from mod.network_function import NetworkFunction
from mod.pmsh_utils import AppConfig


class ControllerTestCase(unittest.TestCase):

    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config')
    @patch('mod.update_logging_config')
    @patch('mod.get_db_connection_url')
    @patch.object(Session, 'put')
    def setUp(self, mock_session, mock_get_db_url, mock_update_config, mock_get_pmsh_config):
        mock_get_db_url.return_value = 'sqlite://'
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_xnfs.json'), 'r') as data:
            self.aai_response_data = data.read()
        mock_session.return_value.status_code = 200
        mock_session.return_value.text = self.aai_response_data
        self.env = EnvironmentVarGuard()
        self.env.set('AAI_SERVICE_PORT', '8443')
        self.env.set('LOGGER_CONFIG', os.path.join(os.path.dirname(__file__), 'log_config.yaml'))
        with open(os.path.join(os.path.dirname(__file__), 'data/cbs_data_1.json'), 'r') as data:
            self.cbs_data = json.load(data)
        mock_get_pmsh_config.return_value = self.cbs_data
        self.nf_1 = NetworkFunction(nf_name='pnf_1', orchestration_status='Inventoried')
        self.nf_2 = NetworkFunction(nf_name='pnf_2', orchestration_status='Active')
        self.app = create_app()
        self.app_context = self.app.app_context()
        self.app_context.push()
        db.create_all()
        self.app_conf = AppConfig()
        self.xnfs = aai_client.get_pmsh_nfs_from_aai(self.app_conf)

    def tearDown(self):
        db.session.remove()
        db.drop_all()

    def test_status_response_healthy(self):
        self.assertEqual(status()['status'], 'healthy')

    def test_get_all_sub_to_nf_relations(self):
        sub_model = self.app_conf.subscription.get()
        for nf in [self.nf_1, self.nf_2]:
            self.app_conf.subscription.add_network_function_to_subscription(nf, sub_model)
        all_subs = get_all_sub_to_nf_relations()
        self.assertEqual(len(all_subs[0]['network_functions']), 2)
        self.assertEqual(all_subs[0]['subscription_name'], 'ExtraPM-All-gNB-R2B')
