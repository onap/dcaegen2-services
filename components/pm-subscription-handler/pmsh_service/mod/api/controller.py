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

from mod.subscription import Subscription
from http import HTTPStatus
from mod import logger
from mod.api.services import subscription_service
from connexion import NoContent
from mod.api.custom_exception import InvalidDataException, DuplicateDataException


def status():
    """
    Returns the health of the PMSH service
    Args:
        NA
    Returns:
        Dictionary detailing 'status' of either 'healthy' or 'unhealthy'.
    Raises:
        NA
    """
    return {'status': 'healthy'}


def get_all_sub_to_nf_relations():
    """ Retrieves all subscription to nf relations

    Returns:
        list: of Subscriptions and it's related Network Functions, else empty
    """
    subs_dict = [s.serialize() for s in Subscription.get_all()]
    return subs_dict


def post_subscription(body):
    """
    Creates a subscription

    Args:
        body (dict): subscription request body to save.

    Returns:
        Success : NoContent, 201
        Invalid Data : Invalid message, 400
        Duplicate Data : Duplicate field detail, 409

    Raises:
        Error: If anything fails in the server.
    """
    response = NoContent, HTTPStatus.CREATED.value
    try:
        subscription_service.create_subscription(body['subscription'])
    except DuplicateDataException as e:
        logger.error(f'Failed to create subscription for '
                     f'{body["subscription"]["subscriptionName"]} due to duplicate data: {e}',
                     exc_info=True)
        response = e.duplicate_field_info, HTTPStatus.CONFLICT.value
    except InvalidDataException as e:
        logger.error(f'Failed to create subscription for '
                     f'{body["subscription"]["subscriptionName"]} due to invalid data: {e}',
                     exc_info=True)
        response = e.invalid_message, HTTPStatus.BAD_REQUEST.value
    return response
