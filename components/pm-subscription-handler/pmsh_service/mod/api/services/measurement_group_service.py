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

from mod.api.custom_exception import InvalidDataException, \
    DataConflictException, DuplicateDataException
from mod.api.db_models import MeasurementGroupModel, \
    NfMeasureGroupRelationalModel, SubscriptionModel
from mod import db, logger
from mod.api.services import nf_service, subscription_service
from mod.network_function import NetworkFunction
from mod.pmsh_config import MRTopic, AppConfig
from mod.subscription import AdministrativeState, SubNfState
from sqlalchemy import or_


def create_measurement_group(subscription, measurement_group_name, body):
    """
    Creates a measurement group for a subscription

    Args:
        subscription (String): Name of the subscription.
        measurement_group_name (String): Name of MeasGroup
        body (dict): measurement group request body to save.

    """
    logger.info(f'Initiating create measurement group for: {body.measurement_group_name}')
    check_duplication(subscription.subscription_name, body.measurement_group_name)
    check_measurement_group_names_comply(measurement_group_name, body.measurement_group_name)
    save_measurement_group(body, subscription.subscription_name)
    measurement_groups = [body]
    filtered_nfs = nf_service.capture_filtered_nfs(subscription.subscription_name)
    if filtered_nfs:
        subscription_service.save_filtered_nfs(filtered_nfs)
        subscription_service.apply_subscription_to_nfs(filtered_nfs, subscription.subscription_name)
        unlocked_msmt_groups = subscription_service.apply_measurement_grp_to_nfs(filtered_nfs,
                                                                                 measurement_groups)
        if unlocked_msmt_groups:
            subscription_service.publish_measurement_grp_to_nfs(subscription, filtered_nfs,
                                                                unlocked_msmt_groups)
        else:
            logger.error(f'Measurement group is locked for subscription: '
                         f'{subscription.subscription_name}, '
                         f'please verify/check measurement group.')
    else:
        logger.error(f'No network functions found for subscription: '
                     f'{subscription.subscription_name}, '
                     f'please verify/check NetworkFunctionFilter.')
    db.session.commit()


def check_measurement_group_names_comply(measurement_group_name, body_measurement_group_name):
    """
    Check if measurement_group_name matches the name in the URI

    Args:
        measurement_group_name (String): Name of the measurement group
        body_measurement_group_name (String): Name of the measurement group in body

    Raises:
        InvalidDataException: exception informing that measurement_group_names do not match
    """
    if measurement_group_name != body_measurement_group_name:
        raise InvalidDataException('Measurement Group Name in body does not match with URI')


def check_duplication(subscription_name, measurement_group_name):
    """
    Check if measurement group exists already

    Args:
        measurement_group_name (String): Name of the measurement group
        subscription_name (string) : subscription name to associate with measurement group.

    Raises:
        DuplicateDataException: exception containing the detail on duplicate data field.
    """
    logger.info(f"Checking that measurement group {measurement_group_name} does not exist")
    existing_meas_group = query_meas_group_by_name(subscription_name, measurement_group_name)
    if existing_meas_group is not None:
        raise DuplicateDataException(f'Measurement Group Name: '
                                     f'{measurement_group_name} already exists.')


def save_measurement_group(measurement_group, subscription_name):
    """
    Saves the measurement_group data request

    Args:
        measurement_group (dict) : measurement group to save
        subscription_name (string) : subscription name to associate with measurement group.

    Returns:
        MeasurementGroupModel : measurement group saved in the database
    """
    logger.info(f'Saving measurement group for subscription request: {subscription_name}')
    if type(measurement_group) != MeasurementGroupModel:
        new_measurement_group = MeasurementGroupModel(
            subscription_name=subscription_name,
            measurement_group_name=measurement_group['measurementGroupName'],
            administrative_state=measurement_group['administrativeState'],
            file_based_gp=measurement_group['fileBasedGP'],
            file_location=measurement_group['fileLocation'],
            measurement_type=measurement_group['measurementTypes'],
            managed_object_dns_basic=measurement_group['managedObjectDNsBasic'])
    else:
        new_measurement_group = measurement_group
    db.session.add(new_measurement_group)
    return new_measurement_group


def apply_nf_status_to_measurement_group(nf_name, measurement_group_name, status):
    """
    Associate and saves the measurement group with Network function

    Args:
        nf_name (string): Network function name.
        measurement_group_name (string): Measurement group name
        status (string): nf status to apply on measurement group
    """
    new_nf_measure_grp_rel = NfMeasureGroupRelationalModel(
        measurement_grp_name=measurement_group_name,
        nf_name=nf_name,
        nf_measure_grp_status=status
    )
    db.session.add(new_nf_measure_grp_rel)


def publish_measurement_group(sub_model, measurement_group, nf, change_type):
    """
    Publishes an event for measurement group against nfs to MR

    Args:
        sub_model(SubscriptionModel): Subscription model object
        measurement_group (MeasurementGroupModel): Measurement group to publish
        nf (NetworkFunction): Network function to publish
        change_type (string): defines event type like CREATE or DELETE
   """
    event_body = nf_service.create_nf_event_body(nf, change_type, sub_model)
    event_body['subscription'] = {
        "administrativeState": measurement_group.administrative_state,
        "subscriptionName": sub_model.subscription_name,
        "fileBasedGP": measurement_group.file_based_gp,
        "fileLocation": measurement_group.file_location,
        "measurementGroup": {
            "measurementGroupName": measurement_group.measurement_group_name,
            "measurementTypes": measurement_group.measurement_type,
            "managedObjectDNsBasic": measurement_group.managed_object_dns_basic
        }
    }
    logger.debug(f'Event Body: {event_body}')
    AppConfig.get_instance().publish_to_topic(MRTopic.POLICY_PM_PUBLISHER.value, event_body)


def update_measurement_group_nf_status(measurement_group_name, status, nf_name):
    """ Updates the status of a measurement grp for a particular nf

    Args:
        measurement_group_name (string): Measurement group name
        nf_name (string): The network function name
        status (string): status of the network function for measurement group
    """
    try:
        logger.info(f'Performing update for measurement group name: {measurement_group_name},'
                    f' network function name: {nf_name} on status: {status}')
        NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == measurement_group_name,
            NfMeasureGroupRelationalModel.nf_name == nf_name). \
            update({NfMeasureGroupRelationalModel.nf_measure_grp_status: status},
                   synchronize_session='evaluate')
        db.session.commit()
    except Exception as e:
        logger.error(f'Failed to update nf: {nf_name} for measurement group: '
                     f'{measurement_group_name} due to: {e}')


def delete_nf_to_measurement_group(nf_name, measurement_group_name, status):
    """ Deletes a particular nf related to a measurement group name and
        if no more relations of nf exist to measurement group then delete nf from PMSH

    Args:
        nf_name (string): The network function name
        measurement_group_name (string): Measurement group name
        status (string): status of the network function for measurement group
    """
    try:
        logger.info(f'Performing delete for measurement group name: {measurement_group_name},'
                    f' network function name: {nf_name} on status: {status}')
        nf_measurement_group_rel = NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == measurement_group_name,
            NfMeasureGroupRelationalModel.nf_name == nf_name).one_or_none()
        db.session.delete(nf_measurement_group_rel)
        db.session.commit()
        nf_relations = NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.nf_name == nf_name).all()
        if not nf_relations:
            NetworkFunction.delete(nf_name=nf_name)
    except Exception as e:
        logger.error(f'Failed to delete nf: {nf_name} for measurement group: '
                     f'{measurement_group_name} due to: {e}')


def query_meas_group_by_name(subscription_name, measurement_group_name):
    """
    Retrieves the measurement group by using sub name and measurement group name

    Args:
        subscription_name (String): Name of the subscription.
        measurement_group_name (String): Name of the measurement group

    Returns:
        MeasurementGroupModel: queried measurement group (or) None
    """
    meas_group = db.session.query(MeasurementGroupModel).filter(
        MeasurementGroupModel.subscription_name == subscription_name,
        MeasurementGroupModel.measurement_group_name == measurement_group_name).one_or_none()
    return meas_group


def query_to_delete_meas_group(subscription_name, measurement_group_name):
    """
    Deletes a measurement group by name

    Args:
        subscription_name (String): Name of the Subscription
        measurement_group_name (String): Name of MG

    Returns:
        int: Returns '1' if subscription exists and deleted successfully else '0'
    """
    affected_rows = db.session.query(MeasurementGroupModel) \
        .filter_by(subscription_name=subscription_name,
                   measurement_group_name=measurement_group_name).delete()
    db.session.commit()
    return affected_rows


def query_get_meas_group_admin_status(subscription_name, measurement_group_name):
    """
    Queries the administrative state by using subscription name and measurement group name

    Args:
        subscription_name (String): Name of the subscription.
        measurement_group_name (String): Name of the measurement group

    Returns:
        administrative_state (String): Admin State (LOCKED, UNLOCKED, LOCKING)
    """
    meas_group = query_meas_group_by_name(subscription_name, measurement_group_name)
    return meas_group.administrative_state


def lock_nf_to_meas_grp(nf_name, measurement_group_name, status):
    """ Deletes a particular nf related to a measurement group name and
        if no more relations of nf exist to measurement group then delete nf from PMSH

    Args:
        nf_name (string): The network function name
        measurement_group_name (string): Measurement group name
        status (string): status of the network function for measurement group
    """
    try:
        delete_nf_to_measurement_group(nf_name, measurement_group_name, status)
        nf_measurement_group_rels = NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == measurement_group_name).all()
        if not nf_measurement_group_rels:
            MeasurementGroupModel.query.filter(
                MeasurementGroupModel.measurement_group_name == measurement_group_name). \
                update({MeasurementGroupModel.administrative_state: AdministrativeState.
                       LOCKED.value}, synchronize_session='evaluate')
            db.session.commit()
    except Exception as e:
        logger.error('Failed update LOCKED status for measurement group name: '
                     f'{measurement_group_name} due to: {e}')


def deactivate_nfs(sub_model, measurement_group, nf_meas_relations):
    """
    Deactivates network functions associated with measurement group

    Args:
        sub_model (SubscriptionModel): Subscription model
        measurement_group (MeasurementGroupModel): Measurement group to update
        nf_meas_relations (list[NfMeasureGroupRelationalModel]): nf to measurement grp relations
    """
    for nf in nf_meas_relations:
        logger.info(f'Saving measurement group to nf name, measure_grp_name: {nf.nf_name},'
                    f'{measurement_group.measurement_group_name}  with DELETE request')
        update_measurement_group_nf_status(measurement_group.measurement_group_name,
                                           SubNfState.PENDING_DELETE.value, nf.nf_name)
        try:
            network_function = NetworkFunction(**nf.serialize_meas_group_nfs())
            logger.info(f'Publishing event for nf name, measure_grp_name: {nf.nf_name},'
                        f'{measurement_group.measurement_group_name} with DELETE request')
            publish_measurement_group(sub_model, measurement_group, network_function, 'DELETE')
        except Exception as ex:
            logger.error(f'Publish event failed for nf name, measure_grp_name, sub_name: '
                         f'{nf.nf_name},{measurement_group.measurement_group_name}, '
                         f'{sub_model.subscription_name} with error: {ex}')


def activate_nfs(sub_model, measurement_group):
    """
    Activates network functions associated with measurement group

    Args:
        sub_model (SubscriptionModel): Subscription model
        measurement_group (MeasurementGroupModel): Measurement group to update
    """
    new_nfs = []
    sub_nf_names = [nf.nf_name for nf in sub_model.nfs]
    filtered_nfs = nf_service.capture_filtered_nfs(sub_model.subscription_name)
    for nf in filtered_nfs:
        if nf.nf_name not in sub_nf_names:
            new_nfs.append(nf)
    if new_nfs:
        logger.info(f'Adding new nfs to the subscription: '
                    f'{sub_model.subscription_name}')
        subscription_service.save_filtered_nfs(new_nfs)
        subscription_service.apply_subscription_to_nfs(new_nfs,
                                                       sub_model.subscription_name)
    for nf in filtered_nfs:
        logger.info(f'Saving measurement group to nf name, measure_grp_name: {nf.nf_name},'
                    f'{measurement_group.measurement_group_name} with CREATE request')

        apply_nf_status_to_measurement_group(nf.nf_name,
                                             measurement_group.measurement_group_name,
                                             SubNfState.PENDING_CREATE.value)
        db.session.commit()
        try:
            network_function = NetworkFunction(**nf.serialize_nf())
            logger.info(f'Publishing event for nf name, measure_grp_name: {nf.nf_name},'
                        f'{measurement_group.measurement_group_name}  with CREATE request')
            publish_measurement_group(sub_model, measurement_group, network_function, 'CREATE')
        except Exception as ex:
            logger.error(f'Publish event failed for nf name, measure_grp_name, sub_name: '
                         f'{nf.nf_name},{measurement_group.measurement_group_name}, '
                         f'{sub_model.subscription_name} with error: {ex}')


def update_admin_status(measurement_group, status):
    """
    Performs administrative status updates for the measurement group

    Args:
        measurement_group (MeasurementGroupModel): Measurement group to update
        status (string): Admin status to update for measurement group

    Raises:
        InvalidDataException: contains details on invalid fields
        DataConflictException: contains details on conflicting state of a field
        Exception: contains runtime error details
    """
    if measurement_group is None:
        raise InvalidDataException('Requested measurement group not available '
                                   'for admin status update')
    elif measurement_group.administrative_state == AdministrativeState.LOCKING.value:
        raise DataConflictException('Cannot update admin status as Locked request is in progress'
                                    f' for sub name: {measurement_group.subscription_name}  and '
                                    f'meas group name: {measurement_group.measurement_group_name}')
    elif measurement_group.administrative_state == status:
        raise InvalidDataException(f'Measurement group is already in {status} state '
                                   f'for sub name: {measurement_group.subscription_name}  and '
                                   f'meas group name: {measurement_group.measurement_group_name}')
    else:
        sub_model = SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == measurement_group.subscription_name) \
            .one_or_none()
        nf_meas_relations = NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == measurement_group.
            measurement_group_name).all()
        if nf_meas_relations and status == AdministrativeState.LOCKED.value:
            status = AdministrativeState.LOCKING.value
        measurement_group.administrative_state = status
        db.session.commit()
        if status == AdministrativeState.LOCKING.value:
            deactivate_nfs(sub_model, measurement_group, nf_meas_relations)
        elif status == AdministrativeState.UNLOCKED.value:
            activate_nfs(sub_model, measurement_group)


def filter_nf_to_meas_grp(nf_name, measurement_group_name, status):
    """ Performs successful status update for a nf under filter update
    request for a particular subscription and measurement group

    Args:
        nf_name (string): The network function name
        measurement_group_name (string): Measurement group name
        status (string): status of the network function for measurement group
    """
    try:
        if status == SubNfState.DELETED.value:
            delete_nf_to_measurement_group(nf_name, measurement_group_name,
                                           SubNfState.DELETED.value)
        elif status == SubNfState.CREATED.value:
            update_measurement_group_nf_status(measurement_group_name,
                                               SubNfState.CREATED.value, nf_name)
        nf_measurement_group_rels = NfMeasureGroupRelationalModel.query.filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == measurement_group_name,
            or_(NfMeasureGroupRelationalModel.nf_measure_grp_status.like('PENDING_%'),
                NfMeasureGroupRelationalModel.nf_measure_grp_status.like('%_FAILED'))
        ).all()
        if not nf_measurement_group_rels:
            MeasurementGroupModel.query.filter(
                MeasurementGroupModel.measurement_group_name == measurement_group_name). \
                update({MeasurementGroupModel.administrative_state: AdministrativeState.
                       UNLOCKED.value}, synchronize_session='evaluate')
            db.session.commit()
    except Exception as e:
        logger.error('Failed update filter response for measurement group name: '
                     f'{measurement_group_name}, nf name: {nf_name} due to: {e}')
