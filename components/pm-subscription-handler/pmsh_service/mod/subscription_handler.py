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
import mod.pmsh_logging as logger
from mod.pmsh_utils import ConfigHandler
from mod.subscription import AdministrativeState


class SubscriptionHandler:
    def __init__(self, administrative_state, mr_pub, app, app_conf, aai_event_thread):
        self.current_nfs = None
        self.current_sub = None
        self.administrative_state = administrative_state
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
        config = ConfigHandler.get_pmsh_config()
        new_administrative_state = config['policy']['subscription']['administrativeState']

        try:
            if self.administrative_state == new_administrative_state:
                logger.debug('Administrative State did not change in the Config')
            else:
                self.current_sub, self.current_nfs = aai.get_pmsh_subscription_data(config)
                self.administrative_state = new_administrative_state
                self.current_sub.process_subscription(self.current_nfs, self.mr_pub, self.app_conf)

                if new_administrative_state == AdministrativeState.UNLOCKED.value:
                    logger.debug('Listening to AAI-EVENT topic in MR.')
                    self.aai_event_thread.start()
                else:
                    logger.debug('Stop listening to AAI-EVENT topic in MR.')
                    self.aai_event_thread.cancel()

        except Exception as err:
            logger.debug(f'Error occurred during the activation/deactivation process {err}')
