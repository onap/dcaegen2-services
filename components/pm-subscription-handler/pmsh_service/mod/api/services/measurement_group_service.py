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

from mod.api.db_models import MeasurementGroupModel, NfMeasureGroupRelationalModel
from mod import db
from mod.subscription import SubNfState
from mod.api.services import nf_service
from flask import current_app


def validate_measurement_group(measurement_group, subscription_name):
    invalid_messages = []
    existing_measurement_grp = (MeasurementGroupModel.query.filter(
        MeasurementGroupModel.measurement_group_name == measurement_group['measurementGroupName'],
        MeasurementGroupModel.subscription_name == subscription_name)
        .one_or_none())
    if existing_measurement_grp is not None:
        invalid_messages.append(f'Measurement Group: {measurement_group["measurementGroupName"]} '
                                f' for Subscription: {subscription_name} '
                                f'already exists.')
    return invalid_messages


def save_measurement_group(measurement_group, subscription_name):
    new_measurement_group = MeasurementGroupModel(
        subscription_name=subscription_name,
        measurement_group_name=measurement_group['measurementGroupName'],
        administrative_state=measurement_group['administrativeState'],
        file_based_gp=measurement_group['fileBasedGP'],
        file_location=measurement_group['fileLocation'],
        measurement_type=measurement_group['measurementTypes'],
        managed_object_dns_basic=measurement_group['managedObjectDNsBasic'])
    db.session.add(new_measurement_group)


def apply_nf(nf, measurement_group):
    new_nf_measure_grp_rel = NfMeasureGroupRelationalModel(
        measurement_grp_name=measurement_group['measurementGroupName'],
        nf_name=nf.nf_name,
        nf_measure_grp_status=SubNfState.PENDING_CREATE.value
    )
    db.session.add(new_nf_measure_grp_rel)


def publish_measurement_group(subscription_name, measurement_group, nfs):
    app_conf = current_app.config['app_config']
    event_body = {"subscriptionName": subscription_name,
                  "measurementGroup": measurement_group,
                  "networkFunctions": [nf_service.create_nf_event_body(nf, 'CREATE')
                                       for nf in nfs]}
    policy_mr_pub = app_conf.get_mr_pub('policy_pm_publisher')
    policy_mr_pub.publish_to_topic(event_body)
