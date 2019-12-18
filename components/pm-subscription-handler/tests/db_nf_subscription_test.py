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
from pmsh_service.mod.db_config import app as app
from pmsh_service.mod import nf_subscription


class NetworkFunctionSubscriptionTests(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        db_url = 'sqlite:///pmsubscription.db'
        app.config['SQLALCHEMY_DATABASE_URI'] = db_url
        cls.db_conn = create_engine(db_url)
        cls.nf1 = {'nf_name': 'nf1', 'status': 'inactive'}
        cls.nf2 = {'nf_name': 'nf2', 'status': 'inactive'}

    def setUp(self):
        db_config.db.create_all()

    def tearDown(self):
        db_config.db.drop_all()

    def test_get_nfs_per_subscription(self):
        nf_array = [self.nf1, self.nf2]

        nf_subscription.add_network_functions_to_subscription('sub1', 'active', nf_array)
        nf_subscription.add_network_functions_to_subscription('sub2', 'active', nf_array)
        nfs_for_sub_1 = nf_subscription.get_nfs_per_subscription('sub1')

        self.assertEqual(2, len(nfs_for_sub_1))

    def test_add_network_functions_per_subscription(self):
        nf_array = [self.nf1, self.nf2]

        nf_subscription.add_network_functions_to_subscription('sub1', 'active', nf_array)
        nf_subs = nf_subscription.get_nfs_per_subscription('sub1')

        self.assertEqual(2, len(nf_subs))

        new_nf_array = [{'nf_name': 'nf3', 'status': 'inactive'}]
        nf_subscription.add_network_functions_to_subscription('sub1', 'active', new_nf_array)
        nf_subs = nf_subscription.get_nfs_per_subscription('sub1')

        self.assertEqual(3, len(nf_subs))

    def test_add_duplicate_network_functions_per_subscription(self):
        sub_name = 'sub1'
        nf_array = [self.nf1]

        nf_subscription.add_network_functions_to_subscription(sub_name, 'active', nf_array)
        nf_subs = nf_subscription.get_nfs_per_subscription(sub_name)

        self.assertEqual(1, len(nf_subs))

        nf_subscription.add_network_functions_to_subscription(sub_name, 'active', nf_array)
        nf_subs = nf_subscription.get_nfs_per_subscription(sub_name)

        self.assertEqual(1, len(nf_subs))
