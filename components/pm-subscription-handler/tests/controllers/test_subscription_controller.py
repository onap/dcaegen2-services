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
from unittest.mock import patch, MagicMock

from mod import aai_client
from tests.base_setup import BaseClassSetup
from mod.api.controllers import subscription_controller
from flask import current_app


class SubscriptionControllerTestCase(BaseClassSetup):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    def setUp(self):
        super().setUp()
        current_app.config['app_config'] = self.app_conf
        with open(os.path.join(os.path.dirname(__file__),
                               '../data/create_subscription_request.json'), 'r') as data:
            self.subscription_request = data.read()
        with open(os.path.join(os.path.dirname(__file__), '../data/aai_xnfs.json'),
                  'r') as data:
            self.aai_response_data = data.read()

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

    @patch('mod.api.services.subscription_service.create_subscription',
           MagicMock(return_value=None))
    def test_post_subscription(self):
        response = subscription_controller.post_subscription(json.loads(self.subscription_request))
        self.assertEqual(response[1], 201)

    @patch.object(aai_client,
                  '_get_all_aai_nf_data')
    def test_post_subscription_duplicate_sub(self, mock_aai):
        mock_aai.return_value = json.loads(self.aai_response_data)
        response = subscription_controller.post_subscription(json.loads(self.subscription_request))
        self.assertEqual(response[1], 400)
        self.assertEqual(response[0][0], 'subscription Name: ExtraPM-All-gNB-R2B already exists.')
