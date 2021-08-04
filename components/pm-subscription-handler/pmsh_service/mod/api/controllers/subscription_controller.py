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

from http import HTTPStatus
from mod.api.services import subscription_service
from connexion import NoContent
from mod.api.custom_exception import InvalidDataException, DuplicateDataException


def post_subscription(body):
    """
        Creates a subscription

        Args:
            body (dict): subscription request body to save.

        Returns:
            Success : NoContent, 201
            Invalid Data : List of Invalid messages, 400

        Raises:
            Error: If anything fails in the server.
    """
    response = NoContent, HTTPStatus.CREATED.value
    try:
        subscription_service.create_subscription(body['subscription'])
    except DuplicateDataException as exception:
        response = exception.duplicate_fields_info, HTTPStatus.CONFLICT.value
    except InvalidDataException as exception:
        response = exception.invalid_messages, HTTPStatus.BAD_REQUEST.value
    return response
