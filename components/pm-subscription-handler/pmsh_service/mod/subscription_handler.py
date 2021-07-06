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
from jsonschema import ValidationError

from mod import logger, aai_client
from mod.aai_client import get_pmsh_nfs_from_aai
from mod.aai_event_handler import process_aai_events
from mod.network_function_filter import NetworkFunctionFilter, get_nfs_for_creation_and_deletion
from mod.pmsh_utils import PeriodicTask
from mod.subscription import AdministrativeState


class SubscriptionHandler:
    def __init__(self, mr_pub, aai_sub, app, app_conf):
        self.mr_pub = mr_pub
        self.aai_sub = aai_sub
        self.app = app
        self.app_conf = app_conf
        self.aai_event_thread = None

    def execute(self):
        """
        Checks for changes of administrative state in config and proceeds to process
        the Subscription if a change has occurred
        """
        self.app.app_context().push()
        try:
            local_admin_state = self.app_conf.subscription.get_local_sub_admin_state()
            if local_admin_state == AdministrativeState.LOCKING.value:
                self._check_for_failed_nfs()
            else:
                self.app_conf.refresh_config()
                self.app_conf.validate_sub_schema()
                local_admin_state = self.apply_subscription_changes()
                self.compare_admin_state(local_admin_state)
        except (ValidationError, TypeError) as err:
            logger.error(f'Error occurred during validation of subscription schema {err}',
                         exc_info=True)
        except Exception as err:
            logger.error(f'Error occurred during the activation/deactivation process {err}',
                         exc_info=True)

    def apply_subscription_changes(self):
        """ Applies changes to subscription

        Returns:
            Enum: Updated administrative state
        """
        local_admin_state = self.app_conf.subscription.get_local_sub_admin_state()
        if local_admin_state == AdministrativeState.FILTERING.value:
            existing_nfs = self.app_conf.subscription.get_network_functions()
            self.app_conf.nf_filter = \
                NetworkFunctionFilter(**self.app_conf.subscription.nfFilter)
            new_nfs = get_pmsh_nfs_from_aai(self.app_conf)
            self.app_conf.subscription.update_subscription_filter()
            get_nfs_for_creation_and_deletion(existing_nfs, new_nfs, 'delete',
                                              self.mr_pub, self.app_conf)
            get_nfs_for_creation_and_deletion(existing_nfs, new_nfs, 'create',
                                              self.mr_pub, self.app_conf)

        return local_admin_state

    def compare_admin_state(self, local_admin_state):
        """ Check for changes in administrative state

        Args:
            local_admin_state(enum):

        """
        new_administrative_state = self.app_conf.subscription.administrativeState
        if local_admin_state == new_administrative_state:
            logger.info(f'Administrative State did not change in the app config: '
                        f'{new_administrative_state}')
        else:
            self._check_state_change(local_admin_state, new_administrative_state)

    def _check_state_change(self, local_admin_state, new_administrative_state):
        if new_administrative_state == AdministrativeState.UNLOCKED.value:
            logger.info(f'Administrative State has changed from {local_admin_state} '
                        f'to {new_administrative_state}.')
            self._activate(new_administrative_state)
        elif new_administrative_state == AdministrativeState.LOCKED.value:
            logger.info(f'Administrative State has changed from {local_admin_state} '
                        f'to {new_administrative_state}.')
            self._deactivate()
        else:
            raise Exception(f'Invalid AdministrativeState: {new_administrative_state}')

    def _activate(self, new_administrative_state):
        if not self.app_conf.nf_filter:
            self.app_conf.nf_filter = NetworkFunctionFilter(**self.app_conf.subscription.nfFilter)
        self._start_aai_event_thread()
        self.app_conf.subscription.update_sub_params(new_administrative_state,
                                                     self.app_conf.subscription.fileBasedGP,
                                                     self.app_conf.subscription.fileLocation,
                                                     self.app_conf.subscription.measurementGroups)
        nfs_in_aai = aai_client.get_pmsh_nfs_from_aai(self.app_conf)
        self.app_conf.subscription.create_subscription_on_nfs(nfs_in_aai, self.mr_pub,
                                                              self.app_conf)
        self.app_conf.subscription.update_subscription_status()

    def _deactivate(self):
        nfs = self.app_conf.subscription.get_network_functions()
        if nfs:
            self.stop_aai_event_thread()
            self.app_conf.subscription.administrativeState = AdministrativeState.LOCKING.value
            logger.info('Subscription is now LOCKING/DEACTIVATING.')
            self.app_conf.subscription.delete_subscription_from_nfs(nfs, self.mr_pub, self.app_conf)
            self.app_conf.subscription.update_subscription_status()

    def _start_aai_event_thread(self):
        logger.info('Starting polling for NF info on AAI-EVENT topic on DMaaP MR.')
        self.aai_event_thread = PeriodicTask(20, process_aai_events, args=(self.aai_sub,
                                                                           self.mr_pub,
                                                                           self.app,
                                                                           self.app_conf))
        self.aai_event_thread.name = 'aai_event_thread'
        self.aai_event_thread.start()

    def stop_aai_event_thread(self):
        if self.aai_event_thread is not None:
            self.aai_event_thread.cancel()
            self.aai_event_thread = None
            logger.info('Stopping polling for NFs events on AAI-EVENT topic in MR.')

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
