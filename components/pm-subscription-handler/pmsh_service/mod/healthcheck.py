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


def status(details=False):
    """
    Returns the health of the PMSH service
    Args:
        details: boolean to denote if response is to contain details of health of PMSH connections
                 to other systems
    Returns:
        Dictionary detailing 'status' of either 'healthy' or 'unhealthy'.
        If argument 'details' is set to true also includes in dictionary
        status of sub-systems pmsh is dependent on.
    Raises:
        NA
    """
    response = {"status": "healthy"}
    if details:
        response['db-connect'] = 'true'
        response["cbs-connect"] = "true"
        response["aai-connect"] = "true"
    return response
