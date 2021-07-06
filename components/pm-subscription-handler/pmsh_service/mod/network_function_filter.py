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


def filter_diff(app_conf, existing_subscription_filter):
    existing_subscription_filter, nf_filter = \
        json.dumps(existing_subscription_filter, sort_keys=True), \
        json.dumps(app_conf.subscription.nfFilter, sort_keys=True)
    return existing_subscription_filter != nf_filter

def process_nfs_for_creation_and_deletion(existing_nfs, new_nfs, mrpub, app_conf):
    """ Finds new/old nfs for creation/deletion from subscription    """
    nfs_for_deletion = [i for i in existing_nfs if i not in new_nfs]
    nfs_for_creation = [i for i in new_nfs if i not in existing_nfs]

    app_conf.subscription.create_subscription_on_nfs(nfs_for_creation, mrpub, app_conf)
    app_conf.subscription.delete_subscription_from_nfs(nfs_for_deletion, mrpub, app_conf)

