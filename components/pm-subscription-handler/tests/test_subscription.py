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
from test.support import EnvironmentVarGuard
from unittest import TestCase
from unittest.mock import patch

from requests import Session
from tenacity import stop_after_attempt

import mod.aai_client as aai_client
from mod import db, create_app
from mod.api.db_models import NetworkFunctionModel
from mod.network_function import NetworkFunction, NetworkFunctionFilter, OrchestrationStatus
from mod.pmsh_utils import AppConfig
from mod.subscription import Subscription


class SubscriptionTest(TestCase):
    @patch('mod.update_config')
    @patch('mod.pmsh_utils._MrPub')
    @patch('mod.pmsh_utils._MrSub')
    @patch('mod.get_db_connection_url')
    @patch.object(Session, 'put')
    @patch('pmsh_service_main.AppConfig')
    def setUp(self, mock_app_config, mock_session, mock_get_db_url,
              mock_mr_sub, mock_mr_pub, mock_update_config):
        mock_get_db_url.return_value = 'sqlite://'
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_xnfs.json'), 'r') as data:
            self.aai_response_data = data.read()
        mock_session.return_value.status_code = 200
        mock_session.return_value.text = self.aai_response_data
        self.env = EnvironmentVarGuard()
        self.env.set('AAI_SERVICE_HOST', '1.2.3.4')
        self.env.set('AAI_SERVICE_PORT', '8443')
        self.env.set('LOGGER_CONFIG', os.path.join(os.path.dirname(__file__), 'log_config.yaml'))
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
        self.mock_mr_sub = mock_mr_sub
        self.mock_mr_pub = mock_mr_pub
        self.app = create_app()
        self.app_context = self.app.app_context()
        self.app_context.push()
        self.mock_app_config = mock_app_config
        db.create_all()

    def tearDown(self):
        db.session.remove()
        db.drop_all()
        self.app_context.pop()

    def test_xnf_filter_true(self):
        self.assertTrue(self.xnf_filter.is_nf_in_filter('pnf1', OrchestrationStatus.ACTIVE.value))

    def test_xnf_filter_false(self):
        self.assertFalse(self.xnf_filter.is_nf_in_filter('PNF-33',
                                                         OrchestrationStatus.ACTIVE.value))

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

    def test_get_nf_names_per_sub(self):
        self.sub_1.create()
        self.sub_1.add_network_function_to_subscription(self.nf_1)
        self.sub_1.add_network_function_to_subscription(self.nf_2)
        nfs = Subscription.get_nf_names_per_sub(self.sub_1.subscriptionName)
        self.assertEqual(2, len(nfs))

    def test_create_existing_subscription(self):
        sub1 = self.sub_1.create()
        same_sub1 = self.sub_1.create()
        self.assertEqual(sub1, same_sub1)
        self.assertEqual(1, len(self.sub_1.get_all()))

    def test_add_network_functions_per_subscription(self):
        for nf in [self.nf_1, self.nf_2]:
            self.sub_1.add_network_function_to_subscription(nf)
        nfs_for_sub_1 = Subscription.get_all_nfs_subscription_relations()
        self.assertEqual(2, len(nfs_for_sub_1))
        new_nf = NetworkFunction(nf_name='vnf_3', orchestration_status='Inventoried')
        self.sub_1.add_network_function_to_subscription(new_nf)
        nf_subs = Subscription.get_all_nfs_subscription_relations()
        self.assertEqual(3, len(nf_subs))

    def test_add_duplicate_network_functions_per_subscription(self):
        self.sub_1.add_network_function_to_subscription(self.nf_1)
        nf_subs = Subscription.get_all_nfs_subscription_relations()
        self.assertEqual(1, len(nf_subs))
        self.sub_1.add_network_function_to_subscription(self.nf_1)
        nf_subs = Subscription.get_all_nfs_subscription_relations()
        self.assertEqual(1, len(nf_subs))

    def test_update_subscription_status(self):
        sub_name = 'ExtraPM-All-gNB-R2B'
        self.sub_1.create()
        self.sub_1.administrativeState = 'new_status'
        self.sub_1.update_subscription_status()
        sub = Subscription.get(sub_name)

        self.assertEqual('new_status', sub.status)

    def test_delete_subscription(self):
        for nf in [self.nf_1, self.nf_2]:
            self.sub_1.add_network_function_to_subscription(nf)
        for nf in [self.nf_2]:
            self.sub_2.add_network_function_to_subscription(nf)

        self.sub_1.delete_subscription()

        self.assertEqual(1, len(Subscription.get_all()))
        self.assertEqual(None, Subscription.get(self.sub_1.subscriptionName))
        self.assertEqual(1, len(Subscription.get_all_nfs_subscription_relations()))
        self.assertEqual(1, len(NetworkFunction.get_all()))
        self.assertEqual(None, NetworkFunction.get(nf_name=self.nf_1.nf_name))

    def test_update_sub_nf_status(self):
        sub_name = 'ExtraPM-All-gNB-R2B'
        for nf in [self.nf_1, self.nf_2]:
            self.sub_1.add_network_function_to_subscription(nf)
        sub_nfs = Subscription.get_all_nfs_subscription_relations()
        self.assertEqual('PENDING_CREATE', sub_nfs[0].nf_sub_status)

        Subscription.update_sub_nf_status(sub_name, 'Active', 'pnf_1')
        sub_nfs = Subscription.get_all_nfs_subscription_relations()
        self.assertEqual('Active', sub_nfs[0].nf_sub_status)
        self.assertEqual('PENDING_CREATE', sub_nfs[1].nf_sub_status)

    @patch('mod.subscription.Subscription.add_network_function_to_subscription')
    @patch('mod.subscription.Subscription.update_sub_nf_status')
    @patch('mod.subscription.Subscription.update_subscription_status')
    def test_process_activate_subscription(self, mock_update_sub_status,
                                           mock_update_sub_nf, mock_add_nfs):
        self.sub_1.process_subscription.retry.stop = stop_after_attempt(1)
        self.sub_1.process_subscription([self.nf_1], self.mock_mr_pub, self.mock_app_config)

        mock_update_sub_status.assert_called()
        mock_add_nfs.assert_called()
        self.assertTrue(self.mock_mr_pub.publish_subscription_event_data.called)
        mock_update_sub_nf.assert_called_with(self.sub_1.subscriptionName,
                                              'PENDING_CREATE', self.nf_1.nf_name)

    @patch('mod.subscription.Subscription.update_sub_nf_status')
    @patch('mod.subscription.Subscription.update_subscription_status')
    def test_process_deactivate_subscription(self, mock_update_sub_status,
                                             mock_update_sub_nf):
        self.sub_1.administrativeState = 'LOCKED'
        self.sub_1.process_subscription.retry.stop = stop_after_attempt(1)
        self.sub_1.process_subscription([self.nf_1], self.mock_mr_pub, self.mock_app_config)

        self.assertTrue(self.mock_mr_pub.publish_subscription_event_data.called)
        mock_update_sub_nf.assert_called_with(self.sub_1.subscriptionName,
                                              'PENDING_DELETE', self.nf_1.nf_name)
        mock_update_sub_status.assert_called()

    def test_process_subscription_exception(self):
        self.sub_1.process_subscription.retry.stop = stop_after_attempt(1)
        self.assertRaises(Exception, self.sub_1.process_subscription,
                          [self.nf_1], 'not_mr_pub', 'app_config')

    def test_prepare_subscription_event(self):
        with open(os.path.join(os.path.dirname(__file__),
                               'data/pm_subscription_event.json'), 'r') as data:
            expected_sub_event = json.load(data)
        app_conf = AppConfig(**self.cbs_data_1['config'])
        actual_sub_event = self.sub_1.prepare_subscription_event(self.nf_1.nf_name, app_conf)
        self.assertEqual(expected_sub_event, actual_sub_event)

    def test_get_nf_models(self):
        for nf in [self.nf_1, self.nf_2]:
            self.sub_1.add_network_function_to_subscription(nf)
        nf_models = self.sub_1._get_nf_models()

        self.assertEqual(2, len(nf_models))
        self.assertIsInstance(nf_models[0], NetworkFunctionModel)

    def test_get_network_functions(self):
        for nf in [self.nf_1, self.nf_2]:
            self.sub_1.add_network_function_to_subscription(nf)
        nfs = self.sub_1.get_network_functions()

        self.assertEqual(2, len(nfs))
        self.assertIsInstance(nfs[0], NetworkFunction)
