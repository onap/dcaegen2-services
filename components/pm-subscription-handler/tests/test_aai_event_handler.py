# ============LICENSE_START===================================================
#  Copyright (C) 2020-2021 Nordix Foundation.
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
import copy
import json
from os import path
from unittest.mock import patch, MagicMock
from mod.aai_event_handler import AAIEventHandler
from mod.api.db_models import NetworkFunctionModel, NetworkFunctionFilterModel, \
    MeasurementGroupModel, SubscriptionModel
from mod.subscription import AdministrativeState
from tests.base_setup import BaseClassSetup
from mod import db


class AAIEventHandlerTest(BaseClassSetup):
    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    def setUp(self):
        super().setUp()
        super().setUpAppConf()
        with open(path.join(path.dirname(__file__), 'data/mr_aai_events.json'), 'r') as data:
            self.mr_aai_events = json.load(data)["mr_response"]

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

    @patch('mod.pmsh_config.AppConfig.get_from_topic')
    @patch('mod.aai_event_handler.NetworkFunction.delete')
    def test_process_aai_delete_events(self, mock_nf_delete, mr_aai_mock):
        mr_aai_mock.return_value = self.mr_aai_events
        aai_handler = AAIEventHandler(self.app)
        network_function = NetworkFunctionModel(
            nf_name="pnf_to_be_deleted1",
            ipv4_address='204.120.0.15',
            ipv6_address='2001:db8:3333:4444:5555:6666:7777:8888',
            model_invariant_id='123',
            model_version_id='234',
            model_name='pnf',
            sdnc_model_name='p-node',
            sdnc_model_version='v1')
        db.session.add(network_function)
        network_function2 = copy.deepcopy(network_function)
        network_function2.nf_name = "pnf_to_be_deleted2"
        db.session.add(network_function2)
        db.session.commit()
        aai_handler.execute()
        self.assertEqual(mock_nf_delete.call_count, 2)

    @patch('mod.aai_event_handler.AAIEventHandler.apply_nfs_to_subscriptions')
    @patch('mod.network_function.NetworkFunction.set_nf_model_params')
    @patch('mod.pmsh_config.AppConfig.get_from_topic')
    def test_process_aai_update_events(self, mr_aai_mock, mock_set_sdnc_params, mock_apply_nfs):
        mock_set_sdnc_params.return_value = True
        mr_aai_mock.return_value = self.mr_aai_events
        aai_handler = AAIEventHandler(self.app)
        aai_handler.execute()
        self.assertEqual(mock_apply_nfs.call_count, 1)

    @patch('mod.pmsh_config.AppConfig.publish_to_topic', MagicMock(return_value=None))
    @patch('mod.api.services.subscription_service.apply_measurement_grp_to_nfs')
    @patch('mod.network_function.NetworkFunction.set_nf_model_params')
    @patch('mod.pmsh_config.AppConfig.get_from_topic')
    def test_process_aai_apply_nfs_to_subscriptions(self, mr_aai_mock, mock_set_sdnc_params,
                                                    apply_nfs_to_measure_grp):
        mock_set_sdnc_params.return_value = True
        mr_aai_mock.return_value = self.mr_aai_events
        aai_handler = AAIEventHandler(self.app)
        subscription = SubscriptionModel(subscription_name='ExtraPM-All-gNB-R2B2',
                                         operational_policy_name='operation_policy',
                                         control_loop_name="control-loop",
                                         status=AdministrativeState.UNLOCKED.value)
        db.session.add(subscription)
        db.session.commit()
        generate_nf_filter_measure_grp('ExtraPM-All-gNB-R2B', 'msr_grp_name')
        generate_nf_filter_measure_grp('ExtraPM-All-gNB-R2B2', 'msr_grp_name2')
        db.session.commit()
        aai_handler.execute()
        self.assertEqual(apply_nfs_to_measure_grp.call_count, 2)

    @patch('mod.pmsh_config.AppConfig.publish_to_topic', MagicMock(return_value=None))
    @patch('mod.api.services.subscription_service.apply_measurement_grp_to_nfs')
    @patch('mod.network_function.NetworkFunction.set_nf_model_params')
    @patch('mod.logger.error')
    @patch('mod.pmsh_config.AppConfig.get_from_topic')
    def test_process_aai_apply_msg_failure(self, mr_aai_mock, mock_logger, mock_set_sdnc_params,
                                           apply_nfs_to_measure_grp):
        mock_set_sdnc_params.return_value = True
        mr_aai_mock.return_value = self.mr_aai_events
        apply_nfs_to_measure_grp.side_effect = Exception("publish failed")
        aai_handler = AAIEventHandler(self.app)
        generate_nf_filter_measure_grp('ExtraPM-All-gNB-R2B', 'msr_grp_name3')
        db.session.commit()
        aai_handler.execute()
        mock_logger.assert_called_with('Failed to process AAI event for '
                                       'subscription: ExtraPM-All-gNB-R2B '
                                       'due to: publish failed')

    @patch('mod.pmsh_config.AppConfig.publish_to_topic', MagicMock(return_value=None))
    @patch('mod.network_function.NetworkFunction.set_nf_model_params')
    @patch('mod.logger.error')
    @patch('mod.pmsh_config.AppConfig.get_from_topic')
    def test_process_aai__failure(self, mr_aai_mock, mock_logger, mock_set_sdnc_params):
        mock_set_sdnc_params.return_value = True
        mr_aai_mock.side_effect = Exception("AAI failure")
        aai_handler = AAIEventHandler(self.app)
        generate_nf_filter_measure_grp('ExtraPM-All-gNB-R2B', 'msr_grp_name3')
        db.session.commit()
        aai_handler.execute()
        mock_logger.assert_called_with('Failed to process AAI event due to: AAI failure')


def generate_nf_filter_measure_grp(sub_name, msg_name):
    nf_filter = NetworkFunctionFilterModel(
        subscription_name=sub_name, nf_names='{^pnf.*, ^vnf.*}',
        model_invariant_ids='{}',
        model_version_ids='{}',
        model_names='{}')
    measurement_group = MeasurementGroupModel(
        subscription_name=sub_name, measurement_group_name=msg_name,
        administrative_state='UNLOCKED', file_based_gp=15, file_location='pm.xml',
        measurement_type=[], managed_object_dns_basic=[])
    db.session.add(nf_filter)
    db.session.add(measurement_group)
