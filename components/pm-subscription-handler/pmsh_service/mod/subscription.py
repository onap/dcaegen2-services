# ============LICENSE_START===================================================
#  Copyright (C) 2019-2021 Nordix Foundation.
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
import json
from enum import Enum

from mod import db, logger
from mod.api.db_models import SubscriptionModel, NfSubRelationalModel, NetworkFunctionModel


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
    FILTERING = 'FILTERING'


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
    },
    AdministrativeState.FILTERING.value: {
        'success': SubNfState.DELETED,
        'failed': SubNfState.DELETE_FAILED
    }
}


def _get_nf_objects(nf_sub_relationships):
    nfs = []
    for nf_sub_entry in nf_sub_relationships:
        nf_model_object = NetworkFunctionModel.query.filter(
            NetworkFunctionModel.nf_name == nf_sub_entry.nf_name).one_or_none()
        nfs.append(nf_model_object.to_nf())
    return nfs


def get_nfs_for_creation_and_deletion(existing_nfs, new_nfs, action, mrpub, app_conf):
    """ Finds new/old nfs for creation/deletion from subscription    """
    for existing_nf in existing_nfs:
        _compare_nfs(action, app_conf, existing_nf, mrpub, new_nfs)


def _compare_nfs(action, app_conf, existing_nf, mrpub, new_nfs):
    """ Compares old nfs list to existing nfs list"""
    for new_nf in new_nfs:
        if existing_nf.nf_name != new_nf.nf_name:
            _apply_action_to_nfs(action, app_conf, existing_nf, mrpub, new_nf)


def _apply_action_to_nfs(action, app_conf, existing_nf, mrpub, new_nf):
    """ Performs create/delete of nf from subscription as required"""
    if action == 'create':
        app_conf.subscription.create_subscription_on_nfs([new_nf], mrpub, app_conf)
    elif action == 'delete':
        app_conf.subscription.delete_subscription_from_nfs([existing_nf], mrpub, app_conf)


class Subscription:
    def __init__(self, **kwargs):
        self.subscriptionName = kwargs.get('subscriptionName')
        self.administrativeState = kwargs.get('administrativeState')
        self.fileBasedGP = kwargs.get('fileBasedGP')
        self.fileLocation = kwargs.get('fileLocation')
        self.nfFilter = kwargs.get('nfFilter')
        self.measurementGroups = kwargs.get('measurementGroups')
        self.create()

    def update_sub_params(self, admin_state, file_based_gp, file_location, meas_groups):
        self.administrativeState = admin_state
        self.fileBasedGP = file_based_gp
        self.fileLocation = file_location
        self.measurementGroups = meas_groups

    def create(self):
        """ Creates a subscription database entry

        Returns:
            Subscription object
        """
        existing_subscription = (SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == self.subscriptionName).one_or_none())
        if existing_subscription is None:
            return self.create_new_sub()
        else:
            if existing_subscription.nfFilter and \
                    self._filter_diff(existing_subscription.nfFilter) and \
                    existing_subscription.status == AdministrativeState.UNLOCKED.value:
                return self.update_existing_sub(existing_subscription)

    def update_existing_sub(self, existing_subscription):
        """Update subscription status

        Args:
            existing_subscription: the current subscription

        Returns:
            Subscription: updated subscription
        """
        self.administrativeState = \
            AdministrativeState.FILTERING.value
        self.nfFilter = existing_subscription.nfFilter
        self.update_subscription_status()
        logger.debug(f'Subscription {self.subscriptionName} already exists,'
                     f' returning this subscription..')
        return existing_subscription

    def create_new_sub(self):
        try:
            new_subscription = SubscriptionModel(subscription_name=self.subscriptionName,
                                                 nfFilter=self.nfFilter,
                                                 status=AdministrativeState.LOCKED.value)
            db.session.add(new_subscription)
            db.session.commit()
            return new_subscription
        except Exception as e:
            logger.error(f'Failed to create subscription {self.subscriptionName} in the DB: {e}',
                         exc_info=True)
        finally:
            db.session.remove()

    def _filter_diff(self, existing_subscription_filter):
        existing_subscription_filter, nfFilter = \
            json.dumps(existing_subscription_filter, sort_keys=True), \
            json.dumps(self.nfFilter, sort_keys=True)
        return existing_subscription_filter != nfFilter

    def update_subscription_status(self):
        """ Updates the status of subscription in subscription table """
        try:
            SubscriptionModel.query.filter(
                SubscriptionModel.subscription_name == self.subscriptionName) \
                .update({SubscriptionModel.status: self.administrativeState},
                        synchronize_session='evaluate')

            db.session.commit()
        except Exception as e:
            logger.error(f'Failed to update status of subscription: {self.subscriptionName}: {e}',
                         exc_info=True)
        finally:
            db.session.remove()

    def update_subscription_filter(self):
        """ Updates the filter of subscription in subscription table """
        try:
            SubscriptionModel.query.filter(
                SubscriptionModel.subscription_name == self.subscriptionName) \
                .update({SubscriptionModel.nfFilter: self.nfFilter},
                        synchronize_session='evaluate')

            db.session.commit()
        except Exception as e:
            logger.error(f'Failed to update status of subscription: {self.subscriptionName}: {e}',
                         exc_info=True)
        finally:
            db.session.remove()

    def prepare_subscription_event(self, nf, app_conf):
        """Prepare the sub event for publishing

        Args:
            nf (NetworkFunction): the AAI nf.
            app_conf (AppConfig): the application configuration.

        Returns:
            dict: the Subscription event to be published.
        """
        try:
            clean_sub = {k: v for k, v in self.__dict__.items()
                         if k != 'nfFilter' and k != 'current_filter'}
            if self.administrativeState == AdministrativeState.LOCKING.value:
                change_type = 'DELETE'
            else:
                change_type = 'CREATE'
            sub_event = {'nfName': nf.nf_name,
                         'ipAddress': nf.ip_address,
                         'blueprintName': nf.sdnc_model_name,
                         'blueprintVersion': nf.sdnc_model_version,
                         'policyName': app_conf.operational_policy_name,
                         'changeType': change_type,
                         'closedLoopControlName': app_conf.control_loop_name,
                         'subscription': clean_sub}
            return sub_event
        except Exception as e:
            logger.error(f'Failed to prep Sub event for xNF {nf.nf_name}: {e}', exc_info=True)
            raise

    def add_network_function_to_subscription(self, nf, sub_model):
        """ Associates a network function to a Subscription

        Args:
            sub_model(SubscriptionModel): The SubscriptionModel from the DB.
            nf(NetworkFunction): A NetworkFunction object.
        """
        try:
            current_nf = nf.create()
            existing_entry = NfSubRelationalModel.query.filter(
                NfSubRelationalModel.subscription_name == self.subscriptionName,
                NfSubRelationalModel.nf_name == current_nf.nf_name).one_or_none()
            if existing_entry is None:
                new_nf_sub = NfSubRelationalModel(self.subscriptionName,
                                                  nf.nf_name, SubNfState.PENDING_CREATE.value)
                sub_model.nfs.append(new_nf_sub)
                db.session.add(sub_model)
                db.session.commit()
                logger.info(f'Network function {current_nf.nf_name} added to Subscription '
                            f'{self.subscriptionName}')
        except Exception as e:
            logger.error(f'Failed to add nf {nf.nf_name} to subscription '
                         f'{self.subscriptionName}: {e}', exc_info=True)
            logger.debug(f'Subscription {self.subscriptionName} now contains these XNFs:'
                         f'{[nf.nf_name for nf.nf_name in self.get_network_functions()]}')

    def get(self):
        """ Retrieves a SubscriptionModel object

        Returns:
            SubscriptionModel object else None
        """
        sub_model = SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == self.subscriptionName).one_or_none()
        return sub_model

    def get_local_sub_admin_state(self):
        """ Retrieves the subscription admin state

        Returns:
            str: The admin state of the SubscriptionModel
        """
        sub_model = SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == self.subscriptionName).one_or_none()
        db.session.remove()
        return sub_model.status

    @staticmethod
    def get_all():
        """ Retrieves a list of subscriptions

        Returns:
            list(SubscriptionModel): Subscriptions list else empty
        """

        sub_models = SubscriptionModel.query.all()
        db.session.remove()
        return sub_models

    def create_subscription_on_nfs(self, nfs, mr_pub, app_conf):
        """ Publishes an event to create a Subscription on an nf

        Args:
            nfs(list[NetworkFunction]): A list of NetworkFunction Objects.
            mr_pub (_MrPub): MR publisher
            app_conf (AppConfig): the application configuration.
        """
        try:
            existing_nfs = self.get_network_functions()
            sub_model = self.get()
            for nf in [new_nf for new_nf in nfs if new_nf not in existing_nfs]:
                logger.info(f'Publishing event to create '
                            f'Sub: {self.subscriptionName} on nf: {nf.nf_name}')
                mr_pub.publish_subscription_event_data(self, nf, app_conf)
                self.add_network_function_to_subscription(nf, sub_model)
                self.update_sub_nf_status(self.subscriptionName, SubNfState.PENDING_CREATE.value,
                                          nf.nf_name)
        except Exception as err:
            raise Exception(f'Error publishing create event to MR: {err}')

    def delete_subscription_from_nfs(self, nfs, mr_pub, app_conf):
        """ Publishes an event to delete a Subscription from an nf

        Args:
            nfs(list[NetworkFunction]): A list of NetworkFunction Objects.
            mr_pub (_MrPub): MR publisher
            app_conf (AppConfig): the application configuration.
        """
        try:
            for nf in nfs:
                logger.debug(f'Publishing Event to delete '
                             f'Sub: {self.subscriptionName} from the nf: {nf.nf_name}')
                mr_pub.publish_subscription_event_data(self, nf, app_conf)
                self.update_sub_nf_status(self.subscriptionName,
                                          SubNfState.PENDING_DELETE.value,
                                          nf.nf_name)
        except Exception as err:
            raise Exception(f'Error publishing delete event to MR: {err}')

    @staticmethod
    def get_all_nfs_subscription_relations():
        """ Retrieves all network function to subscription relations

        Returns:
            list(NfSubRelationalModel): NetworkFunctions per Subscription list else empty
        """
        nf_per_subscriptions = NfSubRelationalModel.query.all()
        db.session.remove()
        return nf_per_subscriptions

    @staticmethod
    def update_sub_nf_status(subscription_name, status, nf_name):
        """ Updates the status of the subscription for a particular nf

        Args:
            subscription_name (str): The subscription name
            nf_name (str): The network function name
            status (str): Status of the subscription
        """
        try:
            NfSubRelationalModel.query.filter(
                NfSubRelationalModel.subscription_name == subscription_name,
                NfSubRelationalModel.nf_name == nf_name). \
                update({NfSubRelationalModel.nf_sub_status: status}, synchronize_session='evaluate')
            db.session.commit()
        except Exception as e:
            logger.error(f'Failed to update status of nf: {nf_name} for subscription: '
                         f'{subscription_name}: {e}', exc_info=True)

    def get_network_functions(self):
        nf_sub_relationships = NfSubRelationalModel.query.filter(
            NfSubRelationalModel.subscription_name == self.subscriptionName)
        nfs = _get_nf_objects(nf_sub_relationships)
        db.session.remove()
        return nfs

    def get_delete_failed_nfs(self):
        nf_sub_relationships = NfSubRelationalModel.query.filter(
            NfSubRelationalModel.subscription_name == self.subscriptionName,
            NfSubRelationalModel.nf_sub_status == SubNfState.DELETE_FAILED.value)
        nfs = _get_nf_objects(nf_sub_relationships)
        db.session.remove()
        return nfs

    def get_delete_pending_nfs(self):
        nf_sub_relationships = NfSubRelationalModel.query.filter(
            NfSubRelationalModel.subscription_name == self.subscriptionName,
            NfSubRelationalModel.nf_sub_status == SubNfState.PENDING_DELETE.value)
        nfs = _get_nf_objects(nf_sub_relationships)
        db.session.remove()
        return nfs
