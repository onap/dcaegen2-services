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
from parameterized import parameterized
from mod.network_function import NetworkFunctionFilter


def custom_name_func(testcase_func, param_num, param):
    return "%s_%s" % (
        testcase_func.__name__,
        parameterized.to_safe_name((str(param.args[0]))),
    )


def load_test_cases():
    test_parameters = []
    with open(os.path.join(os.path.dirname(__file__), 'data/filter_test_data.json'), 'r') as test_data:
        loaded_test_data = json.load(test_data)
    for test in loaded_test_data:
        params = [value for key, value in test.items()]
        test_parameters.append(params)
    return test_parameters


class NetworkFunctionFilterTest(TestCase):

    @parameterized.expand(load_test_cases, name_func=custom_name_func)
    def test(self, test_name, nf_filter, nf_name, model_invariant_uuid, model_version_id, orchestration_status,
             expected_result):
        nf_filter = NetworkFunctionFilter(**nf_filter)
        self.assertEqual(nf_filter.is_nf_in_filter(nf_name,
                                                   model_invariant_uuid,
                                                   model_version_id,
                                                   orchestration_status), expected_result)

    def test_filter_true_on_multiple_modelInvariantUUIDs(self):
        nf_filter = NetworkFunctionFilter(**{
            "nfNames": [
            ],
            "modelInvariantUUIDs": [
                '5845y423-g654-6fju-po78-8n53154532k6',
                '7129e420-d396-4efb-af02-6b83499b12f8'
            ],
            "modelVersionIDs": [
            ]
        })
        self.assertTrue(nf_filter.is_nf_in_filter('pnf1',
                                                  '7129e420-d396-4efb-af02-6b83499b12f8',
                                                  'e80a6ae3-cafd-4d24-850d-e14c084a5ca9',
                                                  'Active'))

    def test_filter_false_on_modelInvariantUUIDs_being_false_and_pnfname_being_true(self):
        nf_filter = NetworkFunctionFilter(**{
            "nfNames": [
                "^pnf.*",
                "^vnf.*"
            ],
            "modelInvariantUUIDs": [
                '5845y423-g654-6fju-po78-8n53154532k6',
                '7129e420-d396-4efb-af02-6b83499b12f8'
            ],
            "modelVersionIDs": [
            ]
        })
        self.assertFalse(nf_filter.is_nf_in_filter('pnf1',
                                                   'WrongModelInvariantUUID',
                                                   'e80a6ae3-cafd-4d24-850d-e14c084a5ca9',
                                                   'Active'))