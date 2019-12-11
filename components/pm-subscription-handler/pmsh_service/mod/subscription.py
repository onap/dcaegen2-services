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
import re


class Subscription:
    def __init__(self, **kwargs):
        self.subscriptionName = kwargs.get('subscriptionName')
        self.administrativeState = kwargs.get('administrativeState')
        self.fileBasedGP = kwargs.get('fileBasedGP')
        self.fileLocation = kwargs.get('fileLocation')
        self.nfFilter = kwargs.get('nfFilter')
        self.measurementGroups = kwargs.get('measurementGroups')

    def apply_filter_to_xnf(self, xnf_name):
        """Match the xnf name against regex values in nfFilter.nfNames

        Args:
            xnf_name: the AAI xnf name.

        Returns:
            bool: True if matched, else False.
        """
        regex_list = []
        for raw_regex in self.nfFilter['nfNames']:
            regex_list.append(re.compile(raw_regex))
        return any(regex.match(xnf_name) for regex in regex_list)
