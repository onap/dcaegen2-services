# ============LICENSE_START===================================================
#  Copyright (C) 2019 Nordix Foundation.
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
import os
import shutil
import time

import mod.pmsh_logging as logger


def main():
    """Entrypoint"""
    if 'PROD_LOGGING' in os.environ:
        logger.create_loggers()
    else:
        tmp_logs_path = '../tmp_logs'
        if os.path.exists(tmp_logs_path):
            shutil.rmtree(tmp_logs_path)
        logger.create_loggers(tmp_logs_path)
    while True:
        time.sleep(30)
        logger.debug("He's not the messiah, he's a very naughty boy!")


if __name__ == '__main__':
    main()
