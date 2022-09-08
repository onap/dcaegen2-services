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

from mod import db, logger
from mod.api.db_models import SubscriptionModel, NfSubRelationalModel, \
    NetworkFunctionFilterModel, NetworkFunctionModel, MeasurementGroupModel, \
    NfMeasureGroupRelationalModel
from mod.api.services import measurement_group_service, nf_service
from mod.api.custom_exception import InvalidDataException, DuplicateDataException, \
    DataConflictException
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
        sub_model = save_subscription_request(subscription)
        db.session.commit()
        logger.info(f'Successfully saved subscription request for: '
                    f'{subscription["subscriptionName"]}')
        filtered_nfs = nf_service.capture_filtered_nfs(sub_model.subscription_name)
        unlocked_mgs = get_unlocked_measurement_grps(sub_model)
        add_new_filtered_nfs(filtered_nfs, unlocked_mgs, sub_model)
    except IntegrityError as e:
        db.session.rollback()
        raise DuplicateDataException(f'DB Integrity issue encountered: {e.orig.args[0]}') from e
    except Exception as e:
        db.session.rollback()
        raise e
    finally:
        db.session.remove()


def add_new_filtered_nfs(filtered_nfs, unlocked_mgs, sub_model):
    """
    Inserts the filtered nfs in measurement groups of subscription

    Args:
        filtered_nfs (List[NetworkFunction]): nfs to be inserted
        unlocked_mgs (List[MeasurementGroupModel]): mgs to be updated with new nfs
        sub_model (SubscriptionModel): subscription model to update
    """
    if filtered_nfs:
        logger.info(f'Applying the filtered nfs for subscription: '
                    f'{sub_model.subscription_name}')
        save_filtered_nfs(filtered_nfs)
        apply_subscription_to_nfs(filtered_nfs, sub_model.subscription_name)
        db.session.commit()
        if unlocked_mgs:
            apply_measurement_grp_to_nfs(filtered_nfs, unlocked_mgs)
            db.session.commit()
            publish_measurement_grp_to_nfs(sub_model, filtered_nfs,
                                           unlocked_mgs)
        else:
            logger.error(f'All measurement groups are locked for subscription: '
                         f'{sub_model.subscription_name}, '
                         f'please verify/check measurement groups.')
    else:
        logger.error(f'No network functions found for subscription: '
                     f'{sub_model.subscription_name}, '
                     f'please verify/check NetworkFunctionFilter.')


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
                    sub_model, measurement_group, nf, 'CREATE')
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


def apply_measurement_grp_to_nfs(filtered_nfs, unlocked_mgs):
    """
    Saves measurement groups against nfs with status as PENDING_CREATE

    Args:
        filtered_nfs (list[NetworkFunction]): list of filtered network functions
        unlocked_mgs (list[MeasurementGroupModel]): list of measurement group

    """
    for measurement_group in unlocked_mgs:
        for nf in filtered_nfs:
            logger.info(f'Saving measurement group to nf name, measure_grp_name: {nf.nf_name},'
                        f'{measurement_group.measurement_group_name}')
            measurement_group_service.apply_nf_status_to_measurement_group(
                nf.nf_name, measurement_group.measurement_group_name,
                measurement_group_service.MgNfState.PENDING_CREATE.value)


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
        SubscriptionModel: subscription object which was added to the session
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
    return sub_model


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
                          status=measurement_group_service.AdministrativeState.LOCKED.value)
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


def query_subscription_by_name(subscription_name):
    """
    Queries the db for existing subscription by name

    Args:
        subscription_name (String): Name of the Subscription

    Returns:
        SubscriptionModel: If subscription was defined else None
    """
    logger.info(f'Attempting to fetch subscription by name: {subscription_name}')
    subscription_model = db.session.query(SubscriptionModel) \
        .options(joinedload(SubscriptionModel.network_filter),
                 joinedload(SubscriptionModel.measurement_groups),
                 joinedload(SubscriptionModel.nfs)) \
        .filter_by(subscription_name=subscription_name).first()
    db.session.remove()
    return subscription_model


def query_all_subscriptions():
    """
    Queries the db for all existing subscriptions defined in PMSH

    Returns
        list (SubscriptionModel): of all subscriptions else None
    """
    logger.info('Attempting to fetch all the subscriptions')
    subscriptions = db.session.query(SubscriptionModel) \
        .options(joinedload(SubscriptionModel.network_filter),
                 joinedload(SubscriptionModel.measurement_groups),
                 joinedload(SubscriptionModel.nfs)) \
        .all()
    db.session.remove()
    return subscriptions


def get_subscriptions_list():
    """ Converts all subscriptions to JSON and appends to list

    Returns
       list: dict of all subscriptions else empty
    """
    subscriptions = query_all_subscriptions()
    subscriptions_list = []
    if subscriptions is not None:
        logger.info('Queried all the subscriptions was successful')
        for subscription in subscriptions:
            if (subscription.network_filter is not None) and \
                    (len(subscription.measurement_groups) != 0):
                subscriptions_list.append(subscription.serialize())
    return subscriptions_list


def query_unlocked_mg_by_sub_name(subscription_name):
    """
    Queries the db for unlocked/locking measurement groups by subscription name

    Args:
        subscription_name (String): Name of the Subscription

    Returns:
        list (MeasurementGroupModel): If measurement groups with admin state
                                      UNLOCKED exists else empty list
    """
    logger.info(f'Attempting to fetch measurement groups by subscription name: {subscription_name}')
    mg_model = db.session.query(MeasurementGroupModel) \
        .filter_by(subscription_name=subscription_name) \
        .filter(MeasurementGroupModel.administrative_state.in_(('UNLOCKED', 'LOCKING'))).all()
    db.session.remove()
    return mg_model


def query_to_delete_subscription_by_name(subscription_name):
    """
    Deletes the subscription by name

    Args:
        subscription_name (String): Name of the Subscription

    Returns:
        int: Returns '1' if subscription exists and deleted successfully else '0'
    """
    effected_rows = db.session.query(SubscriptionModel) \
        .filter_by(subscription_name=subscription_name).delete()
    db.session.commit()
    return effected_rows


def is_duplicate_filter(nf_filter, db_network_filter):
    """
    Checks if the network function filter is unchanged for the subscription

    Args:
        nf_filter (dict): filter object to update in the subscription
        db_network_filter (NetworkFunctionFilterModel): nf filter object from db

    Returns:
        (boolean) : True is nf filters are same else False
    """
    return nf_filter == db_network_filter.serialize()


def update_filter(sub_name, nf_filter):
    """
    Updates the network function filter for the subscription

    Args:
        sub_name (String): Name of the Subscription
        nf_filter (dict): filter object to update in the subscription

    Returns:
        InvalidDataException: contains details on invalid fields
        DataConflictException: contains details on conflicting state of a field
        Exception: contains runtime error details
    """
    sub_model = query_subscription_by_name(sub_name)
    if sub_model is None:
        raise InvalidDataException('Requested subscription is not available '
                                   f'with sub name: {sub_name} for nf filter update')
    if is_duplicate_filter(nf_filter, sub_model.network_filter):
        raise InvalidDataException('Duplicate nf filter update requested for subscription '
                                   f'with sub name: {sub_name}')
    validate_sub_mgs_state(sub_model)
    nf_service.save_nf_filter_update(sub_name, nf_filter)
    del_nfs, new_nfs = extract_del_new_nfs(sub_model)
    NfSubRelationalModel.query.filter(
        NfSubRelationalModel.subscription_name == sub_name,
        NfSubRelationalModel.nf_name.in_(del_nfs)).delete()
    db.session.commit()
    unlocked_mgs = get_unlocked_measurement_grps(sub_model)
    if unlocked_mgs:
        add_new_filtered_nfs(new_nfs, unlocked_mgs, sub_model)
        delete_filtered_nfs(del_nfs, sub_model, unlocked_mgs)
    db.session.remove()


def get_unlocked_measurement_grps(sub_model):
    """
    Gets unlocked measurement groups and logs locked measurement groups

    Args:
        sub_model (SubscriptionModel): Subscription model to perform nfs delete

    Returns:
        unlocked_mgs (List[MeasurementGroupModel]): unlocked msgs in a subscription

    """
    unlocked_mgs = []
    for measurement_group in sub_model.measurement_groups:
        if measurement_group.administrative_state \
                == measurement_group_service.AdministrativeState.UNLOCKED.value:
            unlocked_mgs.append(measurement_group)
        else:
            logger.info(f'No nfs added as measure_grp_name: '
                        f'{measurement_group.measurement_group_name} is LOCKED')
    return unlocked_mgs


def delete_filtered_nfs(del_nfs, sub_model, unlocked_mgs):
    """
    Removes unfiltered nfs

    Args:
        del_nfs (List[String]): Names of nfs to be deleted
        sub_model (SubscriptionModel): Subscription model to perform nfs delete
        unlocked_mgs (List[MeasurementGroupModel]): unlocked msgs to perform nfs delete

    """
    if del_nfs:
        logger.info(f'Removing nfs from subscription: '
                    f'{sub_model.subscription_name}')
        for mg in unlocked_mgs:
            MeasurementGroupModel.query.filter(
                MeasurementGroupModel.measurement_group_name == mg.measurement_group_name) \
                .update({MeasurementGroupModel.administrative_state:
                        measurement_group_service.AdministrativeState.
                        FILTERING.value}, synchronize_session='evaluate')
            db.session.commit()
            nf_meas_relations = NfMeasureGroupRelationalModel.query.filter(
                NfMeasureGroupRelationalModel.measurement_grp_name == mg.
                measurement_group_name, NfMeasureGroupRelationalModel.
                nf_name.in_(del_nfs)).all()
            measurement_group_service.deactivate_nfs(sub_model, mg, nf_meas_relations)


def extract_del_new_nfs(sub_model):
    """
    Captures nfs to be deleted and created for the subscription

    Args:
        sub_model (SubscriptionModel): Subscription model to perform nfs delete

    Returns:
        del_nfs (List[String]): Names of nfs to be deleted
        new_nfs (List[NetworkFunction]): nfs to be inserted
    """
    filtered_nfs = nf_service.capture_filtered_nfs(sub_model.subscription_name)
    filtered_nf_names = [nf.nf_name for nf in filtered_nfs]
    existing_nf_names = [nf.nf_name for nf in sub_model.nfs]
    new_nfs = list(filter(lambda x: x.nf_name not in existing_nf_names, filtered_nfs))
    del_nfs = [nf.nf_name for nf in sub_model.nfs if nf.nf_name not in filtered_nf_names]
    return del_nfs, new_nfs


def validate_sub_mgs_state(sub_model):
    """
    Validates if any measurement group in subscription has
    status Locking or Filtering

    Args:
        sub_model (SubscriptionModel): Subscription model to perform validation before nf filter

    Returns:
        DataConflictException: contains details on conflicting status in measurement group
    """
    mg_names_processing = [mg for mg in sub_model.measurement_groups
                           if mg.administrative_state in [measurement_group_service.
                                                          AdministrativeState.FILTERING.value,
                                                          measurement_group_service.
                                                          AdministrativeState.LOCKING.value]]
    if mg_names_processing:
        raise DataConflictException('Cannot update filter as subscription: '
                                    f'{sub_model.subscription_name} is under '
                                    'transitioning state for the following measurement '
                                    f'groups: {mg_names_processing}')
