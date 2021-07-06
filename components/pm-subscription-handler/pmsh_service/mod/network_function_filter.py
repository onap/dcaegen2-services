# ============LICENSE_START===================================================
#  Copyright (C) 2021 Nordix Foundation.
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
import re

import mod


class NetworkFunctionFilter:
    def __init__(self, **kwargs):
        self.nf_names = kwargs.get('nfNames')
        self.model_invariant_ids = kwargs.get('modelInvariantIDs')
        self.model_version_ids = kwargs.get('modelVersionIDs')
        self.model_names = kwargs.get('modelNames')
        self.regex_matcher = re.compile('|'.join(raw_regex for raw_regex in self.nf_names))


    def is_nf_in_filter(self, nf):
        """Match the nf fields against values in Subscription.nfFilter

        Args:
            nf (NetworkFunction): The NF to be filtered.

        Returns:
            bool: True if matched, else False.
        """
        match = True
        if self.nf_names and self.regex_matcher.search(nf.nf_name) is None:
            match = False
        if self.model_invariant_ids and nf.model_invariant_id not in self.model_invariant_ids:
            match = False
        if self.model_version_ids and nf.model_version_id not in self.model_version_ids:
            match = False
        if self.model_names and nf.model_name not in self.model_names:
            match = False
        return match


def filter_diff(self, existing_subscription_filter):
    existing_subscription_filter, nf_filter = \
        json.dumps(existing_subscription_filter, sort_keys=True), \
        json.dumps(self.nfFilter, sort_keys=True)
    return existing_subscription_filter != nf_filter

def get_nfs_for_creation_and_deletion(existing_nfs, new_nfs, action, mrpub, app_conf):
    """ Finds new/old nfs for creation/deletion from subscription    """
    for existing_nf in existing_nfs:
        _compare_nfs(action, app_conf, existing_nf, mrpub, new_nfs)

def _compare_nfs(action, app_conf, existing_nf, mrpub, new_nfs):
    """ Compares old nfs list to existing nfs list"""
    for new_nf in new_nfs:
        if existing_nf.nf_name != new_nf.nf_name:
            mod.subscription._apply_action_to_nfs(action, app_conf, existing_nf, mrpub, new_nf)