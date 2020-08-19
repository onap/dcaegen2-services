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

import re

from mod import logger, db
from mod.api.db_models import NetworkFunctionModel


class NetworkFunction:
    def __init__(self, **kwargs):
        """ Object representation of the NetworkFunction. """
        self.nf_name = kwargs.get('nf_name')
        self.orchestration_status = kwargs.get('orchestration_status')

    @classmethod
    def nf_def(cls):
        return cls(nf_name=None, orchestration_status=None)

    def __str__(self):
        return f'nf-name: {self.nf_name}, orchestration-status: {self.orchestration_status}'

    def __eq__(self, other):
        return self.nf_name == other.nf_name and \
            self.orchestration_status == other.orchestration_status

    def __hash__(self):
        return hash((self.nf_name, self.orchestration_status))

    def create(self):
        """ Creates a NetworkFunction database entry """
        existing_nf = NetworkFunctionModel.query.filter(
            NetworkFunctionModel.nf_name == self.nf_name).one_or_none()

        if existing_nf is None:
            new_nf = NetworkFunctionModel(nf_name=self.nf_name,
                                          orchestration_status=self.orchestration_status)
            db.session.add(new_nf)
            db.session.commit()
            logger.info(f'Network Function {new_nf.nf_name} successfully created.')
            return new_nf
        else:
            logger.debug(f'Network function {existing_nf.nf_name} already exists,'
                         f' returning this network function..')
            return existing_nf

    @staticmethod
    def get(nf_name):
        """ Retrieves a network function
        Args:
            nf_name (str): The network function name
        Returns:
            NetworkFunctionModel object else None
        """
        return NetworkFunctionModel.query.filter(
            NetworkFunctionModel.nf_name == nf_name).one_or_none()

    @staticmethod
    def get_all():
        """ Retrieves all network functions
        Returns:
            list: NetworkFunctionModel objects else empty
        """
        return NetworkFunctionModel.query.all()

    @staticmethod
    def delete(**kwargs):
        """ Deletes a network function from the database """
        nf_name = kwargs['nf_name']
        nf = NetworkFunctionModel.query.filter(
            NetworkFunctionModel.nf_name == nf_name).one_or_none()

        if nf:
            db.session.delete(nf)
            db.session.commit()


class NetworkFunctionFilter:
    def __init__(self, **kwargs):
        self.nf_names = kwargs.get('nfNames')
        self.model_invariant_ids = kwargs.get('modelInvariantUUIDs')
        self.model_version_ids = kwargs.get('modelVersionIDs')
        self.regex_matcher = re.compile('|'.join(raw_regex for raw_regex in self.nf_names))

    def is_nf_in_filter(self, nf_name, model_invariant_id, model_version_id, orchestration_status):
        """Match the nf name against regex values in Subscription.nfFilter.nfNames

        Args:
            nf_name (str): the AAI nf name.
            invariant_uuid (str): the AAI model-invariant-id
            uuid (str): the AAI model-version-id
            orchestration_status (str): orchestration status of the nf

        Returns:
            bool: True if matched, else False.
        """
        match = True
        if orchestration_status != 'Active':
            match = False
        if self.nf_names and self.regex_matcher.search(nf_name) is None:
            match = False
        if self.model_invariant_ids and not model_invariant_id in self.model_invariant_ids:
            match = False
        if self.model_version_ids and not model_version_id in self.model_version_ids:
            match = False
        return match
