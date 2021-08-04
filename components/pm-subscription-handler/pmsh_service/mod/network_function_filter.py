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

import re
from mod import logger, aai_client
from mod.network_function import NetworkFunction
from mod.api.db_models import NetworkFunctionModel


class NetworkFunctionFilter:
    def __init__(self, **kwargs):
        self.nf_names = kwargs.get('nfNames')
        self.model_invariant_ids = kwargs.get('modelInvariantIDs')
        self.model_version_ids = kwargs.get('modelVersionIDs')
        self.model_names = kwargs.get('modelNames')
        self.regex_matcher = re.compile('|'.join(raw_regex for raw_regex in self.nf_names))

    def filter_nfs(self, nf_data, app_conf):
        """
            Returns and saves a list of filtered NetworkFunctions using the nf_filter.

            Args:
                nf_data (dict): the nf json data from AAI.

            Returns:
                NetworkFunction (list): a list of filtered NetworkFunction Objects.

            Raises:
                KeyError: if AAI data cannot be parsed.
            """
        nf_list = []
        try:
            for nf in nf_data['results']:
                if nf['properties'].get('orchestration-status') != 'Active':
                    continue
                name_identifier = 'pnf-name' if nf['node-type'] == 'pnf' else 'vnf-name'
                new_nf = NetworkFunction(
                    nf_name=nf['properties'].get(name_identifier),
                    ipv4_address=nf['properties'].get('ipaddress-v4-oam'),
                    ipv6_address=nf['properties'].get('ipaddress-v6-oam'),
                    model_invariant_id=nf['properties'].get('model-invariant-id'),
                    model_version_id=nf['properties'].get('model-version-id'))
                if self.is_nf_in_filter(new_nf) \
                        and self.is_sdnc_model_in_filter(new_nf, app_conf):
                    nf_list.append(new_nf)
        except KeyError as e:
            logger.error(f'Failed to parse AAI data: {e}', exc_info=True)
            raise
        return nf_list

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
        return match

    def is_sdnc_model_in_filter(self, new_nf, app_conf):
        """
            saves NetworkFunction model details and confirms.

            Args:
                new_nf (NetworkFunction): the network function to check.
                nf_filter (NetworkFunctionFilter): check if network function needed
            Returns:
                Boolean : true if model name satisfies

            Raises:
                KeyError: if AAI data cannot be parsed.
            """
        match = True
        network_function = NetworkFunctionModel.query.filter(
            NetworkFunctionModel.nf_name == new_nf.nf_name).one_or_none()

        if network_function is None or network_function.model_name is None:
            sdnc_model_data = aai_client.get_aai_model_data(app_conf,
                                                            new_nf.model_invariant_id,
                                                            new_nf.model_version_id, new_nf.nf_name)
            try:
                new_nf.sdnc_model_name = sdnc_model_data['sdnc-model-name']
                new_nf.sdnc_model_version = sdnc_model_data['sdnc-model-version']
                new_nf.model_name = sdnc_model_data['model-name']
            except KeyError as e:
                logger.info(f'Skipping NF {new_nf.nf_name} as there is no '
                            f'sdnc-model data associated in AAI: {e}', exc_info=True)
        else:
            new_nf.sdnc_model_name = network_function.sdnc_model_name
            new_nf.sdnc_model_version = network_function.sdnc_model_name
            new_nf.model_name = network_function.model_name

        if self.model_names and new_nf.model_name not in self.model_names:
            match = False
        return match
