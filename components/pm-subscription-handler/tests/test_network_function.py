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
import unittest

from mod import db, create_app
from mod.network_function import NetworkFunction


class NetworkFunctionTests(unittest.TestCase):

    def setUp(self):
        self.nf_1 = NetworkFunction(nf_name='pnf_1', orchestration_status='Inventoried')
        self.nf_2 = NetworkFunction(nf_name='pnf_2', orchestration_status='Active')
        self.app = create_app('testing')
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
