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
import unittest
from test.support import EnvironmentVarGuard
from unittest import mock

from requests import Session

import aai_event_handler
import mod.aai_client as aai_client
from aai_event_handler import OrchestrationStatus
from mod import db, create_app
from mod.network_function import NetworkFunction


class AAIEventHandlerTest(unittest.TestCase):

    @mock.patch('mod.get_db_connection_url')
    @mock.patch.object(Session, 'put')
    def setUp(self, mock_session, mock_get_db_url):
        mock_get_db_url.return_value = 'sqlite://'
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_xnfs.json'), 'r') as data:
            self.aai_response_data = data.read()
        mock_session.return_value.status_code = 200
        mock_session.return_value.text = self.aai_response_data
        self.env = EnvironmentVarGuard()
        self.env.set('AAI_SERVICE_HOST', '1.2.3.4')
        self.env.set('AAI_SERVICE_PORT_AAI_SSL', '8443')
        self.env.set('TESTING', 'True')
        self.env.set('LOGS_PATH', './unit_test_logs')
        with open(os.path.join(os.path.dirname(__file__), 'data/cbs_data_1.json'), 'r') as data:
            self.cbs_data_1 = json.load(data)
        with open(os.path.join(os.path.dirname(__file__), 'data/mr_aai_events.json'), 'r') as data:
            self.mr_aai_events = json.load(data)["mr_response"]
        self.sub_1, self.xnfs = aai_client.get_pmsh_subscription_data(self.cbs_data_1)
        self.app = create_app()
        self.app_context = self.app.app_context()
        self.app_context.push()
        db.create_all()

    def tearDown(self):
        db.session.remove()
        db.drop_all()
        self.app_context.pop()

    def test_process_aai_events(self):
        pnf_new = NetworkFunction(nf_name='pnf_newly_discovered',
                                  orchestration_status=OrchestrationStatus.ACTIVE.value)
        pnf_already_active = NetworkFunction(nf_name='pnf_already_active',
                                             orchestration_status=OrchestrationStatus.ACTIVE.value)
        nfs_in_db = [pnf_already_active]
        self.sub_1.add_network_functions_to_subscription(nfs_in_db)
        mock_mr_sub = mock.Mock(get_from_topic=mock.Mock(return_value=self.mr_aai_events))
        mock_mr_pub = mock.Mock()
        mock_app = mock.Mock()
        expected_mr_publish_args = ((self.sub_1, pnf_new.nf_name),)

        aai_event_handler.process_aai_events(mock_mr_sub, self.sub_1, mock_mr_pub, mock_app)
        actual_mr_publish_args = mock_mr_pub.publish_subscription_event_data.call_args

        self.assertEqual(1, mock_mr_pub.publish_subscription_event_data.call_count)
        self.assertEqual(expected_mr_publish_args, actual_mr_publish_args)
