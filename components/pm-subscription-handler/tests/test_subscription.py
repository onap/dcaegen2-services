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
from unittest import mock

from requests import Session

import mod.aai_client as aai_client
from mod import db, create_app
from mod.network_function import NetworkFunction
from mod.subscription import Subscription, NetworkFunctionFilter


class SubscriptionTest(unittest.TestCase):

    @mock.patch.object(Session, 'put')
    def setUp(self, mock_session):
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_xnfs.json'), 'r') as data:
            self.aai_response_data = data.read()
        mock_session.return_value.status_code = 200
        mock_session.return_value.text = self.aai_response_data
        self.env = EnvironmentVarGuard()
        self.env.set('AAI_SERVICE_HOST', '1.2.3.4')
        self.env.set('AAI_SERVICE_PORT_AAI_SSL', '8443')
        with open(os.path.join(os.path.dirname(__file__), 'data/cbs_data_1.json'), 'r') as data:
            self.cbs_data_1 = json.load(data)
        with open(os.path.join(os.path.dirname(__file__),
                               'data/cbs_data_2.json'), 'r') as data:
            self.cbs_data_2 = json.load(data)
        self.sub_1, self.xnfs = aai_client.get_pmsh_subscription_data(self.cbs_data_1)
        self.sub_2, self.xnfs = aai_client.get_pmsh_subscription_data(self.cbs_data_2)
        self.nf_1 = NetworkFunction(nf_name='pnf_1', orchestration_status='Inventoried')
        self.nf_2 = NetworkFunction(nf_name='pnf_2', orchestration_status='Active')
        self.xnf_filter = NetworkFunctionFilter(**self.sub_1.nfFilter)
        self.app = create_app('testing')
        self.app_context = self.app.app_context()
        self.app_context.push()
        db.create_all()

    def tearDown(self):
        db.session.remove()
        db.drop_all()
        self.app_context.pop()

    @classmethod
    def tearDownClass(cls):
        os.chdir('../')
        db_file = f'{os.getcwd()}/pmsh_service/mod/pmsubscription.db'
        if os.path.exists(db_file):
            os.remove(db_file)

    def test_xnf_filter_true(self):
        self.assertTrue(self.xnf_filter.is_nf_in_filter('pnf1'))

    def test_xnf_filter_false(self):
        self.assertFalse(self.xnf_filter.is_nf_in_filter('PNF-33'))

    def test_sub_measurement_group(self):
        self.assertEqual(len(self.sub_1.measurementGroups), 2)

    def test_sub_file_location(self):
        self.assertEqual(self.sub_1.fileLocation, '/pm/pm.xml')

    def test_get_subscription(self):
        sub_name = 'ExtraPM-All-gNB-R2B'
        self.sub_1.create()
        new_sub = Subscription.get(sub_name)
        self.assertEqual(sub_name, new_sub.subscription_name)

    def test_get_subscription_no_match(self):
        sub_name = 'sub2_does_not_exist'
        sub = Subscription.get(sub_name)
        self.assertEqual(sub, None)

    def test_get_subscriptions(self):
        self.sub_1.create()
        self.sub_2.create()
        subs = self.sub_1.get_all()

        self.assertEqual(2, len(subs))

    def test_create_existing_subscription(self):
        sub1 = self.sub_1.create()
        same_sub1 = self.sub_1.create()
        self.assertEqual(sub1, same_sub1)
        self.assertEqual(1, len(self.sub_1.get_all()))

    def test_get_nfs_per_subscription(self):
        nf_array = [self.nf_1, self.nf_2]
        self.sub_1.add_network_functions_to_subscription(nf_array)
        nfs_for_sub_1 = Subscription.get_all_nfs_subscription_relations()
        self.assertEqual(2, len(nfs_for_sub_1))

    def test_add_network_functions_per_subscription(self):
        nf_array = [self.nf_1, self.nf_2]
        self.sub_1.add_network_functions_to_subscription(nf_array)
        nfs_for_sub_1 = Subscription.get_all_nfs_subscription_relations()
        self.assertEqual(2, len(nfs_for_sub_1))
        new_nf_array = [NetworkFunction(nf_name='vnf_3', orchestration_status='Inventoried')]
        self.sub_1.add_network_functions_to_subscription(new_nf_array)
        nf_subs = Subscription.get_all_nfs_subscription_relations()
        print(nf_subs)
        self.assertEqual(3, len(nf_subs))

    def test_add_duplicate_network_functions_per_subscription(self):
        nf_array = [self.nf_1]
        self.sub_1.add_network_functions_to_subscription(nf_array)
        nf_subs = Subscription.get_all_nfs_subscription_relations()
        self.assertEqual(1, len(nf_subs))
        self.sub_1.add_network_functions_to_subscription(nf_array)
        nf_subs = Subscription.get_all_nfs_subscription_relations()
        self.assertEqual(1, len(nf_subs))
