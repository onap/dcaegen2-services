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

import json

from mod import logger
from mod.network_function import NetworkFunction
from mod.subscription import Subscription, AdministrativeState, subscription_nf_states

policy_response_handle_functions = {
    AdministrativeState.LOCKED.value: {
        'success': NetworkFunction.delete,
        'failed': Subscription.update_sub_nf_status
    },
    AdministrativeState.UNLOCKED.value: {
        'success': Subscription.update_sub_nf_status,
        'failed': Subscription.update_sub_nf_status
    },
    AdministrativeState.FILTERING.value: {
        'success': NetworkFunction.delete,
        'failed': Subscription.update_sub_nf_status
    },
    AdministrativeState.LOCKING.value: {
        'success': NetworkFunction.delete,
        'failed': Subscription.update_sub_nf_status
    }
}


class PolicyResponseHandler:
    def __init__(self, mr_sub, app_conf, app):
        self.mr_sub = mr_sub
        self.app_conf = app_conf
        self.app = app

    def poll_policy_topic(self):
        """
        This method polls MR for response from policy. It checks whether the message is for the
        relevant subscription and then handles the response
        """
        self.app.app_context().push()
        administrative_state = self.app_conf.subscription.administrativeState
        logger.info('Polling MR for XNF activation/deactivation policy response events.')
        try:
            response_data = self.mr_sub.get_from_topic('dcae_pmsh_policy_cl_input')
            for data in response_data:
                data = json.loads(data)
                if data['status']['subscriptionName'] \
                        == self.app_conf.subscription.subscriptionName:
                    nf_name = data['status']['nfName']
                    response_message = data['status']['message']
                    self._handle_response(self.app_conf.subscription.subscriptionName,
                                          administrative_state, nf_name, response_message)
        except Exception as err:
            logger.error(f'Error trying to poll policy response topic on MR: {err}', exc_info=True)

    @staticmethod
    def _handle_response(subscription_name, administrative_state, nf_name, response_message):
        """
        Handles the response from Policy, updating the DB

        Args:
            subscription_name (str): The subscription name
            administrative_state (str): The administrative state of the subscription
            nf_name (str): The network function name
            response_message (str): The message in the response regarding the state (success|failed)
        """
        logger.info(f'Response from MR: Sub: {subscription_name} for '
                    f'NF: {nf_name} received, updating the DB')
        try:
            sub_nf_status = subscription_nf_states[administrative_state][response_message].value
            policy_response_handle_functions[administrative_state][response_message](
                subscription_name=subscription_name, status=sub_nf_status, nf_name=nf_name)
        except Exception as err:
            logger.error(f'Error changing nf_sub status in the DB: {err}')
            raise
