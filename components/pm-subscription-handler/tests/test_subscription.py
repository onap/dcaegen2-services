# ============LICENSE_START===================================================
#  Copyright (C) 2019 Nordix Foundation.
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
import unittest

from subscription import Subscription


class SubscriptionTestCase(unittest.TestCase):

    def setUp(self):
        with open(os.path.join(os.path.dirname(__file__), 'data/cbs_data.json'), 'r') as data:
            self.cbs_data = json.load(data)
        self.sub = Subscription(**self.cbs_data['policy']['subscription'])

    def test_sub_filter_true(self):
        self.assertTrue(self.sub.is_xnf_in_filter('pnf1'))

    def test_sub_filter_false(self):
        self.assertFalse(self.sub.is_xnf_in_filter('PNF-33'))

    def test_sub_measurement_group(self):
        self.assertEqual(len(self.sub.measurementGroups), 2)

    def test_sub_file_location(self):
        self.assertEqual(self.sub.fileLocation, '/pm/pm.xml')
