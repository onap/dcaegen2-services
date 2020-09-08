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

import mod.aai_client
from mod import logger, db
from mod.api.db_models import NetworkFunctionModel


class NetworkFunction:
    def __init__(self, **kwargs):
        """ Object representation of the NetworkFunction. """
        self.nf_name = kwargs.get('nf_name')
        self.model_invariant_id = kwargs.get('model_invariant_id')
        self.model_version_id = kwargs.get('model_version_id')
        self.sdnc_model_name = None
        self.sdnc_model_version = None

    @classmethod
    def nf_def(cls):
        return cls(nf_name=None, model_invariant_id=None, model_version_id=None,
                   sdnc_model_name=None, sdnc_model_version=None)

    def __str__(self):
        return f'nf-name: {self.nf_name}, ' \
               f'model-invariant-id: {self.model_invariant_id}, ' \
               f'model-version-id: {self.model_version_id}, ' \
               f'sdnc-model-name: {self.sdnc_model_name}, ' \
               f'sdnc-model-version: {self.sdnc_model_version}'

    def __eq__(self, other):
        return \
            self.nf_name == other.nf_name and \
            self.model_invariant_id == other.model_invariant_id and \
            self.model_version_id == other.model_version_id and \
            self.sdnc_model_name == other.sdnc_model_name and \
            self.sdnc_model_version == other.sdnc_model_version

    def __hash__(self):
        return hash((self.nf_name, self.model_invariant_id,
                     self.model_version_id, self.sdnc_model_name, self.sdnc_model_version))

    def create(self):
        """ Creates a NetworkFunction database entry """
        existing_nf = NetworkFunctionModel.query.filter(
            NetworkFunctionModel.nf_name == self.nf_name).one_or_none()

        if existing_nf is None:
            new_nf = NetworkFunctionModel(nf_name=self.nf_name,
                                          model_invariant_id=self.model_invariant_id,
                                          model_version_id=self.model_version_id,
                                          sdnc_model_name=self.sdnc_model_name,
                                          sdnc_model_version=self.sdnc_model_version)
            db.session.add(new_nf)
            db.session.commit()
            logger.info(f'Network Function {new_nf.nf_name} successfully created.')
            return new_nf
        else:
            logger.debug(f'Network function {existing_nf.nf_name} already exists,'
                         f' returning this network function..')
            return existing_nf

    def set_sdnc_params(self, app_conf):
        params_set = True
        try:
            sdnc_model_data = mod.aai_client.get_aai_model_data(app_conf, self.model_invariant_id,
                                                                self.model_version_id, self.nf_name)
            try:
                self.sdnc_model_name = sdnc_model_data['sdnc-model-name']
                self.sdnc_model_version = sdnc_model_data['sdnc-model-version']
                return params_set
            except KeyError as e:
                logger.info(f'Skipping NF {self.nf_name} as there is no '
                            f'sdnc-model data associated in AAI: {e}', exc_info=True)
                return not params_set
        except Exception as e:
            logger.error(f'Failed to get sdnc-model info for XNFs from AAI: {e}', exc_info=True)
            return not params_set

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
        self.model_invariant_ids = kwargs.get('modelInvariantIDs')
        self.model_version_ids = kwargs.get('modelVersionIDs')
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
        return match
