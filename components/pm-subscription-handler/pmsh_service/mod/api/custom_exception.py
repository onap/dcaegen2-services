# ============LICENSE_START===================================================
#  Copyright (C) 2020-2021 Nordix Foundation.
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
        message -- detail on invalid fields
    """

    def __init__(self, invalid_messages):
        self.invalid_messages = invalid_messages


class DuplicateDataException(Exception):
    """Exception raised for invalid inputs.

    Attributes:
        message -- detail on duplicate fields
    """

    def __init__(self, duplicate_fields_info):
        self.duplicate_fields_info = duplicate_fields_info
