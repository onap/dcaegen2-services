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

from mod.api.db_models import MeasurementGroupModel, NfMeasureGroupRelationalModel
from mod import db, logger
from mod.api.services import nf_service
from mod.network_function import NetworkFunction
from mod.pmsh_config import MRTopic, AppConfig


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
    new_measurement_group = MeasurementGroupModel(
        subscription_name=subscription_name,
        measurement_group_name=measurement_group['measurementGroupName'],
        administrative_state=measurement_group['administrativeState'],
        file_based_gp=measurement_group['fileBasedGP'],
        file_location=measurement_group['fileLocation'],
        measurement_type=measurement_group['measurementTypes'],
        managed_object_dns_basic=measurement_group['managedObjectDNsBasic'])
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


def publish_measurement_group(sub_model, measurement_group, nf):
    """
    Publishes an event for measurement group against nfs to MR

    Args:
        sub_model(SubscriptionModel): Subscription model object
        measurement_group (MeasurementGroupModel): Measurement group to publish
        nf (NetworkFunction): Network function to publish.
   """
    event_body = nf_service.create_nf_event_body(nf, 'CREATE', sub_model)
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
