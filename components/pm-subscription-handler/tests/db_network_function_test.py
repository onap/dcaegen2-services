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

from sqlalchemy import create_engine

import pmsh_service.mod.db_config as db_config
from pmsh_service.mod import network_functions
from pmsh_service.mod.db_config import app as app


class NetworkFunctionTests(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        db_url = 'sqlite:///pmsubscription.db'
        app.config['SQLALCHEMY_DATABASE_URI'] = db_url
        cls.db_conn = create_engine(db_url)

    def setUp(self):
        db_config.db.create_all()

    def tearDown(self):
        db_config.db.drop_all()

    def test_get_network_function(self):
        nf_name = 'nf1'
        network_functions.create(nf_name)
        nf = network_functions.get(nf_name)

        self.assertEqual(nf_name, nf.nf_name)

    def test_get_network_function_no_match(self):
        network_functions.create('nf1_exists')
        nf_name = 'nf2_does_not_exist'
        nf = network_functions.get(nf_name)
        self.assertEqual(nf, None)

    def test_get_network_functions(self):
        network_functions.create('sub1')
        network_functions.create('sub2')
        nfs = network_functions.get_all()

        self.assertEqual(2, len(nfs))

    def test_create_existing_network_function(self):
        nf = network_functions.create('nf1')
        same_nf = network_functions.create('nf1')

        self.assertEqual(nf, same_nf)
