# ============LICENSE_START===================================================
#  Copyright (C) 2020-2021 Nordix Foundation.
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

from mod import db, logger
from mod.api.db_models import SubscriptionModel, NfSubRelationalModel
from mod.api.services import measurement_group_service, nf_service
from mod.api.custom_exception import InvalidDataException
from mod.subscription import AdministrativeState


def create_subscription(subscription):
    """
        Creates a subscription

        Args:
            subscription (dict): subscription to save.

        Raises:
            Error: If anything fails in the server.
    """
    perform_validation(subscription)
    try:
        save_subscription_request(subscription)
        filtered_nfs = nf_service.capture_filtered_nfs(subscription["nfFilter"])
        if len(filtered_nfs) > 0:
            save_filtered_nfs(filtered_nfs)
            apply_subscription_to_nfs(filtered_nfs, subscription["subscriptionName"])
            apply_measurement_grp_to_nfs(subscription["subscriptionName"],
                                         filtered_nfs, subscription.get('measurementGroups'))
        db.session.commit()
    except Exception as e:
        db.session.rollback()
        logger.error(f'Failed to create subscription '
                     f'{subscription["subscriptionName"]} in the DB: {e}', exc_info=True)
        raise e
    finally:
        db.session.remove()


def save_filtered_nfs(filtered_nfs):
    """
        Saves a network function

        Args:
            filtered_nfs (dict): list of filtered network functions to save.
    """
    for nf in filtered_nfs:
        nf_service.save_nf(nf)


def apply_subscription_to_nfs(filtered_nfs, subscription_name):
    """
        Associate and saves the subscription with Network functions

        Args:
            filtered_nfs (dict): list of filtered network functions to save.
            subscription_name (string): subscription name to save against nfs
    """
    for nf in filtered_nfs:
        new_nf_sub_rel = NfSubRelationalModel(subscription_name=subscription_name,
                                              nf_name=nf.nf_name)
        db.session.add(new_nf_sub_rel)


def apply_measurement_grp_to_nfs(subscription_name, filtered_nfs, measurement_groups):
    """
        Publishes an event for measurement groups against nfs
        And saves the successful trigger action as PENDING_CREATE

        Args:
            subscription_name (string): subscription name to publish against nfs
            filtered_nfs (dict): list of filtered network functions to publish.
            measurement_groups (dict): list of measurement group to publish
    """
    if measurement_groups:
        for measurement_group in measurement_groups:
            measurement_group_details = measurement_group['measurementGroup']
            if measurement_group_details['administrativeState'] \
                    == AdministrativeState.UNLOCKED.value:
                measurement_group_service.publish_measurement_group(
                    subscription_name, measurement_group_details, filtered_nfs)
                for nf in filtered_nfs:
                    measurement_group_service.apply_nf(nf, measurement_group_details)


def perform_validation(subscription):
    """
    validates the subscription and if invalid raises an exception
    to indicate duplicate/invalid request

    Args:
        subscription (Subscription): subscription to validate

    Raises:
        InvalidDataException: exception containing the list of invalid data.
    """
    invalid_input_messages = validate_subscription(subscription)
    if invalid_input_messages:
        raise InvalidDataException(invalid_input_messages)


def save_subscription_request(subscription):
    """
        Saves the subscription request consisting of:
        network function filter and measurement groups

        Args:
            subscription (dict): subscription request to be saved.
    """
    save_subscription(subscription)
    nf_service.save_nf_filter(subscription["nfFilter"], subscription["subscriptionName"])
    if subscription.get('measurementGroups'):
        for measurement_group in subscription['measurementGroups']:
            measurement_group_service \
                .save_measurement_group(measurement_group['measurementGroup'],
                                        subscription["subscriptionName"])


def validate_subscription(subscription):
    """
        validates the subscription content if already present
        and if present raises an exception to indicate duplicate/invalid request

        Args:
            subscription (Subscription): subscription to validate

        Returns:
            invalid_messages: list of invalid data details.
        """
    invalid_messages = []
    existing_subscription = (SubscriptionModel.query.filter(
        SubscriptionModel.subscription_name == subscription['subscriptionName']).one_or_none())
    if existing_subscription is not None:
        invalid_messages.append(f'subscription Name: {subscription["subscriptionName"]}'
                                f' already exists.')
    return invalid_messages


def save_subscription(subscription):
    """
        Saves the subscription data

        Args:
            subscription (dict): subscription request to be saved.
    """
    new_subscription = SubscriptionModel(subscription_name=subscription["subscriptionName"],
                                         status='LOCKED')
    db.session.add(new_subscription)
