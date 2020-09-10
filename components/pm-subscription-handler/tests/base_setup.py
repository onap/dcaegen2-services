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
from unittest import TestCase
from unittest.mock import patch, MagicMock

from mod import create_app, db
from mod.pmsh_utils import AppConfig


def get_pmsh_config():
    with open(os.path.join(os.path.dirname(__file__), 'data/cbs_data_1.json'), 'r') as data:
        return json.load(data)


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

    def tearDown(self):
        db.drop_all()

    @classmethod
    def tearDownClass(cls):
        db.session.remove()
