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
    def __init__(self, mr_pub, app, app_conf, aai_event_thread):
        self.mr_pub = mr_pub
        self.app = app
        self.app_conf = app_conf
        self.aai_event_thread = aai_event_thread

    def execute(self):
        """
        Checks for changes of administrative state in config and proceeds to process
        the Subscription if a change has occurred
        """
        self.app.app_context().push()
        self.app_conf.refresh_config()
        local_admin_state = self.app_conf.subscription.get_local_sub_admin_state()
        new_administrative_state = self.app_conf.subscription.administrativeState
        try:
            if local_admin_state == new_administrative_state:
                logger.info(f'Administrative State did not change in the app config: '
                            f'{new_administrative_state}')
            else:
                self._check_state_change(local_admin_state, new_administrative_state)
        except Exception as err:
            logger.error(f'Error occurred during the activation/deactivation process {err}',
                         exc_info=True)

    def _check_state_change(self, local_admin_state, new_administrative_state):
        if local_admin_state == AdministrativeState.LOCKING.value:
            self._check_for_failed_nfs()
        else:
            if new_administrative_state == AdministrativeState.UNLOCKED.value:
                logger.info(f'Administrative State has changed from {local_admin_state} '
                            f'to {new_administrative_state}.')
                self._activate()
            elif new_administrative_state == AdministrativeState.LOCKED.value:
                logger.info(f'Administrative State has changed from {local_admin_state} '
                            f'to {new_administrative_state}.')
                self._deactivate()
            else:
                logger.error(f'Invalid AdministrativeState: {new_administrative_state}')

    def _activate(self):
        nfs_in_aai = aai.get_pmsh_nfs_from_aai(self.app_conf)
        self.app_conf.subscription.create_subscription_on_nfs(nfs_in_aai, self.mr_pub,
                                                              self.app_conf)
        self.app_conf.subscription.update_subscription_status()
        if not self.aai_event_thread.is_alive():
            logger.info('Start polling for NF info on AAI-EVENT topic on DMaaP MR.')
            self.aai_event_thread.start()

    def _deactivate(self):
        nfs = self.app_conf.subscription.get_network_functions()
        if nfs:
            self.aai_event_thread.cancel()
            logger.info('Stop listening for NFs events on AAI-EVENT topic in MR.')
            self.app_conf.subscription.administrativeState = AdministrativeState.LOCKING.value
            logger.info('Subscription is now LOCKING/DEACTIVATING.')
            self.app_conf.subscription.delete_subscription_from_nfs(nfs, self.mr_pub, self.app_conf)
            self.app_conf.subscription.update_subscription_status()

    def _check_for_failed_nfs(self):
        logger.info('Checking for DELETE_FAILED NFs before LOCKING Subscription.')
        del_failed_nfs = self.app_conf.subscription.get_delete_failed_nfs()
        if del_failed_nfs or self.app_conf.subscription.get_delete_pending_nfs():
            for nf in del_failed_nfs:
                nf_model = nf.get(nf.nf_name)
                if nf_model.retry_count < 3:
                    logger.info(f'Retry deletion of subscription '
                                f'{self.app_conf.subscription.subscriptionName} '
                                f'from NF: {nf.nf_name}')
                    self.app_conf.subscription.delete_subscription_from_nfs([nf], self.mr_pub,
                                                                            self.app_conf)
                    nf.increment_retry_count()
                else:
                    logger.error(f'Failed to delete the subscription '
                                 f'{self.app_conf.subscription.subscriptionName} '
                                 f'from NF: {nf.nf_name} after {nf_model.retry_count} '
                                 f'attempts. Removing NF from DB')
                    nf.delete(nf_name=nf.nf_name)
        else:
            logger.info('Proceeding to LOCKED adminState.')
            self.app_conf.subscription.administrativeState = AdministrativeState.LOCKED.value
            self.app_conf.subscription.update_subscription_status()
