# ============LICENSE_START===================================================
#  Copyright (C) 2019-2022 Nordix Foundation.
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
import sys
from signal import signal, SIGTERM
from mod.aai_event_handler import AAIEventHandler
from mod import db, create_app, launch_api_server, logger
from mod.exit_handler import ExitHandler
from mod.pmsh_config import AppConfig
from mod.policy_response_handler import PolicyResponseHandler
from threading import Timer


class PeriodicTask(Timer):
    """
    See :class:`Timer`.
    """

    def run(self):
        self.function(*self.args, **self.kwargs)
        while not self.finished.wait(self.interval):
            try:
                self.function(*self.args, **self.kwargs)
            except Exception as e:
                logger.error(f'Exception in thread: {self.name}: {e}', exc_info=True)


def main():
    try:
        try:
            app = create_app()
            app.app_context().push()
            db.create_all(app=app)
            pmsh_app_conf = AppConfig()
        except Exception as e:
            logger.error(f'Failed to get config and create application: {e}', exc_info=True)
            sys.exit(e)

        policy_response_handler = PolicyResponseHandler(app)
        policy_response_handler_thread = PeriodicTask(25, policy_response_handler.poll_policy_topic)
        policy_response_handler_thread.name = 'policy_event_thread'
        logger.info('Start polling PMSH_CL_INPUT topic on DMaaP MR.')
        policy_response_handler_thread.start()

        aai_event_handler = AAIEventHandler(app)
        aai_event_handler_thread = PeriodicTask(20, aai_event_handler.execute)
        aai_event_handler_thread.name = 'aai_event_thread'
        aai_event_handler_thread.start()

        periodic_tasks = [policy_response_handler_thread,
                          aai_event_handler_thread]
        signal(SIGTERM, ExitHandler(periodic_tasks=periodic_tasks))
        launch_api_server(pmsh_app_conf)

    except Exception as e:
        logger.error(f'Failed to initialise PMSH: {e}', exc_info=True)
        sys.exit(e)


if __name__ == '__main__':
    main()
