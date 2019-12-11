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
        self.nfTypeModelInvariantId = kwargs.get('nfTypeModelInvariantId')
        self.nfFilter = kwargs.get('nfFilter')
        self.measurementGroups = kwargs.get('measurementGroups')

    def prepare_subscription_event(self, xnf_name):
        """Prepare the sub event for publishing

        Args:
            xnf_name: the AAI xnf name.

        Returns:
            dict: the Subscription event to be published.
        """
        clean_sub = {k: v for k, v in self.__dict__.items() if k != 'nfFilter'}
        clean_sub.update({'nfName': xnf_name, 'policyName': f'OP-{self.subscriptionName}'})
        return clean_sub


class XnfFilter:
    def __init__(self, **kwargs):
        self.nf_sw_version = kwargs.get('swVersions')
        self.nf_names = kwargs.get('nfNames')
        self.regex_matcher = re.compile('|'.join(raw_regex for raw_regex in self.nf_names))

    def is_xnf_in_filter(self, xnf_name):
        """Match the xnf name against regex values in Subscription.nfFilter.nfNames

        Args:
            xnf_name: the AAI xnf name.

        Returns:
            bool: True if matched, else False.
        """

        return self.regex_matcher.search(xnf_name)
