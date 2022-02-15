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
import uuid
from functools import wraps
from os import getenv
from threading import Timer

from onaplogging.mdcContext import MDC
from mod import logger


def mdc_handler(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        request_id = str(uuid.uuid1())
        invocation_id = str(uuid.uuid1())
        MDC.put('ServiceName', getenv('HOSTNAME'))
        MDC.put('RequestID', request_id)
        MDC.put('InvocationID', invocation_id)

        kwargs['request_id'] = request_id
        kwargs['invocation_id'] = invocation_id
        return func(*args, **kwargs)

    return wrapper


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
