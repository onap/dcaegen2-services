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
import os
from test.support import EnvironmentVarGuard
from unittest import TestCase
from unittest.mock import patch

from mod import db, create_app
from mod.network_function import NetworkFunction
from mod.subscription import Subscription


class NetworkFunctionTests(TestCase):

    @patch('mod.update_logging_config')
    @patch('mod.get_db_connection_url')
    def setUp(self, mock_get_db_url, mock_update_config):
        mock_get_db_url.return_value = 'sqlite://'
        self.nf_1 = NetworkFunction(nf_name='pnf_1', orchestration_status='Inventoried')
        self.nf_2 = NetworkFunction(nf_name='pnf_2', orchestration_status='Active')
        self.env = EnvironmentVarGuard()
        self.env.set('LOGGER_CONFIG', os.path.join(os.path.dirname(__file__), 'log_config.yaml'))
        self.app = create_app()
        self.app_context = self.app.app_context()
        self.app_context.push()
        db.create_all()

    def tearDown(self):
        db.session.remove()
        db.drop_all()
        self.app_context.pop()

    def test_get_network_function(self):
        self.nf_1.create()
        nf = NetworkFunction.get('pnf_1')
        self.assertEqual(self.nf_1.nf_name, nf.nf_name)

    def test_get_network_function_no_match(self):
        self.nf_1.create()
        nf_name = 'nf2_does_not_exist'
        nf = NetworkFunction.get(nf_name)
        self.assertEqual(nf, None)

    def test_get_network_functions(self):
        self.nf_1.create()
        self.nf_2.create()
        nfs = NetworkFunction.get_all()

        self.assertEqual(2, len(nfs))

    def test_create_existing_network_function(self):
        nf = self.nf_1.create()
        same_nf = self.nf_1.create()

        self.assertEqual(nf, same_nf)

    def test_delete_network_function(self):
        self.nf_1.create()
        self.nf_2.create()
        sub = Subscription(**{"subscriptionName": "sub"})
        for nf in [self.nf_1, self.nf_2]:
            sub.add_network_function_to_subscription(nf)

        NetworkFunction.delete(nf_name=self.nf_1.nf_name)

        nfs = NetworkFunction.get_all()
        self.assertEqual(1, len(nfs))
        self.assertEqual(1, len(Subscription.get_all_nfs_subscription_relations()))
        pnf_1_deleted = [nf for nf in nfs if nf.nf_name != self.nf_1.nf_name]
        self.assertTrue(pnf_1_deleted)
