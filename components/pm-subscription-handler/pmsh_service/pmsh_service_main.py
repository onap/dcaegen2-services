# ============LICENSE_START===================================================
#  Copyright (C) 2019-2020 Nordix Foundation.
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

import mod.aai_client as aai
import mod.pmsh_logging as logger
from mod import db, create_app, launch_api_server
from mod.aai_event_handler import process_aai_events
from mod.exit_handler import ExitHandler
from mod.pmsh_utils import AppConfig, PeriodicTask, ConfigHandler
from mod.policy_response_handler import PolicyResponseHandler
from mod.subscription import Subscription, AdministrativeState
from mod.subscription_handler import SubscriptionHandler


def main():
    try:
        app = create_app()
        app.app_context().push()
        db.create_all(app=app)

        config = ConfigHandler.get_pmsh_config()
        app_conf = AppConfig(**config['config'])

        sub, nfs = aai.get_pmsh_subscription_data(config)
        policy_mr_pub = app_conf.get_mr_pub('policy_pm_publisher')
        policy_mr_sub = app_conf.get_mr_sub('policy_pm_subscriber')
        mr_aai_event_sub = app_conf.get_mr_sub('aai_subscriber')
        subscription_in_db = Subscription.get(sub.subscriptionName)
        administrative_state = subscription_in_db.status if subscription_in_db \
            else AdministrativeState.LOCKED.value

        aai_event_thread = PeriodicTask(10, process_aai_events,
                                        args=(mr_aai_event_sub,
                                              sub, policy_mr_pub, app, app_conf))
        subscription_handler = SubscriptionHandler(administrative_state,
                                                   policy_mr_pub, app, app_conf, aai_event_thread)
        policy_response_handler = PolicyResponseHandler(policy_mr_sub, sub.subscriptionName, app)

        subscription_handler_thread = PeriodicTask(30, subscription_handler.execute)
        policy_response_handler_thread = PeriodicTask(5, policy_response_handler.poll_policy_topic)
        subscription_handler_thread.start()
        policy_response_handler_thread.start()
        periodic_tasks = [aai_event_thread, subscription_handler_thread,
                          policy_response_handler_thread]

        signal(SIGTERM, ExitHandler(periodic_tasks=periodic_tasks,
                                    subscription_handler=subscription_handler))
        launch_api_server(app_conf)

    except Exception as e:
        logger.debug(f'Failed to Init PMSH: {e}')
        sys.exit(e)


if __name__ == '__main__':
    main()
