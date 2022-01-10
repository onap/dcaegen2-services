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

from http import HTTPStatus
from mod import logger
from mod.api.services import subscription_service, measurement_group_service
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


def get_subscription_by_name(subscription_name):
    """
    Retrieves subscription based on the name

    Args:
        subscription_name (String): Name of the subscription.

    Returns:
       dict, HTTPStatus: single Sub in PMSH, 200
       dict, HTTPStatus: subscription not defined, 404
       dict, HTTPStatus: Exception details of failure, 500
    """
    logger.info('API call received to fetch subscription by name')
    try:
        subscription = subscription_service.query_subscription_by_name(subscription_name)
        if subscription is not None:
            logger.info(f'queried subscription was successful with the name: {subscription_name}')
            return subscription.serialize(), HTTPStatus.OK.value
        else:
            logger.error('queried subscription was un successful with the name: '
                         f'{subscription_name}')
            return {'error': 'Subscription was not defined with the name : '
                             f'{subscription_name}'}, HTTPStatus.NOT_FOUND.value
    except Exception as exception:
        logger.error(f'While querying the subscription with name: {subscription_name}, '
                     f'it occurred the following exception "{exception}"')
        return {'error': 'Request was not processed due to Exception : '
                         f'{exception}'}, HTTPStatus.INTERNAL_SERVER_ERROR.value


def get_subscriptions():
    """ Retrieves all the subscriptions that are defined in PMSH.

    Returns:
       list (dict), HTTPStatus: All subs in PMSH, 200
       dict, HTTPStatus: Exception details of failure, 500
    """
    logger.info('API call received to fetch all subscriptions')
    try:
        subscriptions = subscription_service.get_subscriptions_list()
        return subscriptions, HTTPStatus.OK.value
    except Exception as exception:
        logger.error(f'The following exception occurred while fetching subscriptions: {exception}')
        return {'error': 'Request was not processed due to Exception : '
                         f'{exception}'}, HTTPStatus.INTERNAL_SERVER_ERROR.value


def get_meas_group_with_nfs(subscription_name, measurement_group_name):
    """
    Retrieves the measurement group and it's associated network functions

    Args:
        subscription_name (String): Name of the subscription.
        measurement_group_name (String): Name of the measurement group

    Returns:
       dict, HTTPStatus: measurement group info with associated nfs, 200
       dict, HTTPStatus: measurement group was not defined, 404
       dict, HTTPStatus: Exception details of failure, 500
    """
    logger.info('API call received to query measurement group and associated network'
                f' functions by using sub name: {subscription_name} and measurement '
                f'group name: {measurement_group_name}')
    try:
        meas_group = measurement_group_service.query_meas_group_by_name(subscription_name,
                                                                        measurement_group_name)
        if meas_group is not None:
            return meas_group.meas_group_with_nfs(), HTTPStatus.OK.value
        else:
            logger.error('measurement group was not defined with the sub name: '
                         f'{subscription_name} and meas group name: '
                         f'{measurement_group_name}')
            return {'error': 'measurement group was not defined with the sub name: '
                             f'{subscription_name} and meas group name: '
                             f'{measurement_group_name}'}, HTTPStatus.NOT_FOUND.value
    except Exception as exception:
        logger.error('The following exception occurred while fetching measurement group: '
                     f'{exception}')
        return {'error': 'Request was not processed due to Exception : '
                         f'{exception}'}, HTTPStatus.INTERNAL_SERVER_ERROR.value


def delete_subscription_by_name(subscription_name):
    """ Deletes subscription based on the name

    Args:
        subscription_name (String): Name of the subscription

    Returns:
       NoneType, HTTPStatus: None, 204
       dict, HTTPStatus: subscription not defined, 404
       dict, HTTPStatus: Reason for not deleting subscription, 409
       dict, HTTPStatus: Exception details of failure, 500
    """
    logger.info('API call received to delete subscription by name')
    try:
        delete_response = subscription_service.delete_subscription_by_name(subscription_name)
        return delete_response
    except Exception as exception:
        logger.error(f'Try again, subscription with name {subscription_name}'
                     f'is not deleted due to following exception: {exception}')
        return {'error': f'Try again, subscription with name {subscription_name}'
                         f'is not deleted due to following exception: {exception}'}, \
            HTTPStatus.INTERNAL_SERVER_ERROR.value
