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
import time
import threading

import mod.aai_client as aai
import mod.pmsh_logging as logger
from mod import db, create_app
from mod.config_handler import ConfigHandler
from mod.pmsh_utils import AppConfig
from mod.subscription import Subscription, AdministrativeState


def subscription_processor(config_handler, administrative_state, mr_pub, app):
    """
    Checks for changes of administrative state in config and proceeds to process
    the Subscription if a change has occurred

    Args:
        config_handler (ConfigHandler): Configuration Handler used to get config
        administrative_state (str): The administrative state
        mr_pub (_MrPub): MR publisher
        app (db): DB application
    """
    app.app_context().push()
    config = config_handler.get_config()
    sub, nfs = aai.get_pmsh_subscription_data(config)
    new_administrative_state = config['policy']['subscription']['administrativeState']
    polling_period = 30.0

    try:
        if administrative_state == new_administrative_state:
            logger.debug('Administrative State did not change in the Config')
        else:
            sub.process_subscription(nfs, mr_pub)

    except Exception as err:
        logger.debug(f'Error occurred during the activation/deactivation process {err}')

    threading.Timer(polling_period, subscription_processor,
                    [config_handler, new_administrative_state, mr_pub, app]).start()


def main():

    try:
        config_handler = ConfigHandler()
        config = config_handler.get_config()
        app_conf = AppConfig(**config['config'])
        app = create_app()
        app.app_context().push()
        db.create_all(app=app)
        sub, nfs = aai.get_pmsh_subscription_data(config)
        mr_pub = app_conf.get_mr_pub('policy_pm_publisher')
        mr_sub = app_conf.get_mr_sub('policy_pm_subscriber')
        initial_start_delay = 5.0

        administrative_state = AdministrativeState.LOCKED.value
        subscription_in_db = Subscription.get(sub.subscriptionName)
        if subscription_in_db is not None:
            administrative_state = subscription_in_db.status

        threading.Timer(initial_start_delay, subscription_processor,
                        [config_handler, administrative_state, mr_pub, app]).start()

        threading.Timer(20.0, mr_sub.poll_policy_topic, [sub.subscriptionName, app]).start()

    except Exception as e:
        logger.debug(f'Failed to Init PMSH: {e}')
        sys.exit(e)

    while True:
        logger.debug(Subscription.get_all_nfs_subscription_relations())
        time.sleep(5)


if __name__ == '__main__':
    main()
