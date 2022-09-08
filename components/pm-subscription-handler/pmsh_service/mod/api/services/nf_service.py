# ============LICENSE_START===================================================
#  Copyright (C) 2021-2022 Nordix Foundation.
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

from mod import db, aai_client, logger
from mod.api.db_models import NetworkFunctionModel, NetworkFunctionFilterModel
from mod.pmsh_config import AppConfig
from mod.network_function import NetworkFunctionFilter


def capture_filtered_nfs(sub_name):
    """
    Retrieves network functions from AAI client and
    returns a list of filtered NetworkFunctions using the Filter

    Args:
        sub_name (string): The name of subscription inorder to perform filtering
    Returns:
        list[NetworkFunction]: a list of filtered NetworkFunction Objects
        or an empty list if no network function is filtered.
    """
    logger.info(f'Getting filtered nfs for subscription: {sub_name}')
    nf_filter = NetworkFunctionFilter.get_network_function_filter(sub_name)
    return aai_client.get_pmsh_nfs_from_aai(AppConfig.get_instance(), nf_filter)


def create_nf_event_body(nf, change_type, sub_model):
    """
    Creates a network function event body to publish on MR

    Args:
        nf (NetworkFunction): the Network function to include in the event.
        change_type (string): define the change type to be applied on node
        sub_model(SubscriptionModel): Subscription model object
    Returns:
        dict: network function event body to publish on MR.
    """
    return {'nfName': nf.nf_name,
            'ipAddress': nf.ipv4_address if nf.ipv6_address in (None, '')
            else nf.ipv6_address,
            'blueprintName': nf.sdnc_model_name,
            'blueprintVersion': nf.sdnc_model_version,
            'operationalPolicyName': sub_model.operational_policy_name,
            'changeType': change_type,
            'controlLoopName': sub_model.control_loop_name}


def save_nf(nf):
    """
    Saves the network function request to the db
    Args:
        nf (NetworkFunction) : requested network function to save
    """
    network_function = NetworkFunctionModel(nf_name=nf.nf_name,
                                            ipv4_address=nf.ipv4_address,
                                            ipv6_address=nf.ipv6_address,
                                            model_invariant_id=nf.model_invariant_id,
                                            model_version_id=nf.model_version_id,
                                            model_name=nf.model_name,
                                            sdnc_model_name=nf.sdnc_model_name,
                                            sdnc_model_version=nf.sdnc_model_version)
    db.session.add(network_function)


def save_nf_filter_update(sub_name, nf_filter):
    """
    Updates the network function filter for the subscription in the db

    Args:
       sub_name (String): Name of the Subscription
       nf_filter (dict): filter object to update in the subscription
    """
    NetworkFunctionFilterModel.query.filter(
        NetworkFunctionFilterModel.subscription_name == sub_name). \
        update({NetworkFunctionFilterModel.nf_names: nf_filter['nfNames'],
                NetworkFunctionFilterModel.model_invariant_ids: nf_filter['modelInvariantIDs'],
                NetworkFunctionFilterModel.model_version_ids: nf_filter['modelVersionIDs'],
                NetworkFunctionFilterModel.model_names: nf_filter['modelNames']},
               synchronize_session='evaluate')
    db.session.commit()
    logger.info(f'Successfully saved filter for subscription: {sub_name}')
