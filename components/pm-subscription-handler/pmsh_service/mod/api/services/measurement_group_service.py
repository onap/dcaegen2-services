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
from mod.subscription import SubNfState
from mod.api.services import nf_service
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


def apply_nf_to_measgroup(nf_name, measurement_group_name):
    """
    Associate and saves the measurement group with Network function

    Args:
        nf_name (string): Network function name.
        measurement_group_name (string): Measurement group name
    """
    new_nf_measure_grp_rel = NfMeasureGroupRelationalModel(
        measurement_grp_name=measurement_group_name,
        nf_name=nf_name,
        nf_measure_grp_status=SubNfState.PENDING_CREATE.value
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


def measurement_group_encoder(measurement_group):
    """
    Encodes the Measurement Group object as JSON

    Args:
        measurement_group(MeasurementGroupModel): Measurement Group object
    Returns:
        Measurement Group Object in JSON Format
    """
    logger.info(f'Encoding Measurement Group object "{measurement_group}" as JSON')
    return {'measurementGroup': {'measurementGroupName': measurement_group.measurement_group_name,
                                 'administrativeState': measurement_group.administrative_state,
                                 'fileBasedGP': measurement_group.file_based_gp,
                                 'fileLocation': measurement_group.file_location,
                                 'measurementTypes':
                                     measurement_group.measurement_type,
                                 'managedObjectDNsBasic':
                                     measurement_group.managed_object_dns_basic}}
