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

import mod.aai_client as aai_client
import mod.pmsh_logging as logger
from mod import db, create_prod_app
from mod.config_handler import ConfigHandler
from mod.subscription import Subscription


def main():

    try:
        app = create_prod_app()
        app.app_context().push()
        db.create_all(app=app)

        config_handler = ConfigHandler()
        cbs_data = config_handler.get_config()
        subscription, xnfs = aai_client.get_pmsh_subscription_data(cbs_data)
        subscription.add_network_functions_to_subscription(xnfs)
    except Exception as e:
        logger.debug(f'Failed to Init PMSH: {e}')
        sys.exit(e)

    while True:
        logger.debug(Subscription.get_all_nfs_subscription_relations())
        time.sleep(5)


if __name__ == '__main__':
    main()
