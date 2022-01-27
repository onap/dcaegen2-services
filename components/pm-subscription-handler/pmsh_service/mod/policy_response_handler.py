# ============LICENSE_START===================================================
#  Copyright (C) 2020-2022 Nordix Foundation.
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
import json
from mod.pmsh_config import MRTopic, AppConfig
from mod import logger
from mod.subscription import AdministrativeState, subscription_nf_states
from mod.api.db_models import MeasurementGroupModel
from mod.api.services import measurement_group_service

policy_response_handle_functions = {
    AdministrativeState.LOCKED.value: {
        'success': measurement_group_service.delete_nf_to_measurement_group,
        'failed': measurement_group_service.update_measurement_group_nf_status
    },
    AdministrativeState.UNLOCKED.value: {
        'success': measurement_group_service.update_measurement_group_nf_status,
        'failed': measurement_group_service.update_measurement_group_nf_status
    },
    AdministrativeState.LOCKING.value: {
        'success': measurement_group_service.lock_nf_to_meas_grp,
        'failed': measurement_group_service.update_measurement_group_nf_status
    }
}


class PolicyResponseHandler:
    def __init__(self, app):
        self.app = app

    def poll_policy_topic(self):
        """
        This method polls MR for response from policy. It checks whether the message is for the
        relevant subscription and then handles the response
        """
        self.app.app_context().push()
        logger.info('Polling MR for XNF activation/deactivation policy response events.')
        try:
            response_data = AppConfig.get_instance(). \
                get_from_topic(MRTopic.POLICY_PM_SUBSCRIBER.value, 'dcae_pmsh_policy_cl_input')
            for data in response_data:
                data = json.loads(data)
                measurement_group_name = data['status']['measurementGroupName']
                subscription_name = data['status']['subscriptionName']
                measurement_group = (MeasurementGroupModel.query.filter(
                    MeasurementGroupModel.measurement_group_name == measurement_group_name,
                    subscription_name == MeasurementGroupModel
                    .subscription_name).one_or_none())
                nf_name = data['status']['nfName']
                response_message = data['status']['message']
                if measurement_group:
                    self._handle_response(measurement_group_name,
                                          measurement_group.administrative_state,
                                          nf_name, response_message)
                else:
                    logger.info(f'Polled MR response provides missing measurement '
                                f'group name :  {measurement_group_name}')
        except Exception as err:
            logger.error(f'Error trying to poll policy response topic on MR: {err}', exc_info=True)

    @staticmethod
    def _handle_response(measurement_group_name, administrative_state, nf_name, response_message):
        """
        Handles the response from Policy, updating the DB
        Args:
            measurement_group_name (string): The measurement group name
            administrative_state (string): The administrative state of the measurement group
            nf_name (string): The network function name
            response_message (string): The message in the response
                                        regarding the state (success|failed)
        """
        logger.info(f'Response from MR: measurement group name: {measurement_group_name} for '
                    f'NF: {nf_name} received, updating the DB')
        try:
            nf_measure_grp_status = subscription_nf_states[administrative_state][response_message]\
                .value
            policy_response_handle_functions[administrative_state][response_message](
                measurement_group_name=measurement_group_name, status=nf_measure_grp_status,
                nf_name=nf_name)
        except Exception as err:
            logger.error(f'Error changing nf_sub status in the DB: {err}')
            raise
