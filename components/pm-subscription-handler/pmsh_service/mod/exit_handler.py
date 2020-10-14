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

from mod import logger, db
from mod.subscription import AdministrativeState


class ExitHandler:
    """ Handles PMSH graceful exit when a SIGTERM signal is received.

    Args:
        periodic_tasks (List[PeriodicTask]): PeriodicTasks that needs to be cancelled.
        app_conf (AppConfig): The PMSH Application Configuration.
        subscription_handler (SubscriptionHandler): The subscription handler instance.
    """

    shutdown_signal_received = False

    def __init__(self, *, periodic_tasks, app_conf, subscription_handler):
        self.periodic_tasks = periodic_tasks
        self.app_conf = app_conf
        self.subscription_handler = subscription_handler

    def __call__(self, sig_num, frame):
        logger.info('Graceful shutdown of PMSH initiated.')
        logger.debug(f'ExitHandler was called with signal number: {sig_num}.')
        for thread in self.periodic_tasks:
            if thread.name == 'app_conf_thread':
                logger.info(f'Cancelling thread {thread.name}')
                thread.cancel()
        current_sub = self.app_conf.subscription
        if current_sub.administrativeState == AdministrativeState.UNLOCKED.value:
            try:
                current_sub.deactivate_subscription(self.subscription_handler.mr_pub, self.app_conf)
            except Exception as e:
                logger.error(f'Failed to shut down PMSH application: {e}', exc_info=True)
        for thread in self.periodic_tasks:
            logger.info(f'Cancelling thread {thread.name}')
            thread.cancel()
        logger.info('Closing all DB connections')
        db.session.bind.dispose()
        db.session.close()
        db.engine.dispose()
        ExitHandler.shutdown_signal_received = True
