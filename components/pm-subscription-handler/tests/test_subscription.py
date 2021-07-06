# ============LICENSE_START===================================================
#  Copyright (C) 2019-2021 Nordix Foundation.
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
from unittest.mock import patch, Mock

from requests import Session

import mod
import mod.aai_client as aai_client
from mod.network_function import NetworkFunction
from mod.subscription import Subscription
from tests.base_setup import BaseClassSetup
from mod.network_function_filter import NetworkFunctionFilter

class SubscriptionTest(BaseClassSetup):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    @patch.object(Session, 'get')
    @patch.object(Session, 'put')
    def setUp(self, mock_session_put, mock_session_get):
        super().setUp()
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_xnfs.json'), 'r') as data:
            self.aai_response_data = data.read()
        mock_session_put.return_value.status_code = 200
        mock_session_put.return_value.text = self.aai_response_data
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_model_info.json'), 'r') as data:
            self.aai_model_data = data.read()
        mock_session_get.return_value.status_code = 200
        mock_session_get.return_value.text = self.aai_model_data
        self.mock_mr_sub = Mock()
        self.mock_mr_pub = Mock()
        self.app_conf.subscription.create()
        self.xnfs = aai_client.get_pmsh_nfs_from_aai(self.app_conf)
        self.sub_model = self.app_conf.subscription.get()

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

    def test_sub_measurement_group(self):
        self.assertEqual(len(self.app_conf.subscription.measurementGroups), 2)

    def test_sub_file_location(self):
        self.assertEqual(self.app_conf.subscription.fileLocation, '/pm/pm.xml')

    def test_get_subscription(self):
        sub_name = 'ExtraPM-All-gNB-R2B'
        new_sub = self.app_conf.subscription.get()
        self.assertEqual(sub_name, new_sub.subscription_name)

    def test_get_nf_names_per_sub(self):
        self.app_conf.subscription.add_network_function_to_subscription(list(self.xnfs)[0],
                                                                        self.sub_model)
        self.app_conf.subscription.add_network_function_to_subscription(list(self.xnfs)[1],
                                                                        self.sub_model)

    def test_create_existing_subscription(self):
        sub1 = self.app_conf.subscription.create()
        same_sub1 = self.app_conf.subscription.create()
        self.assertEqual(sub1, same_sub1)
        self.assertEqual(1, len(self.app_conf.subscription.get_all()))

    def test_add_duplicate_network_functions_per_subscription(self):
        self.app_conf.subscription.add_network_function_to_subscription(list(self.xnfs)[0],
                                                                        self.sub_model)
        nf_subs = Subscription.get_all_nfs_subscription_relations()
        self.assertEqual(1, len(nf_subs))
        self.app_conf.subscription.add_network_function_to_subscription(list(self.xnfs)[0],
                                                                        self.sub_model)
        nf_subs = Subscription.get_all_nfs_subscription_relations()
        self.assertEqual(1, len(nf_subs))

    def test_update_subscription_status(self):
        self.app_conf.subscription.administrativeState = 'new_status'
        self.app_conf.subscription.update_subscription_status()
        sub = self.app_conf.subscription.get()

        self.assertEqual('new_status', sub.status)

    def test_update_sub_nf_status(self):
        sub_name = 'ExtraPM-All-gNB-R2B'
        for nf in self.xnfs:
            self.app_conf.subscription.add_network_function_to_subscription(nf, self.sub_model)
        sub_nfs = Subscription.get_all_nfs_subscription_relations()
        self.assertEqual('PENDING_CREATE', sub_nfs[0].nf_sub_status)

        Subscription.update_sub_nf_status(sub_name, 'Active', 'pnf_23')
        sub_nfs = Subscription.get_all_nfs_subscription_relations()
        self.assertEqual('PENDING_CREATE', sub_nfs[0].nf_sub_status)
        self.assertEqual('PENDING_CREATE', sub_nfs[1].nf_sub_status)

    @patch('mod.subscription.Subscription.add_network_function_to_subscription')
    @patch('mod.subscription.Subscription.update_sub_nf_status')
    def test_process_activate_subscription(self, mock_update_sub_nf, mock_add_nfs):
        self.app_conf.subscription.create_subscription_on_nfs([list(self.xnfs)[0]],
                                                              self.mock_mr_pub, self.app_conf)

        mock_add_nfs.assert_called()
        self.assertTrue(self.mock_mr_pub.publish_subscription_event_data.called)
        mock_update_sub_nf.assert_called_with(self.app_conf.subscription.subscriptionName,
                                              'PENDING_CREATE', list(self.xnfs)[0].nf_name)

    @patch('mod.subscription.Subscription.get_network_functions')
    @patch('mod.subscription.Subscription.update_sub_nf_status')
    def test_process_deactivate_subscription(self, mock_update_sub_nf, mock_get_nfs):
        self.app_conf.subscription.administrativeState = 'LOCKED'
        mock_get_nfs.return_value = [list(self.xnfs)[0]]
        self.app_conf.subscription.delete_subscription_from_nfs(self.xnfs, self.mock_mr_pub,
                                                                self.app_conf)
        self.assertTrue(self.mock_mr_pub.publish_subscription_event_data.called)
        self.assertEqual(mock_update_sub_nf.call_count, 3)

    def test_activate_subscription_exception(self):
        self.assertRaises(Exception, self.app_conf.subscription.create_subscription_on_nfs,
                          [list(self.xnfs)[0]], 'not_mr_pub', 'app_config')

    def test_prepare_subscription_event(self):
        with open(os.path.join(os.path.dirname(__file__),
                               'data/pm_subscription_event.json'), 'r') as data:
            expected_sub_event = json.load(data)
        nf = NetworkFunction(nf_name='pnf_1',
                             ip_address='1.2.3.4',
                             model_invariant_id='some-id',
                             model_version_id='some-id')
        nf.sdnc_model_name = 'some-name'
        nf.sdnc_model_version = 'some-version'
        actual_sub_event = self.app_conf.subscription.prepare_subscription_event(nf, self.app_conf)
        print(actual_sub_event)
        self.assertEqual(expected_sub_event, actual_sub_event)

    def test_get_network_functions(self):
        for nf in self.xnfs:
            self.app_conf.subscription.add_network_function_to_subscription(nf, self.sub_model)
        nfs = self.app_conf.subscription.get_network_functions()

        self.assertEqual(3, len(nfs))
        self.assertIsInstance(nfs[0], NetworkFunction)

    def test_filter_diff_with_difference(self):
        networkFunction = '{"nfNames":["^pnf.*","^vnf.*"],"modelInvariantIDs": ["Extra Data"],' \
                          '"modelVersionIDs": ["Extra Data"],"modelNames": ["Extra Data""]}'
        self.assertTrue(mod.network_function_filter.filter_diff(self.app_conf.subscription, networkFunction))

    def test_filter_diff_without_difference(self):
        networkFunction = '{"nfNames":["^pnf.*","^vnf.*"],"modelInvariantIDs": [],' \
                          '"modelVersionIDs": [],"modelNames": []}'
        self.assertTrue(mod.network_function_filter.filter_diff(self.app_conf.subscription, networkFunction))
        self.assertIsNotNone(self.app_conf.subscription)

    def test_update_subscription_filter(self):
        original_filter = self.app_conf.subscription.nfFilter
        self.app_conf.subscription.nfFilter = '{"nfNames":["^pnf.*","^vnf.*"],"modelInvariantIDs": ["Extra Data"],' \
                                              '"modelVersionIDs": ["Extra Data"],"modelNames": ["Extra Data""]}'
        self.app_conf.subscription.update_subscription_filter()
        updated_subscription = (self.app_conf.subscription.get())
        self.assertTrue(updated_subscription.nfFilter == self.app_conf.subscription.nfFilter)
        self.assertFalse(updated_subscription == original_filter)
        print(self.app_conf.subscription.get_network_functions())
