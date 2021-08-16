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

from mod.network_function import NetworkFunction
from tests.base_setup import BaseClassSetup


class NetworkFunctionTests(BaseClassSetup):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()

    def setUp(self):
        super().setUp()
        self.nf_1 = NetworkFunction(sdnc_model_name='blah', sdnc_model_version=1.0,
                                    **{'nf_name': 'pnf_1',
                                       'ipv4_address': '204.120.0.15',
                                       'ipv6_address': '2001:db8:3333:4444:5555:6666:7777:8888',
                                       'model_invariant_id': 'some_id',
                                       'model_version_id': 'some_other_id'})
        self.nf_2 = NetworkFunction(sdnc_model_name='blah', sdnc_model_version=2.0,
                                    **{'nf_name': 'pnf_2',
                                       'ipv4_address': '204.120.0.15',
                                       'ipv6_address': '2001:db8:3333:4444:5555:6666:7777:8888',
                                       'model_invariant_id': 'some_id',
                                       'model_version_id': 'some_other_id'})
        with open(os.path.join(os.path.dirname(__file__), 'data/aai_model_info.json'), 'r') as data:
            self.good_model_info = json.loads(data.read())
        with open(os.path.join(os.path.dirname(__file__),
                               'data/aai_model_info_no_sdnc.json'), 'r') as data:
            self.bad_model_info = json.loads(data.read())

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls):
        super().tearDownClass()

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
        for nf in [self.nf_1, self.nf_2]:
            self.app_conf.subscription.add_network_function_to_subscription(nf, Mock())
        nfs = NetworkFunction.get_all()
        self.assertEqual(2, len(nfs))
        NetworkFunction.delete(nf_name=self.nf_1.nf_name)
        nfs = NetworkFunction.get_all()
        self.assertEqual(1, len(nfs))

    @patch('mod.aai_client.get_aai_model_data')
    def test_set_sdnc_params_true(self, mock_get_aai_model):
        mock_get_aai_model.return_value = self.good_model_info
        self.assertTrue(self.nf_1.set_nf_model_params(self.app_conf))

    @patch('mod.aai_client.get_aai_model_data')
    def test_set_sdnc_params_false(self, mock_get_aai_model):
        mock_get_aai_model.return_value = self.bad_model_info
        self.assertFalse(self.nf_1.set_nf_model_params(self.app_conf))
