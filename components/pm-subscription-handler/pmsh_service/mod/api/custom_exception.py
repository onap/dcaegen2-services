# ============LICENSE_START===================================================
#  Copyright (C) 2021 Nordix Foundation.
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

class InvalidDataException(Exception):
    """Exception raised for invalid inputs.

    Attributes:
        message -- detail on invalid data
    """

    def __init__(self, invalid_message):
        self.invalid_message = invalid_message


class DuplicateDataException(Exception):
    """Exception raised for duplicate inputs.

    Attributes:
        message -- detail on duplicate field
    """

    def __init__(self, duplicate_field_info):
        self.duplicate_field_info = duplicate_field_info


class DataConflictException(Exception):
    """Exception raised for conflicting data state in PMSH.

    Attributes:
        message -- detail on conflicting data
    """

    def __init__(self, data_conflict_message):
        self.data_conflict_message = data_conflict_message
