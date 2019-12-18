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

from pmsh_service.mod.db_config import db
from pmsh_service.mod.db_models import NetworkFunction
from pmsh_service.mod import pmsh_logging as logger


def get(nf_name):
    """ Retrieves a network function
    Args:
        nf_name (str): The network function name
    Returns:
        NetworkFunction object
    """
    return NetworkFunction.query.filter(
        NetworkFunction.nf_name == nf_name).one_or_none()


def get_all():
    """ Retrieves all network functions
    Returns:
        list: NetworkFunction objects
    """
    return NetworkFunction.query.all()


def create(nf_name):
    """ Creates a NetworkFunction database entry
    Args:
        nf_name (str): The network function name
    Returns:
        NetworkFunction object
    """
    existing_nf = NetworkFunction.query.filter(NetworkFunction.nf_name == nf_name).one_or_none()

    if existing_nf is None:
        new_nf = NetworkFunction(nf_name=nf_name)
        db.session.add(new_nf)
        db.session.commit()

        return new_nf
    else:
        logger.debug(f'Network function {existing_nf} already exists,'
                     f' returning this network function..')
        return existing_nf
