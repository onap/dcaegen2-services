# ============LICENSE_START===================================================
#  Copyright (C) 2020 Nordix Foundation.
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

from tenacity import retry, wait_fixed, retry_if_exception_type

import mod.pmsh_logging as logger
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
    }
}


class PolicyResponseHandler:
    def __init__(self, mr_sub, subscription_name, app):
        self.mr_sub = mr_sub
        self.subscription_name = subscription_name
        self.app = app

    @retry(wait=wait_fixed(5), retry=retry_if_exception_type(Exception))
    def poll_policy_topic(self):
        """
        This method polls MR for response from policy. It checks whether the message is for the
        relevant subscription and then handles the response
        """
        self.app.app_context().push()
        administrative_state = Subscription.get(self.subscription_name).status
        try:
            response_data = self.mr_sub.get_from_topic('policy_response_consumer')
            for data in response_data:
                data = json.loads(data)
                if data['status']['subscriptionName'] == self.subscription_name:
                    nf_name = data['status']['nfName']
                    response_message = data['status']['message']
                    self._handle_response(self.subscription_name, administrative_state,
                                          nf_name, response_message)
        except Exception as err:
            raise Exception(f'Error trying to poll policy response topic on MR: {err}')

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
        logger.debug(f'Response from MR: Sub: {subscription_name} for '
                     f'NF: {nf_name} received, updating the DB')
        try:
            sub_nf_status = subscription_nf_states[administrative_state][response_message].value
            policy_response_handle_functions[administrative_state][response_message](
                subscription_name=subscription_name, status=sub_nf_status, nf_name=nf_name)
        except Exception as err:
            raise Exception(f'Error changing nf_sub status in the DB: {err}')
