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
from mod.api.custom_exception import InvalidDataException, DuplicateDataException, \
    DataConflictException


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
        response = e.args[0], HTTPStatus.CONFLICT.value
    except InvalidDataException as e:
        logger.error(f'Failed to create subscription for '
                     f'{body["subscription"]["subscriptionName"]} due to invalid data: {e}',
                     exc_info=True)
        response = e.args[0], HTTPStatus.BAD_REQUEST.value
    return response


def post_meas_group(subscription_name, measurement_group_name, body):
    """
    Creates a measurement group for a subscription

    Args:
        subscription_name (String): Name of the subscription.
        measurement_group_name (String): Name of the measurement group
        body (dict): measurement group request body to save.

    Returns:
        Success : NoContent, 201
        Invalid Data: Invalid message, 400
        Not Found: Subscription no found, 404
        Duplicate Data : Duplicate field detail, 409

    Raises:
        Error: If anything fails in the server.
    """
    response = NoContent, HTTPStatus.CREATED.value
    try:
        subscription = subscription_service.query_subscription_by_name(subscription_name)
        if subscription is not None:
            try:
                measurement_group_service.create_measurement_group(subscription,
                                                                   measurement_group_name, body)
            except DuplicateDataException as e:
                logger.error(f'Failed to create measurement group for '
                             f'{subscription_name} due to duplicate data: {e}',
                             exc_info=True)
                response = e.args[0], HTTPStatus.CONFLICT.value
            except InvalidDataException as e:
                logger.error(f'Failed to create measurement group for '
                             f'{subscription_name} due to invalid data: {e}',
                             exc_info=True)
                response = e.args[0], HTTPStatus.BAD_REQUEST.value
            except Exception as e:
                logger.error(f'Failed to create measurement group due to exception {e}')
                response = e.args[0], HTTPStatus.INTERNAL_SERVER_ERROR.value
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


def delete_meas_group_by_name(subscription_name, measurement_group_name):
    """Deletes the measurement group by name

    Args:
        subscription_name (String): Name of the subscription
        measurement_group_name (String): Name of measurement group

    Returns:
          NoneType, HTTPStatus: None, 204
          dict, HTTPStatus: measurement group not defined, 404
          dict, HTTPStatus: Reason for not deleting measurement group, 409
          dict, HTTPStatus: Exception details of failure, 500
    """
    logger.info(f'API call received to delete measurement group: {measurement_group_name}')
    try:
        measurement_group_administrative_status = \
            measurement_group_service.query_get_meas_group_admin_status(subscription_name,
                                                                        measurement_group_name)
        if measurement_group_administrative_status == \
                measurement_group_service.AdministrativeState.LOCKED.value:
            if measurement_group_service.query_to_delete_meas_group(subscription_name,
                                                                    measurement_group_name) == 1:
                return None, HTTPStatus.NO_CONTENT
            else:
                logger.error(f'Measurement Group not found with name {measurement_group_name}')
                return {'error': f'Measurement Group not found with name '
                                 f'{measurement_group_name}'}, HTTPStatus.NOT_FOUND.value
        else:
            logger.error('Measurement Group was not deleted because the Administrative State '
                         f'was {measurement_group_administrative_status}')
            return {'error': 'Measurement Group was not deleted because the Administrative State '
                             'was {measurement_group_administrative_status}'}, \
                HTTPStatus.CONFLICT.value
    except Exception as e:
        logger.error(f'Try again, measurement group {measurement_group_name} was not'
                     f'deleted due to exception: {e}')
        return {'error': f'Try again, measurement group {measurement_group_name} was not '
                         f'deleted due to exception: {e}'}, HTTPStatus.INTERNAL_SERVER_ERROR.value


def delete_subscription_by_name(subscription_name):
    """ Deletes the subscription by name

    Args:
        subscription_name (String): Name of the subscription

    Returns:
       NoneType, HTTPStatus: None, 204
       dict, HTTPStatus: subscription not defined, 404
       dict, HTTPStatus: Reason for not deleting subscription, 409
       dict, HTTPStatus: Exception details of failure, 500
    """
    logger.info(f'API call received to delete subscription by name: {subscription_name}')
    try:
        unlocked_locking_mgs = \
            subscription_service.query_unlocked_mg_by_sub_name(subscription_name)
        if not unlocked_locking_mgs:
            if subscription_service.query_to_delete_subscription_by_name(subscription_name) == 1:
                return None, HTTPStatus.NO_CONTENT
            else:
                logger.error(f'Subscription is not defined with name {subscription_name}')
                return {'error': f'Subscription is not defined with name {subscription_name}'}, \
                    HTTPStatus.NOT_FOUND.value
        else:
            logger.error('Subscription is not deleted due to associated MGs were UNLOCKED '
                         '(or) under update process to LOCKED')
            return {'error': 'Subscription is not deleted due to the following MGs were UNLOCKED '
                    '(or) under update process to LOCKED', 'measurementGroupNames':
                        [{'measurementGroupName':
                            mg.measurement_group_name}for mg in unlocked_locking_mgs]}, \
                HTTPStatus.CONFLICT.value
    except Exception as exception:
        logger.error(f'Try again, subscription with name {subscription_name}'
                     f'is not deleted due to following exception: {exception}')
        return {'error': f'Try again, subscription with name {subscription_name}'
                         f'is not deleted due to following exception: {exception}'}, \
            HTTPStatus.INTERNAL_SERVER_ERROR.value


def update_admin_state(subscription_name, measurement_group_name, body):
    """
    Performs administrative state update for the respective subscription
    and measurement group name

    Args:
        subscription_name (String): Name of the subscription.
        measurement_group_name (String): Name of the measurement group
        body (dict): Request body with admin state to update.
    Returns:
       string, HTTPStatus: Successfully updated admin state, 200
       string, HTTPStatus: Invalid request details, 400
       string, HTTPStatus: Cannot update as Locked request is in progress, 409
       string, HTTPStatus: Exception details of server failure, 500
    """
    logger.info('Performing administration status update for measurement group '
                f'with sub name: {subscription_name} and measurement '
                f'group name: {measurement_group_name} to {body["administrativeState"]} status')
    response = 'Successfully updated admin state', HTTPStatus.OK.value
    try:
        meas_group = measurement_group_service.query_meas_group_by_name(subscription_name,
                                                                        measurement_group_name)
        measurement_group_service.update_admin_status(meas_group, body["administrativeState"])
    except InvalidDataException as exception:
        logger.error(exception.args[0])
        response = exception.args[0], HTTPStatus.BAD_REQUEST.value
    except DataConflictException as exception:
        logger.error(exception.args[0])
        response = exception.args[0], HTTPStatus.CONFLICT.value
    except Exception as exception:
        logger.error('Update admin status request was not processed for sub name: '
                     f'{subscription_name} and meas group name: '
                     f'{measurement_group_name} due to Exception : {exception}')
        response = 'Update admin status request was not processed for sub name: '\
                   f'{subscription_name} and meas group name: {measurement_group_name}'\
                   f' due to Exception : {exception}', HTTPStatus.INTERNAL_SERVER_ERROR

    return response


def put_nf_filter(subscription_name, body):
    """
    Performs network function filter update for the respective subscription

    Args:
        subscription_name (String): Name of the subscription.
        body (dict): Request body with nf filter data to update.
    Returns:
       string, HTTPStatus: Successfully updated network function Filter, 200
       string, HTTPStatus: Invalid request details, 400
       string, HTTPStatus: Cannot update as Locked/Filtering request is in progress, 409
       string, HTTPStatus: Exception details of server failure, 500
    """
    logger.info('Performing network function filter update for subscription '
                f'with sub name: {subscription_name} ')
    response = 'Successfully updated network function Filter', HTTPStatus.OK.value
    try:
        subscription_service.update_filter(subscription_name, body)
    except InvalidDataException as exception:
        logger.error(exception.args[0])
        response = exception.args[0], HTTPStatus.BAD_REQUEST.value
    except DataConflictException as exception:
        logger.error(exception.args[0])
        response = exception.args[0], HTTPStatus.CONFLICT.value
    except Exception as exception:
        logger.error('Update nf filter request was not processed for sub name: '
                     f'{subscription_name} due to Exception : {exception}')
        response = 'Update nf filter request was not processed for sub name: ' \
                   f'{subscription_name} due to Exception : {exception}', \
                   HTTPStatus.INTERNAL_SERVER_ERROR
    return response
