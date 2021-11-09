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

from mod import db, logger
from mod.api.db_models import SubscriptionModel, NfSubRelationalModel, \
    NetworkFunctionFilterModel, NetworkFunctionModel
from mod.api.services import measurement_group_service, nf_service
from mod.api.custom_exception import InvalidDataException, DuplicateDataException
from mod.subscription import AdministrativeState
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import joinedload


def create_subscription(subscription):
    """
    Creates a subscription

    Args:
        subscription (dict): subscription to save.

    Raises:
        DuplicateDataException: contains details on duplicate fields
        Exception: contains runtime error details
    """
    logger.info(f'Initiating create subscription for: {subscription["subscriptionName"]}')
    perform_validation(subscription)
    try:
        sub_model, measurement_groups = save_subscription_request(subscription)
        db.session.commit()
        logger.info(f'Successfully saved subscription request for: '
                    f'{subscription["subscriptionName"]}')
        filtered_nfs = nf_service.capture_filtered_nfs(sub_model.subscription_name)
        if filtered_nfs:
            logger.info(f'Applying the filtered nfs for subscription: '
                        f'{sub_model.subscription_name}')
            save_filtered_nfs(filtered_nfs)
            apply_subscription_to_nfs(filtered_nfs, sub_model.subscription_name)
            unlocked_msmt_groups = apply_measurement_grp_to_nfs(filtered_nfs, measurement_groups)
            db.session.commit()
            if unlocked_msmt_groups:
                publish_measurement_grp_to_nfs(sub_model, filtered_nfs,
                                               unlocked_msmt_groups)
            else:
                logger.error(f'All measurement groups are locked for subscription: '
                             f'{sub_model.subscription_name}, '
                             f'please verify/check measurement groups.')
        else:
            logger.error(f'No network functions found for subscription: '
                         f'{sub_model.subscription_name}, '
                         f'please verify/check NetworkFunctionFilter.')
    except IntegrityError as e:
        db.session.rollback()
        raise DuplicateDataException(f'DB Integrity issue encountered: {e.orig.args[0]}')
    except Exception as e:
        db.session.rollback()
        raise e
    finally:
        db.session.remove()


def publish_measurement_grp_to_nfs(sub_model, filtered_nfs,
                                   measurement_groups):
    """
    Publishes an event for measurement groups against nfs

    Args:
        sub_model(SubscriptionModel): Subscription model object
        filtered_nfs (list[NetworkFunction])): list of filtered network functions
        measurement_groups (list[MeasurementGroupModel]): list of unlocked measurement group
    """
    for measurement_group in measurement_groups:
        for nf in filtered_nfs:
            try:
                logger.info(f'Publishing event for nf name, measure_grp_name: {nf.nf_name},'
                            f'{measurement_group.measurement_group_name}')
                measurement_group_service.publish_measurement_group(
                    sub_model, measurement_group, nf)
            except Exception as ex:
                logger.error(f'Publish event failed for nf name, measure_grp_name, sub_name: '
                             f'{nf.nf_name},{measurement_group.measurement_group_name}, '
                             f'{sub_model.subscription_name} with error: {ex}')


def save_filtered_nfs(filtered_nfs):
    """
    Saves a network function

    Args:
        filtered_nfs (list[NetworkFunction]): list of filtered network functions to save.
    """
    pmsh_nf_names = list(nf.nf_name for nf in NetworkFunctionModel.query.all())
    for nf in filtered_nfs:
        if nf.nf_name not in pmsh_nf_names:
            nf_service.save_nf(nf)


def apply_subscription_to_nfs(filtered_nfs, subscription_name):
    """
    Associate and saves the subscription with Network functions

    Args:
        filtered_nfs (list[NetworkFunction]): list of filtered network functions to save.
        subscription_name (string): subscription name to save against nfs
    """
    logger.info(f'Saving filtered nfs for subscription: {subscription_name}')
    for nf in filtered_nfs:
        new_nf_sub_rel = NfSubRelationalModel(subscription_name=subscription_name,
                                              nf_name=nf.nf_name)
        db.session.add(new_nf_sub_rel)


def apply_measurement_grp_to_nfs(filtered_nfs, measurement_groups):
    """
    Saves measurement groups against nfs with status as PENDING_CREATE

    Args:
        filtered_nfs (list[NetworkFunction])): list of filtered network functions
        measurement_groups (list[MeasurementGroupModel]): list of measurement group

    Returns:
        list[MeasurementGroupModel]: list of Unlocked measurement groups
    """
    unlocked_msmt_groups = []
    for measurement_group in measurement_groups:
        if measurement_group.administrative_state \
                == AdministrativeState.UNLOCKED.value:
            unlocked_msmt_groups.append(measurement_group)
            for nf in filtered_nfs:
                logger.info(f'Saving measurement group to nf name, measure_grp_name: {nf.nf_name},'
                            f'{measurement_group.measurement_group_name}')
                measurement_group_service.apply_nf_to_measgroup(
                    nf.nf_name, measurement_group.measurement_group_name)
        else:
            logger.info(f'No nfs added as measure_grp_name: '
                        f'{measurement_group.measurement_group_name} is LOCKED')
    return unlocked_msmt_groups


def check_missing_data(subscription):
    """
    checks if the subscription request has missing data

    Args:
        subscription (dict): subscription to validate

    Raises:
        InvalidDataException: exception containing the list of invalid data.
    """
    if subscription['subscriptionName'].strip() in (None, ''):
        raise InvalidDataException("No value provided in subscription name")
    if subscription['operationalPolicyName'].strip() in (None, ''):
        raise InvalidDataException("Value required for operational Policy Name")

    for measurement_group in subscription.get('measurementGroups'):
        measurement_group_details = measurement_group['measurementGroup']
        if measurement_group_details['administrativeState'].strip() in (None, ''):
            raise InvalidDataException("No value provided for administrative state")
        if measurement_group_details['measurementGroupName'].strip() in (None, ''):
            raise InvalidDataException("No value provided for measurement group name")


def perform_validation(subscription):
    """
    validates the subscription and if invalid raises an exception
    to indicate duplicate/invalid request

    Args:
        subscription (dict): subscription to validate

    Raises:
        DuplicateDataException: exception containing the detail on duplicate data field.
        InvalidDataException: exception containing the detail on invalid data.
    """
    logger.info(f'Performing subscription validation for: {subscription["subscriptionName"]}')
    check_missing_data(subscription)
    logger.info(f'No missing data found for: {subscription["subscriptionName"]}')
    check_duplicate_fields(subscription["subscriptionName"])
    logger.info(f'No duplicate data found for: {subscription["subscriptionName"]}')
    validate_nf_filter(subscription["nfFilter"])
    logger.info(f'Filter data is valid for: {subscription["subscriptionName"]}')


def save_subscription_request(subscription):
    """
    Saves the subscription request consisting of:
    network function filter and measurement groups

    Args:
        subscription (dict): subscription request to be saved.

    Returns:
        string: Subscription name
        list[MeasurementGroupModel]: list of measurement groups
    """
    logger.info(f'Saving subscription request for: {subscription["subscriptionName"]}')
    sub_model = save_subscription(subscription)
    save_nf_filter(subscription["nfFilter"], subscription["subscriptionName"])
    measurement_groups = []
    for measurement_group in subscription['measurementGroups']:
        measurement_groups.append(
            measurement_group_service.save_measurement_group(
                measurement_group['measurementGroup'],
                subscription["subscriptionName"]))
    return sub_model, measurement_groups


def check_duplicate_fields(subscription_name):
    """
    validates the subscription content if already present
    and captures duplicate fields

    Args:
        subscription_name (string): subscription name

    Raises:
        InvalidDataException: exception containing the list of invalid data.
    """

    existing_subscription = (SubscriptionModel.query.filter(
        SubscriptionModel.subscription_name == subscription_name).one_or_none())
    if existing_subscription is not None:
        raise DuplicateDataException(f'subscription Name: {subscription_name} already exists.')


def save_subscription(subscription):
    """
    Saves the subscription data

    Args:
        subscription (dict): subscription model to be saved.
    Returns:
        subscription_model(SubscriptionModel): subscription model
                                               which is added to the session
    """
    control_loop_name = ""
    if 'controlLoopName' in subscription:
        control_loop_name = subscription['controlLoopName']
    subscription_model = \
        SubscriptionModel(subscription_name=subscription["subscriptionName"],
                          operational_policy_name=subscription["operationalPolicyName"],
                          control_loop_name=control_loop_name,
                          status=AdministrativeState.LOCKED.value)
    db.session.add(subscription_model)
    return subscription_model


def validate_nf_filter(nf_filter):
    """
    checks if the nf filter is valid

    Args:
        nf_filter (dict): nf filter to validate

    Raises:
        InvalidDataException: if no field is available in nf_filter
    """
    for filter_name, filter_values in nf_filter.items():
        filter_values[:] = [x for x in filter_values if x.strip()]
    if not [filter_name for filter_name, val in nf_filter.items() if len(val) > 0]:
        raise InvalidDataException("At least one filter within nfFilter must not be empty")


def save_nf_filter(nf_filter, subscription_name):
    """
    Saves the nf_filter data request

    Args:
        nf_filter (dict) : network function filter to save
        subscription_name (string) : subscription name to associate with nf filter.
    """
    logger.info(f'Saving nf filter for subscription request: {subscription_name}')
    new_filter = NetworkFunctionFilterModel(subscription_name=subscription_name,
                                            nf_names=nf_filter['nfNames'],
                                            model_invariant_ids=nf_filter['modelInvariantIDs'],
                                            model_version_ids=nf_filter['modelVersionIDs'],
                                            model_names=nf_filter['modelNames'])
    db.session.add(new_filter)


def get_subscription_by_name(subscription_name):
    """
    Retrieves the subscription information by matching its Name

    Args:
        subscription_name(String): Name of the Subscription
    Returns:
        subscription(SubscriptionModel): else empty
    """
    subscription_model = db.session.query(SubscriptionModel) \
        .options(joinedload(SubscriptionModel.network_filter),
                 joinedload(SubscriptionModel.measurement_groups)) \
        .filter_by(subscription_name=subscription_name).first()
    db.session.remove()
    return subscription_model


def subscription_encoder(subscription):
    """
    Encodes the subscription Model as JSON

    Args:
        subscription(SubscriptionModel) : SubscriptionModel from the DB
    Returns:
        Subscription JSON: which contains subscriptionName, controlLoopName,
        operationalPolicyName, nfFilter, and measurementGroups
    """
    control_loop_name = ''
    if subscription.control_loop_name is not None:
        control_loop_name = subscription.control_loop_name
    return \
        {'subscription':
            {'subscriptionName': subscription.subscription_name,
             'operationalPolicyName': subscription.operational_policy_name,
             'controlLoopName': control_loop_name,
             'nfFilter':
                nf_service.nf_filter_encoder
                (subscription.network_filter[0]),
             'measurementGroups': [measurement_group_service.
                                   measurement_group_encoder(mg)
                                   for mg in subscription.measurement_groups]}}
