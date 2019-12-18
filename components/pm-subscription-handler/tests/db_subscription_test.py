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
from os import environ

from flask_sqlalchemy import sqlalchemy

import pmsh_service.mod.db_config as db_config
from pmsh_service.mod import subscriptions
from pmsh_service.mod.db_config import app as app


class SubscriptionTest(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        test_db_uri = 'sqlite:///pmsubscription.db'
        app.config['SQLALCHEMY_DATABASE_URI'] = test_db_uri
        cls.db_conn = sqlalchemy.create_engine(test_db_uri)
        cls.active = 'active'

    def setUp(self):
        db_config.db.create_all()

    def tearDown(self):
        db_config.db.drop_all()

    def test_get_subscription(self):
        sub_name = 'sub1'
        subscriptions.create(sub_name, self.active)
        sub = subscriptions.get(sub_name)

        self.assertEqual(sub_name, sub.subscription_name)

    def test_get_subscription_no_match(self):
        subscriptions.create('sub1_exists', self.active)
        sub_name = 'sub2_does_not_exist'
        sub = subscriptions.get(sub_name)
        self.assertEqual(sub, None)

    def test_get_subscriptions(self):
        subscriptions.create('sub1', self.active)
        subscriptions.create('sub2', self.active)
        subs = subscriptions.get_all()

        self.assertEqual(2, len(subs))

    def test_create_existing_subscription(self):
        sub1 = subscriptions.create('sub1', self.active)
        same_sub1 = subscriptions.create('sub1', self.active)

        self.assertEqual(sub1, same_sub1)
        self.assertEqual(1, len(subscriptions.get_all()))
