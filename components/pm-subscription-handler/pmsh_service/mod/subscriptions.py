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
from pmsh_service.mod.db_models import Subscription
from pmsh_service.mod import pmsh_logging as logger


def get(subscription_name):
    """ Retrieves a subscription
    Args:
        subscription_name (str): The subscription name
    Returns:
        Subscription object
    """
    return Subscription.query.filter(
        Subscription.subscription_name == subscription_name).one_or_none()


def get_all():
    """ Retrieves a list of subscriptions
    Returns:
        list: Subscription list
    """
    return Subscription.query.all()


def create(subscription_name, subscription_status):
    """ Creates a subscription database entry
    Args:
        subscription_name (str): The subscription name
        subscription_status (str): The subscription status
    Returns:
        Subscription object
    """
    existing_subscription = (
        Subscription.query.filter(
            Subscription.subscription_name == subscription_name).one_or_none())

    if existing_subscription is None:
        new_subscription = Subscription(subscription_name=subscription_name,
                                        status=subscription_status)

        db.session.add(new_subscription)
        db.session.commit()

        return new_subscription

    else:
        logger.debug(f'Subscription {subscription_name} already exists,'
                     f' returning this subscription..')
        return existing_subscription
