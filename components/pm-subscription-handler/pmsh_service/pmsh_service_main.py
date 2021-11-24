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
import sys

from mod import db, create_app, launch_api_server, logger
from mod.pmsh_config import AppConfig as NewAppConfig


def main():
    try:
        try:
            app = create_app()
            app.app_context().push()
            db.create_all(app=app)
            pmsh_app_conf = NewAppConfig()
        except Exception as e:
            logger.error(f'Failed to get config and create application: {e}', exc_info=True)
            sys.exit(e)
        launch_api_server(pmsh_app_conf)

    except Exception as e:
        logger.error(f'Failed to initialise PMSH: {e}', exc_info=True)
        sys.exit(e)


if __name__ == '__main__':
    main()
