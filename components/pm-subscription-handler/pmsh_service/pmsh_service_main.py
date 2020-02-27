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

import mod.aai_client as aai
import mod.pmsh_logging as logger
from mod import db, create_app, launch_api_server
from mod.config_handler import ConfigHandler
from mod.pmsh_utils import AppConfig, PeriodicTask
from mod.policy_response_handler import PolicyResponseHandler
from mod.subscription import Subscription, AdministrativeState
from mod.subscription_handler import SubscriptionHandler


def main():
    try:
        config_handler = ConfigHandler()
        config = config_handler.get_config()
        app_conf = AppConfig(**config['config'])
        app = create_app()
        app.app_context().push()
        db.create_all(app=app)
        sub, nfs = aai.get_pmsh_subscription_data(config)
        policy_mr_pub = app_conf.get_mr_pub('policy_pm_publisher')
        policy_mr_sub = app_conf.get_mr_sub('policy_pm_subscriber')
        mr_aai_event_sub = app_conf.get_mr_sub('aai_subscriber')
        administrative_state = AdministrativeState.LOCKED.value
        subscription_in_db = Subscription.get(sub.subscriptionName)

        if subscription_in_db is not None:
            administrative_state = subscription_in_db.status

        subscription_handler = SubscriptionHandler(config_handler, administrative_state,
                                                   policy_mr_pub, mr_aai_event_sub, app, app_conf)
        policy_response_handler = PolicyResponseHandler(policy_mr_sub, sub.subscriptionName, app)

        subscription_handler_thread = PeriodicTask(30, subscription_handler.execute)
        policy_response_handler_thread = PeriodicTask(5, policy_response_handler.poll_policy_topic)

        subscription_handler_thread.start()
        policy_response_handler_thread.start()

        launch_api_server(app_conf)

    except Exception as e:
        logger.debug(f'Failed to Init PMSH: {e}')
        sys.exit(e)


if __name__ == '__main__':
    main()
