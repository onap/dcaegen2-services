# ============LICENSE_START===================================================
#  Copyright (C) 2020-2023 Nordix Foundation.
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
from mod.api.services import subscription_service, measurement_group_service
from mod.api.services.measurement_group_service import AdministrativeState


class ExitHandler:
    """ Handles PMSH graceful exit when a SIGTERM signal is received.

    Args:
        periodic_tasks (List[PeriodicTask]): PeriodicTasks that needs to be cancelled.
    """

    shutdown_signal_received = False

    def __init__(self, *, periodic_tasks):
        self.periodic_tasks = periodic_tasks

    def __call__(self, sig_num, frame):
        logger.info('Graceful shutdown of PMSH initiated.')
        logger.debug(f'ExitHandler was called with signal number: {sig_num}.')
        try:
            subscriptions_all = subscription_service.query_all_subscriptions()
            for subscription in subscriptions_all:
                for mg in subscription.measurement_groups:
                    if mg.administrative_state == AdministrativeState.UNLOCKED.value:
                        measurement_group_service.\
                            update_admin_status(mg, AdministrativeState.LOCKED.value)
        except Exception as e:
            logger.error(f'Failed to shut down PMSH application: {e}', exc_info=True)
        for thread in self.periodic_tasks:
            logger.info(f'Cancelling thread {thread.name}')
            thread.cancel()
        logger.info('Closing all DB connections')
        if db.session.bind is not None:
            db.session.bind.dispose()
        db.session.close()
        db.engine.dispose()
        ExitHandler.shutdown_signal_received = True
