# ============LICENSE_START===================================================
#  Copyright (C) 2019-2021 Nordix Foundation.
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
from mod import db, aai_client
from mod.api.db_models import NetworkFunctionFilterModel, NetworkFunctionModel
from flask import current_app
from mod.network_function_filter import NetworkFunctionFilter


def save_nf_filter(nf_filter, subscriptionName):
    new_filter = NetworkFunctionFilterModel(subscription_name=subscriptionName,
                                            nf_names=nf_filter['nfNames'],
                                            model_invariant_ids=nf_filter['modelInvariantIDs'],
                                            model_version_ids=nf_filter['modelVersionIDs'],
                                            model_names=nf_filter['modelNames'])
    db.session.add(new_filter)


def capture_filtered_nfs(nf_filter):
    app_conf = current_app.config['app_config']
    nfs_in_aai = aai_client._get_all_aai_nf_data(app_conf)
    nf_filter_module = NetworkFunctionFilter(**nf_filter)
    return nf_filter_module.filter_nfs(nfs_in_aai, app_conf)


def create_nf_event_body(nf, change_type):
    app_conf = current_app.config['app_config']
    return {'networkFunction': {'nfName': nf.nf_name,
                                'ipv4Address': nf.ipv4_address,
                                'ipv6Address': nf.ipv6_address,
                                'blueprintName': nf.sdnc_model_name,
                                'blueprintVersion': nf.sdnc_model_version,
                                'policyName': app_conf.operational_policy_name,
                                'changeType': change_type,
                                'closedLoopControlName': app_conf.control_loop_name}}


def save_nf(nf):
    network_function = NetworkFunctionModel.query.filter(
        NetworkFunctionModel.nf_name == nf.nf_name).one_or_none()
    if network_function is None:
        network_function = NetworkFunctionModel(nf_name=nf.nf_name,
                                                ipv4_address=nf.ipv4_address,
                                                ipv6_address=nf.ipv6_address,
                                                model_invariant_id=nf.model_invariant_id,
                                                model_version_id=nf.model_version_id,
                                                model_name=nf.model_name,
                                                sdnc_model_name=nf.sdnc_model_name,
                                                sdnc_model_version=nf.sdnc_model_version)
        db.session.add(network_function)
    elif network_function.model_name is None:
        network_function.update({NetworkFunctionModel.sdnc_model_name: nf.sdnc_model_name,
                                 NetworkFunctionModel.sdnc_model_version: nf.sdnc_model_version,
                                 NetworkFunctionModel.model_name: nf.model_name
                                 }, synchronize_session='evaluate')
