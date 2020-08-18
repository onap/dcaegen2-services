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
from enum import Enum

from mod import db, logger
from mod.api.db_models import SubscriptionModel, NfSubRelationalModel, NetworkFunctionModel
from mod.network_function import NetworkFunction


class SubNfState(Enum):
    PENDING_CREATE = 'PENDING_CREATE'
    CREATE_FAILED = 'CREATE_FAILED'
    CREATED = 'CREATED'
    PENDING_DELETE = 'PENDING_DELETE'
    DELETE_FAILED = 'DELETE_FAILED'


class AdministrativeState(Enum):
    UNLOCKED = 'UNLOCKED'
    LOCKED = 'LOCKED'
    PENDING = 'PENDING'


subscription_nf_states = {
    AdministrativeState.LOCKED.value: {
        'success': SubNfState.CREATED,
        'failed': SubNfState.DELETE_FAILED
    },
    AdministrativeState.UNLOCKED.value: {
        'success': SubNfState.CREATED,
        'failed': SubNfState.CREATE_FAILED
    }
}


class Subscription:
    def __init__(self, **kwargs):
        self.subscriptionName = kwargs.get('subscriptionName')
        self.administrativeState = kwargs.get('administrativeState')
        self.fileBasedGP = kwargs.get('fileBasedGP')
        self.fileLocation = kwargs.get('fileLocation')
        self.nfFilter = kwargs.get('nfFilter')
        self.measurementGroups = kwargs.get('measurementGroups')
        self.create()

    def create(self):
        """ Creates a subscription database entry

        Returns:
            Subscription object
        """
        try:
            existing_subscription = (SubscriptionModel.query.filter(
                SubscriptionModel.subscription_name == self.subscriptionName).one_or_none())
            if existing_subscription is None:
                new_subscription = SubscriptionModel(subscription_name=self.subscriptionName,
                                                     status=AdministrativeState.PENDING.value)
                db.session.add(new_subscription)
                db.session.commit()
                return new_subscription
            else:
                logger.debug(f'Subscription {self.subscriptionName} already exists,'
                             f' returning this subscription..')
                return existing_subscription
        except Exception as e:
            logger.error(f'Failed to create subscription {self.subscriptionName} in the DB: {e}',
                         exc_info=True)

    def update_subscription_status(self):
        """ Updates the status of subscription in subscription table """
        try:
            SubscriptionModel.query.filter(
                SubscriptionModel.subscription_name == self.subscriptionName)\
                .update({SubscriptionModel.status: self.administrativeState},
                        synchronize_session='evaluate')

            db.session.commit()
        except Exception as e:
            logger.error(f'Failed to update status of subscription: {self.subscriptionName}: {e}',
                         exc_info=True)

    def delete_subscription(self):
        """ Deletes a subscription and all its association from the database. A network function
        that is only associated with the subscription being removed will also be deleted."""
        try:
            subscription = SubscriptionModel.query.filter(
                SubscriptionModel.subscription_name == self.subscriptionName).one_or_none()
            if subscription:
                for nf_relationship in subscription.nfs:
                    other_nf_relationship = NfSubRelationalModel.query.filter(
                        NfSubRelationalModel.subscription_name != self.subscriptionName,
                        NfSubRelationalModel.nf_name == nf_relationship.nf_name).one_or_none()
                    if not other_nf_relationship:
                        db.session.delete(nf_relationship.nf)
                db.session.delete(subscription)
                db.session.commit()
        except Exception as e:
            logger.error(f'Failed to delete subscription: {self.subscriptionName} '
                         f'and it\'s relations from the DB: {e}', exc_info=True)

    def prepare_subscription_event(self, xnf_name, app_conf):
        """Prepare the sub event for publishing

        Args:
            xnf_name: the AAI xnf name.
            app_conf (AppConfig): the application configuration.

        Returns:
            dict: the Subscription event to be published.
        """
        try:
            clean_sub = {k: v for k, v in self.__dict__.items() if k != 'nfFilter'}
            sub_event = {'nfName': xnf_name, 'policyName': app_conf.operational_policy_name,
                         'changeType': 'DELETE'
                         if self.administrativeState == AdministrativeState.LOCKED.value
                         else 'CREATE', 'closedLoopControlName': app_conf.control_loop_name,
                         'subscription': clean_sub}
            return sub_event
        except Exception as e:
            logger.error(f'Failed to prep Sub event for xNF {xnf_name}: {e}', exc_info=True)
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
                         f'{Subscription.get_nf_names_per_sub(self.subscriptionName)}')

    def get(self):
        """ Retrieves a SubscriptionModel object

        Returns:
            SubscriptionModel object else None
        """
        return SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == self.subscriptionName).one_or_none()

    def get_local_sub_admin_state(self):
        """ Retrieves the subscription admin state

        Returns:
            str: The admin state of the SubscriptionModel
        """
        sub_model = SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == self.subscriptionName).one_or_none()
        return sub_model.status

    @staticmethod
    def get_all():
        """ Retrieves a list of subscriptions

        Returns:
            list(SubscriptionModel): Subscriptions list else empty
        """
        return SubscriptionModel.query.all()

    @staticmethod
    def get_nf_names_per_sub(subscription_name):
        """ Retrieves a list of network function names related to the subscription

        Args:
            subscription_name (str): The subscription name

        Returns:
            list(str): List of network function names
        """
        nf_sub_rel = NfSubRelationalModel.query.filter(
            NfSubRelationalModel.subscription_name == subscription_name).all()
        list_of_nfs = []
        for nf in nf_sub_rel:
            list_of_nfs.append(nf.nf_name)

        return list_of_nfs

    def activate_subscription(self, nfs, mr_pub, app_conf):
        logger.info(f'Activate subscription initiated for {self.subscriptionName}.')
        try:
            sub_model = self.get()
            for nf in nfs:
                mr_pub.publish_subscription_event_data(self, nf.nf_name, app_conf)
                logger.info(f'Publishing event to activate '
                            f'Sub: {self.subscriptionName} for the nf: {nf.nf_name}')
                self.add_network_function_to_subscription(nf, sub_model)
                self.update_sub_nf_status(self.subscriptionName, SubNfState.PENDING_CREATE.value,
                                          nf.nf_name)
        except Exception as err:
            raise Exception(f'Error publishing activation event to MR: {err}')

    def deactivate_subscription(self, mr_pub, app_conf):
        nfs = self.get_network_functions()
        try:
            if nfs:
                logger.info(f'Deactivate subscription initiated for {self.subscriptionName}.')
                for nf in nfs:
                    mr_pub.publish_subscription_event_data(self, nf.nf_name, app_conf)
                    logger.debug(f'Publishing Event to deactivate '
                                 f'Sub: {self.subscriptionName} for the nf: {nf.nf_name}')
                    self.update_sub_nf_status(self.subscriptionName,
                                              SubNfState.PENDING_DELETE.value,
                                              nf.nf_name)
        except Exception as err:
            raise Exception(f'Error publishing deactivation event to MR: {err}')

    @staticmethod
    def get_all_nfs_subscription_relations():
        """ Retrieves all network function to subscription relations

        Returns:
            list(NfSubRelationalModel): NetworkFunctions per Subscription list else empty
        """
        nf_per_subscriptions = NfSubRelationalModel.query.all()
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

    def _get_nf_models(self):
        nf_sub_relationships = NfSubRelationalModel.query.filter(
            NfSubRelationalModel.subscription_name == self.subscriptionName)
        nf_models = []
        for nf_sub_entry in nf_sub_relationships:
            nf_model_object = NetworkFunctionModel.query.filter(
                NetworkFunctionModel.nf_name == nf_sub_entry.nf_name).one_or_none()
            nf_models.append(nf_model_object)

        return nf_models

    def get_network_functions(self):
        nfs = []
        nf_models = self._get_nf_models()
        for nf_model in nf_models:
            nf = NetworkFunction(
                nf_name=nf_model.nf_name,
                orchestration_status=nf_model.orchestration_status
            )
            nfs.append(nf)

        return nfs
