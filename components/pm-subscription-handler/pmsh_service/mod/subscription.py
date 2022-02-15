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
from enum import Enum


class SubNfState(Enum):
    PENDING_CREATE = 'PENDING_CREATE'
    CREATE_FAILED = 'CREATE_FAILED'
    CREATED = 'CREATED'
    PENDING_DELETE = 'PENDING_DELETE'
    DELETE_FAILED = 'DELETE_FAILED'
    DELETED = 'DELETED'


class AdministrativeState(Enum):
    UNLOCKED = 'UNLOCKED'
    LOCKING = 'LOCKING'
    LOCKED = 'LOCKED'


subscription_nf_states = {
    AdministrativeState.LOCKED.value: {
        'success': SubNfState.DELETED,
        'failed': SubNfState.DELETE_FAILED
    },
    AdministrativeState.UNLOCKED.value: {
        'success': SubNfState.CREATED,
        'failed': SubNfState.CREATE_FAILED
    },
    AdministrativeState.LOCKING.value: {
        'success': SubNfState.DELETED,
        'failed': SubNfState.DELETE_FAILED
    }
}
