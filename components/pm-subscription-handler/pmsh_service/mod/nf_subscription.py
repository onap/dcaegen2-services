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


import pmsh_service.mod.network_functions as network_functions
import pmsh_service.mod.subscriptions as subscriptions
from pmsh_service.mod.db_config import db
from pmsh_service.mod.db_models import NetworkFunctionSubscription


def add_network_functions_per_subscription(subscription_name, subscription_status, nf_list):
    """ Associates network functions to a subscription
    Args:
        subscription_name (str): The subscription name
        subscription_status (str): The subscription name
        nf_list (list): Network function object list
    """

    current_subscription = subscriptions.create(subscription_name, subscription_status)

    for nf in nf_list:
        nf_name = nf['nf_name']
        nf_status = nf['status']
        current_nf = network_functions.create(nf_name)

        existing_entry = NetworkFunctionSubscription.query.filter(
            NetworkFunctionSubscription.subscription_name == current_subscription.subscription_name,
            NetworkFunctionSubscription.nf_name == current_nf.nf_name).one_or_none()
        if existing_entry is None:
            nf_sub = NetworkFunctionSubscription(subscription_name, nf_name, nf_status)
            nf_sub.nf = current_nf
            current_subscription.nfs.append(nf_sub)

    db.session.add(current_subscription)
    db.session.commit()


def get_nfs_per_subscription(subscription_name):
    """ Retrieves network functions per subscription
    Args:
        subscription_name (str): The subscription name
    Returns:
        list: Network function subscription list
    """
    nf_subscriptions = (NetworkFunctionSubscription.query.filter(
        NetworkFunctionSubscription.subscription_name == subscription_name).all())

    return nf_subscriptions
