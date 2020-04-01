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

from mod.pmsh_utils import logger
from mod.subscription import AdministrativeState
from mod.network_function import NetworkFunction


class ExitHandler:
    """ Handles PMSH graceful exit when a SIGTERM signal is received.

    Args:
        periodic_tasks (List[PeriodicTask]): PeriodicTasks that needs to be cancelled.
        subscription_handler (SubscriptionHandler): The subscription handler instance.
    """

    shutdown_signal_received = False

    def __init__(self, *, periodic_tasks, subscription_handler):
        self.periodic_tasks = periodic_tasks
        self.subscription_handler = subscription_handler

    def __call__(self, sig_num, frame):
        logger.debug(f'ExitHandler was called with signal number: {sig_num}.')
        current_sub = self.subscription_handler.current_sub
        if current_sub and current_sub.administrativeState == AdministrativeState.UNLOCKED.value:
            for thread in self.periodic_tasks:
                logger.debug(f'Cancelling periodic task with thread name: {thread.name}.')
                thread.cancel()
            current_sub.administrativeState = AdministrativeState.LOCKED.value
            nf_model_object_list = NetworkFunction.get_nf_model_objects_from_relationship(
                current_sub.subscriptionName)
            nfs = NetworkFunction.get_nf_objects_from_nf_model_objects(nf_model_object_list)
            current_sub.process_subscription(nfs,
                                             self.subscription_handler.mr_pub,
                                             self.subscription_handler.app_conf)
        ExitHandler.shutdown_signal_received = True
