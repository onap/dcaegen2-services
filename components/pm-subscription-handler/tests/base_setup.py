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
import os
from unittest import TestCase
from unittest.mock import patch, MagicMock

from mod import create_app, db
from mod.api.db_models import NetworkFunctionFilterModel, MeasurementGroupModel, SubscriptionModel
from mod.network_function import NetworkFunctionFilter
from mod.pmsh_utils import AppConfig
from mod.pmsh_config import AppConfig as NewAppConfig


def get_pmsh_config(file_path='data/cbs_data_1.json'):
    with open(os.path.join(os.path.dirname(__file__), file_path), 'r') as data:
        return json.load(data)


def subscription_data(subscription_name):
    nf_filter = NetworkFunctionFilterModel(subscription_name, '{^pnf.*,^vnf.*}',
                                           '{}', '{}', '{}')
    mg_first = MeasurementGroupModel(subscription_name, 'MG1', 'UNLOCKED', 15, '/pm/pm.xml',
                                     '[{ "measurementType": "countera" }, '
                                     '{ "measurementType": "counterb" }]',
                                     '[{ "DN":"dna"},{"DN":"dnb"}]')
    mg_second = copy.deepcopy(mg_first)
    mg_second.measurement_group_name = 'MG2'
    mg_second.administrative_state = 'LOCKED'
    mg_list = [mg_first, mg_second]
    subscription_model = SubscriptionModel(subscription_name, 'pmsh_operational_policy',
                                           'pmsh_control_loop_name', 'LOCKED')
    subscription_model.network_filter = nf_filter
    subscription_model.measurement_groups = mg_list
    return subscription_model


def subscription_multiple(subscription_names):
    subscriptions = []
    for subscription_name in subscription_names:
        subscriptions.append(subscription_data(subscription_name))
    return subscriptions


class BaseClassSetup(TestCase):
    app = None
    app_context = None

    @classmethod
    @patch('mod.get_db_connection_url', MagicMock(return_value='sqlite://'))
    @patch('mod.update_logging_config', MagicMock())
    def setUpClass(cls):
        os.environ['LOGGER_CONFIG'] = os.path.join(os.path.dirname(__file__), 'log_config.yaml')
        os.environ['LOGS_PATH'] = '.'
        cls.app = create_app()
        cls.app_context = cls.app.app_context()
        cls.app_context.push()

    @patch('mod.pmsh_utils.AppConfig._get_pmsh_config', MagicMock(return_value=get_pmsh_config()))
    def setUp(self):
        os.environ['AAI_SERVICE_PORT'] = '8443'
        db.create_all()
        self.app_conf = AppConfig()
        self.app_conf.nf_filter = NetworkFunctionFilter(**self.app_conf.subscription.nfFilter)

    @patch('mod.pmsh_config.AppConfig._get_config', MagicMock(return_value=get_pmsh_config()))
    def setUpAppConf(self):
        self.pmsh_app_conf = NewAppConfig()

    def tearDown(self):
        db.drop_all()

    @classmethod
    def tearDownClass(cls):
        db.session.remove()
