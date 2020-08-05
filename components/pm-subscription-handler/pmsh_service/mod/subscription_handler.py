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

import mod.aai_client as aai
from mod import logger
from mod.subscription import AdministrativeState


class SubscriptionHandler:
    def __init__(self, mr_pub, app, app_conf, aai_event_thread, policy_event_thread):
        self.mr_pub = mr_pub
        self.app = app
        self.app_conf = app_conf
        self.aai_event_thread = aai_event_thread
        self.policy_event_thread = policy_event_thread

    def execute(self):
        """
        Checks for changes of administrative state in config and proceeds to process
        the Subscription if a change has occurred
        """
        self.app.app_context().push()
        local_admin_state = self.app_conf.subscription.get_local_sub_admin_state()
        new_administrative_state = self.app_conf.subscription.administrativeState
        try:
            if local_admin_state == new_administrative_state:
                logger.info('Administrative State did not change in the Config')
            else:
                if new_administrative_state == AdministrativeState.UNLOCKED.value:
                    self._activate(local_admin_state, new_administrative_state)
                else:
                    self._deactivate(local_admin_state, new_administrative_state)
        except Exception as err:
            logger.error(f'Error occurred during the activation/deactivation process {err}',
                         exc_info=True)

    def _activate(self, local_admin_state, new_administrative_state):
        existing_nfs_in_aai = aai.get_pmsh_nfs_from_aai(self.app_conf)
        logger.info(f'Administrative State has changed from {local_admin_state} '
                    f'to {new_administrative_state}.')
        self.app_conf.subscription.activate_subscription(existing_nfs_in_aai, self.mr_pub,
                                                         self.app_conf)
        self.app_conf.subscription.update_subscription_status()
        logger.info('Start listening for new NFs on AAI-EVENT topic in MR.')
        self.aai_event_thread.start()
        self.policy_event_thread.start()

    def _deactivate(self, local_admin_state, new_administrative_state):
        logger.info(f'Administrative State has changed from {local_admin_state} '
                    f'to {new_administrative_state}.')
        self.aai_event_thread.cancel()
        logger.info('Stop listening for NFs on AAI-EVENT topic in MR.')
        self.app_conf.subscription.deactivate_subscription(self.mr_pub, self.app_conf)
        self.app_conf.subscription.update_subscription_status()
