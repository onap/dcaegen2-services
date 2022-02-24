# ============LICENSE_START===================================================
#  Copyright (C) 2019-2022 Nordix Foundation.
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
from unittest.mock import patch, MagicMock
from http import HTTPStatus

from mod import aai_client, db
from mod.api.controller import status, post_subscription, get_subscription_by_name, \
    get_subscriptions, get_meas_group_with_nfs, delete_subscription_by_name, update_admin_state, \
    delete_meas_group_by_name, put_nf_filter
from mod.api.services.measurement_group_service import query_meas_group_by_name
from tests.base_setup import BaseClassSetup
from mod.api.custom_exception import InvalidDataException, DataConflictException
from mod.api.db_models import SubscriptionModel, NfMeasureGroupRelationalModel
from mod.subscription import SubNfState
from mod.network_function import NetworkFunctionFilter
from tests.base_setup import create_subscription_data, create_multiple_subscription_data, \
    create_multiple_network_function_data
from mod.api.services import measurement_group_service, nf_service, subscription_service


class ControllerTestCase(BaseClassSetup):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    def setUp(self):
        super().setUp()
        super().setUpAppConf()
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_xnfs.json'), 'r') as data:
            self.aai_response_data = data.read()
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_model_info.json'), 'r') as data:
            self.good_model_info = data.read()
        with open(os.path.join(os.path.dirname(__file__),
                               'data/create_subscription_request.json'), 'r') as data:
            self.subscription_request = data.read()

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

    def test_status_response_healthy(self):
        self.assertEqual(status()['status'], 'healthy')

    def create_test_subs(self, new_sub_name, new_msrmt_grp_name):
        subscription = self.subscription_request.replace('ExtraPM-All-gNB-R2B', new_sub_name)
        subscription = subscription.replace('msrmt_grp_name', new_msrmt_grp_name)
        return subscription

    @patch('mod.api.services.subscription_service.save_nf_filter', MagicMock(return_value=None))
    @patch('mod.pmsh_config.AppConfig.publish_to_topic', MagicMock(return_value=None))
    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_post_subscription(self, mock_filter_call, mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-post', 'msrmt_grp_name-post')
        subscription = json.loads(subscription)
        mock_filter_call.return_value = NetworkFunctionFilter(
            **subscription['subscription']["nfFilter"])
        sub_name = subscription['subscription']['subscriptionName']
        mes_grp = subscription['subscription']['measurementGroups'][0]['measurementGroup']
        mes_grp_name = mes_grp['measurementGroupName']
        response = post_subscription(subscription)
        subscription = (SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == sub_name).one_or_none())
        self.assertIsNotNone(subscription)
        msr_grp_nf_rel = (NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == mes_grp_name)).all()
        for published_event in msr_grp_nf_rel:
            self.assertEqual(published_event.nf_measure_grp_status,
                             SubNfState.PENDING_CREATE.value)
        self.assertEqual(response[1], 201)

    def test_post_subscription_duplicate_sub(self):
        # Posting the same subscription request stored in previous test to get duplicate response
        response = post_subscription(json.loads(self.subscription_request))
        self.assertEqual(response[1], 409)
        self.assertEqual(response[0], 'subscription Name: ExtraPM-All-gNB-R2B already exists.')

    def test_post_subscription_invalid_filter(self):
        subscription = self.create_test_subs('xtraPM-All-gNB-R2B-invalid', 'msrmt_grp_name-invalid')
        subscription = json.loads(subscription)
        subscription['subscription']['nfFilter']['nfNames'] = []
        subscription['subscription']['nfFilter']['modelInvariantIDs'] = []
        subscription['subscription']['nfFilter']['modelVersionIDs'] = []
        subscription['subscription']['nfFilter']['modelNames'] = []
        response = post_subscription(subscription)
        self.assertEqual(response[1], 400)
        self.assertEqual(response[0], 'At least one filter within nfFilter must not be empty')

    def test_post_subscription_missing(self):
        subscription = json.loads(self.subscription_request)
        subscription['subscription']['subscriptionName'] = ''
        response = post_subscription(subscription)
        self.assertEqual(response[1], 400)
        self.assertEqual(response[0], 'No value provided in subscription name')

    @patch('mod.api.services.subscription_service.query_subscription_by_name',
           MagicMock(return_value=create_subscription_data('sub_demo')))
    def test_get_subscription_by_name_api(self):
        sub, status_code = get_subscription_by_name('sub_demo')
        self.assertEqual(status_code, HTTPStatus.OK.value)
        self.assertEqual(sub['subscription']['subscriptionName'], 'sub_demo')
        self.assertEqual(sub['subscription']['nfFilter']['nfNames'],
                         ['^pnf.*', '^vnf.*'])
        self.assertEqual(sub['subscription']['controlLoopName'],
                         'pmsh_control_loop_name')
        self.assertEqual(len(sub['subscription']['measurementGroups']), 2)
        self.assertEqual(sub['subscription']['operationalPolicyName'],
                         'pmsh_operational_policy')

    @patch('mod.api.services.subscription_service.query_subscription_by_name',
           MagicMock(return_value=None))
    def test_get_subscription_by_name_api_none(self):
        sub, status_code = get_subscription_by_name('sub_demo')
        self.assertEqual(status_code, HTTPStatus.NOT_FOUND.value)
        self.assertEqual(sub['error'],
                         'Subscription was not defined with the name : sub_demo')

    @patch('mod.api.services.subscription_service.query_subscription_by_name',
           MagicMock(side_effect=Exception('something failed')))
    def test_get_subscription_by_name_api_exception(self):
        sub, status_code = get_subscription_by_name('sub_demo')
        self.assertEqual(status_code, HTTPStatus.INTERNAL_SERVER_ERROR.value)

    @patch('mod.api.services.subscription_service.query_all_subscriptions',
           MagicMock(return_value=create_multiple_subscription_data(
               ['sub_demo_one', 'sub_demo_two'])))
    def test_get_subscriptions_api(self):
        subs, status_code = get_subscriptions()
        self.assertEqual(status_code, HTTPStatus.OK.value)
        self.assertEqual(subs[0]['subscription']['subscriptionName'], 'sub_demo_one')
        self.assertEqual(subs[1]['subscription']['subscriptionName'], 'sub_demo_two')
        self.assertEqual(subs[1]['subscription']['measurementGroups'][0]['measurementGroup']
                         ['measurementGroupName'], 'MG1')
        self.assertEqual(len(subs[1]['subscription']['measurementGroups']), 2)
        self.assertEqual(subs[0]['subscription']['nfs'][0], 'pnf_101')
        self.assertEqual(subs[0]['subscription']['nfs'][1], 'pnf_102')
        self.assertEqual(subs[1]['subscription']['nfs'], [])
        self.assertEqual(len(subs), 2)

    @patch('mod.api.services.subscription_service.query_all_subscriptions',
           MagicMock(return_value=None))
    def test_get_subscriptions_api_none(self):
        subs, status_code = get_subscriptions()
        self.assertEqual(status_code, HTTPStatus.OK.value)
        self.assertEqual(subs, [])

    @patch('mod.api.services.subscription_service.query_all_subscriptions',
           MagicMock(side_effect=Exception('something failed')))
    def test_get_subscriptions_api_exception(self):
        subs, status_code = get_subscriptions()
        self.assertEqual(status_code, HTTPStatus.INTERNAL_SERVER_ERROR.value)

    def test_get_meas_group_with_nfs_api(self):
        sub = create_subscription_data('sub1')
        nf_list = create_multiple_network_function_data(['pnf101', 'pnf102'])
        measurement_group_service.save_measurement_group(sub.measurement_groups[0].
                                                         serialize()['measurementGroup'],
                                                         sub.subscription_name)
        for nf in nf_list:
            nf_service.save_nf(nf)
            measurement_group_service. \
                apply_nf_status_to_measurement_group(nf.nf_name, sub.measurement_groups[0].
                                                     measurement_group_name,
                                                     SubNfState.PENDING_CREATE.value)
        db.session.commit()
        mg_with_nfs, status_code = get_meas_group_with_nfs('sub1', 'MG1')
        self.assertEqual(status_code, HTTPStatus.OK.value)
        self.assertEqual(mg_with_nfs['subscriptionName'], 'sub1')
        self.assertEqual(mg_with_nfs['measurementGroupName'], 'MG1')
        self.assertEqual(mg_with_nfs['administrativeState'], 'UNLOCKED')
        self.assertEqual(len(mg_with_nfs['networkFunctions']), 2)

    def test_get_meas_group_with_nfs_api_none(self):
        error, status_code = get_meas_group_with_nfs('sub1', 'MG1')
        self.assertEqual(error['error'], 'measurement group was not defined with '
                                         'the sub name: sub1 and meas group name: MG1')
        self.assertEqual(status_code, HTTPStatus.NOT_FOUND.value)

    @patch('mod.api.services.measurement_group_service.query_meas_group_by_name',
           MagicMock(side_effect=Exception('something failed')))
    def test_get_meas_group_with_nfs_api_exception(self):
        error, status_code = get_meas_group_with_nfs('sub1', 'MG1')
        self.assertEqual(status_code, HTTPStatus.INTERNAL_SERVER_ERROR.value)

    def test_delete_sub_when_state_unlocked(self):
        subscription_unlocked_data = create_subscription_data('MG_unlocked')
        subscription_unlocked_data.measurement_groups[0].measurement_group_name = 'unlock'
        subscription_unlocked_data.measurement_groups[0].administrative_state = 'UNLOCKED'
        db.session.add(subscription_unlocked_data)
        db.session.add(subscription_unlocked_data.measurement_groups[0])
        db.session.commit()
        db.session.remove()
        message, status_code = delete_subscription_by_name('MG_unlocked')
        self.assertEqual(status_code, HTTPStatus.CONFLICT.value)
        self.assertEqual(subscription_service.query_subscription_by_name('MG_unlocked')
                         .subscription_name, 'MG_unlocked')

    def test_delete_mg_when_state_unlocked(self):
        subscription_unlocked_data = create_subscription_data('MG_unlocked')
        db.session.add(subscription_unlocked_data)
        db.session.commit()
        db.session.remove()
        message, status_code = delete_meas_group_by_name('MG_unlocked', 'MG1')
        self.assertEqual(status_code, HTTPStatus.CONFLICT.value)
        self.assertEqual(query_meas_group_by_name('MG_unlocked', 'MG1').measurement_group_name,
                         'MG1')

    def test_delete_sub_when_state_locked(self):
        subscription_unlocked_data = create_subscription_data('MG_locked')
        subscription_unlocked_data.measurement_groups[0].measurement_group_name = 'lock'
        subscription_unlocked_data.measurement_groups[0].administrative_state = 'LOCKED'
        db.session.add(subscription_unlocked_data)
        db.session.add(subscription_unlocked_data.measurement_groups[0])
        db.session.commit()
        db.session.remove()
        none_type, status_code = delete_subscription_by_name('MG_locked')
        self.assertEqual(none_type, None)
        self.assertEqual(status_code, HTTPStatus.NO_CONTENT.value)
        self.assertEqual(subscription_service.query_subscription_by_name('MG_locked'), None)

    def test_delete_mg_when_state_locked(self):
        subscription_locked_data = create_subscription_data('MG_locked')
        subscription_locked_data.measurement_groups[0].administrative_state = 'LOCKED'
        db.session.add(subscription_locked_data)
        db.session.add(subscription_locked_data.measurement_groups[0])
        db.session.commit()
        db.session.remove()
        non_type, status_code = delete_meas_group_by_name('MG_locked', 'MG1')
        self.assertEqual(non_type, None)
        self.assertEqual(status_code, HTTPStatus.NO_CONTENT.value)
        self.assertEqual(query_meas_group_by_name('MG_locked', 'MG1'), None)

    def test_delete_sub_when_state_locking(self):
        subscription_locking_data = create_subscription_data('MG_locking')
        subscription_locking_data.measurement_groups[0].measurement_group_name = 'locking'
        subscription_locking_data.measurement_groups[0].administrative_state = 'LOCKING'
        db.session.add(subscription_locking_data)
        db.session.add(subscription_locking_data.measurement_groups[0])
        db.session.commit()
        db.session.remove()
        message, status_code = delete_subscription_by_name('MG_locking')
        self.assertEqual(status_code, HTTPStatus.CONFLICT.value)
        self.assertEqual(subscription_service.query_subscription_by_name('MG_locking')
                         .subscription_name, 'MG_locking')

    def test_delete_mg_when_state_locking(self):
        subscription_locking_data = create_subscription_data('MG_locking')
        subscription_locking_data.measurement_groups[0].administrative_state = 'LOCKING'
        db.session.add(subscription_locking_data)
        db.session.add(subscription_locking_data.measurement_groups[0])
        db.session.commit()
        db.session.remove()
        message, status_code = delete_meas_group_by_name('MG_locking', 'MG1')
        self.assertEqual(status_code, HTTPStatus.CONFLICT.value)
        self.assertEqual(query_meas_group_by_name('MG_locking', 'MG1').measurement_group_name,
                         'MG1')

    def test_delete_sub_none(self):
        message, status_code = delete_subscription_by_name('None')
        self.assertEqual(message['error'], 'Subscription is not defined with name None')
        self.assertEqual(status_code, HTTPStatus.NOT_FOUND.value)

    def test_delete_mg_exception(self):
        subscription_locking_data = create_subscription_data('MG_locking')
        message, status_code = delete_meas_group_by_name(subscription_locking_data, 'None')
        self.assertEqual(status_code, HTTPStatus.INTERNAL_SERVER_ERROR.value)

    @patch('mod.api.services.subscription_service.query_to_delete_subscription_by_name',
           MagicMock(side_effect=Exception('something failed')))
    def test_delete_sub_exception(self):
        error, status_code = delete_subscription_by_name('None')
        self.assertEqual(status_code, HTTPStatus.INTERNAL_SERVER_ERROR.value)

    @patch('mod.pmsh_config.AppConfig.publish_to_topic', MagicMock(return_value=None))
    def test_update_admin_state_api_for_locked_update(self):
        sub = create_subscription_data('sub1')
        nf_list = create_multiple_network_function_data(['pnf_101', 'pnf_102'])
        db.session.add(sub)
        for nf in nf_list:
            nf_service.save_nf(nf)
            measurement_group_service. \
                apply_nf_status_to_measurement_group(nf.nf_name, sub.measurement_groups[0].
                                                     measurement_group_name,
                                                     SubNfState.CREATED.value)
        db.session.commit()
        response = update_admin_state('sub1', 'MG1', {'administrativeState': 'LOCKED'})
        self.assertEqual(response[1], HTTPStatus.OK.value)
        self.assertEqual(response[0], 'Successfully updated admin state')
        mg_with_nfs, status_code = get_meas_group_with_nfs('sub1', 'MG1')
        self.assertEqual(mg_with_nfs['subscriptionName'], 'sub1')
        self.assertEqual(mg_with_nfs['measurementGroupName'], 'MG1')
        self.assertEqual(mg_with_nfs['administrativeState'], 'LOCKING')
        for nf in mg_with_nfs['networkFunctions']:
            self.assertEqual(nf['nfMgStatus'], SubNfState.PENDING_DELETE.value)

    @patch('mod.pmsh_config.AppConfig.publish_to_topic', MagicMock(return_value=None))
    @patch.object(aai_client, '_get_all_aai_nf_data')
    @patch.object(aai_client, 'get_aai_model_data')
    @patch.object(NetworkFunctionFilter, 'get_network_function_filter')
    def test_update_admin_state_api_for_unlocked_update(self, mock_filter_call,
                                                        mock_model_aai, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        mock_model_aai.return_value = json.loads(self.good_model_info)
        sub = create_subscription_data('sub1')
        db.session.add(sub)
        nf_list = create_multiple_network_function_data(['pnf_101', 'pnf_102'])
        for network_function in nf_list:
            db.session.add(network_function)
        db.session.commit()
        mock_filter_call.return_value = NetworkFunctionFilter.get_network_function_filter('sub')
        response = update_admin_state('sub1', 'MG2', {'administrativeState': 'UNLOCKED'})
        self.assertEqual(response[1], HTTPStatus.OK.value)
        self.assertEqual(response[0], 'Successfully updated admin state')
        mg_with_nfs, status_code = get_meas_group_with_nfs('sub1', 'MG2')
        self.assertEqual(mg_with_nfs['subscriptionName'], 'sub1')
        self.assertEqual(mg_with_nfs['measurementGroupName'], 'MG2')
        self.assertEqual(mg_with_nfs['administrativeState'], 'UNLOCKED')
        for nf in mg_with_nfs['networkFunctions']:
            self.assertEqual(nf['nfMgStatus'], SubNfState.PENDING_CREATE.value)

    @patch('mod.api.services.measurement_group_service.update_admin_status',
           MagicMock(side_effect=InvalidDataException('Bad request')))
    def test_update_admin_state_api_invalid_data_exception(self):
        error, status_code = update_admin_state('sub4', 'MG2',
                                                {'administrativeState': 'UNLOCKED'})
        self.assertEqual(status_code, HTTPStatus.BAD_REQUEST.value)
        self.assertEqual(error, 'Bad request')

    @patch('mod.api.services.measurement_group_service.update_admin_status',
           MagicMock(side_effect=DataConflictException('Data conflict')))
    def test_update_admin_state_api_data_conflict_exception(self):
        error, status_code = update_admin_state('sub4', 'MG2',
                                                {'administrativeState': 'UNLOCKED'})
        self.assertEqual(status_code, HTTPStatus.CONFLICT.value)
        self.assertEqual(error, 'Data conflict')

    @patch('mod.api.services.measurement_group_service.update_admin_status',
           MagicMock(side_effect=Exception('Server Error')))
    def test_update_admin_state_api_exception(self):
        error, status_code = update_admin_state('sub4', 'MG2',
                                                {'administrativeState': 'UNLOCKED'})
        self.assertEqual(status_code, HTTPStatus.INTERNAL_SERVER_ERROR.value)
        self.assertEqual(error, 'Update admin status request was not processed for sub name: sub4 '
                                'and meas group name: MG2 due to Exception : Server Error')

    @patch('mod.api.services.subscription_service.update_filter', MagicMock(return_value=None))
    def test_put_nf_filter(self):
        response = put_nf_filter('sub1', json.loads('{"nfNames": ["^pnf.*", "^vnf.*"]}'))
        self.assertEqual(response[0], 'Successfully updated network function Filter')
        self.assertEqual(response[1], HTTPStatus.OK.value)

    @patch('mod.api.services.subscription_service.update_filter',
           MagicMock(side_effect=InvalidDataException('Bad request')))
    def test_put_nf_filter_api_invalid_data_exception(self):
        error, status_code = put_nf_filter('sub1',
                                           json.loads('{"nfNames": ["^pnf.*", "^vnf.*"]}'))
        self.assertEqual(status_code, HTTPStatus.BAD_REQUEST.value)
        self.assertEqual(error, 'Bad request')

    @patch('mod.api.services.subscription_service.update_filter',
           MagicMock(side_effect=DataConflictException('Data conflict')))
    def test_put_nf_filter_api_data_conflict_exception(self):
        error, status_code = put_nf_filter('sub1',
                                           json.loads('{"nfNames": ["^pnf.*", "^vnf.*"]}'))
        self.assertEqual(status_code, HTTPStatus.CONFLICT.value)
        self.assertEqual(error, 'Data conflict')

    @patch('mod.api.services.subscription_service.update_filter',
           MagicMock(side_effect=Exception('Server Error')))
    def test_put_nf_filter_api_exception(self):
        error, status_code = put_nf_filter('sub1', json.loads('{"nfNames": ["^pnf.*", "^vnf.*"]}'))
        self.assertEqual(status_code, HTTPStatus.INTERNAL_SERVER_ERROR.value)
        self.assertEqual(error, 'Update nf filter request was not processed for sub name: sub1 '
                                'due to Exception : Server Error')
