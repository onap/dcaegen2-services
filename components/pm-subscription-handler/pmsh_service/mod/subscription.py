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

from tenacity import retry, retry_if_exception_type, wait_exponential, stop_after_attempt

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

    def prepare_subscription_event(self, xnf_name, app_conf):
        """Prepare the sub event for publishing

        Args:
            xnf_name: the AAI xnf name.
            app_conf (AppConfig): the application configuration.

        Returns:
            dict: the Subscription event to be published.
        """
        clean_sub = {k: v for k, v in self.__dict__.items() if k != 'nfFilter'}
        sub_event = {'nfName': xnf_name, 'policyName': app_conf.operational_policy_name,
                     'changeType': 'DELETE'
                     if self.administrativeState == AdministrativeState.LOCKED.value
                     else 'CREATE', 'closedLoopControlName': app_conf.control_loop_name,
                     'subscription': clean_sub}
        return sub_event

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
                                                     status=self.administrativeState)
                db.session.add(new_subscription)
                db.session.commit()
                return new_subscription
            else:
                logger.debug(f'Subscription {self.subscriptionName} already exists,'
                             f' returning this subscription..')
                return existing_subscription
        except Exception as e:
            logger.debug(f'Failed to create subscription {self.subscriptionName} in the DB: {e}')

    def add_network_function_to_subscription(self, nf):
        """ Associates a network function to a Subscription

        Args:
            nf : A NetworkFunction object.
        """
        current_sub = self.create()
        try:
            current_nf = nf.create()
            logger.debug(f'Adding network function {nf.nf_name} to Subscription '
                         f'{current_sub.subscription_name}')
            existing_entry = NfSubRelationalModel.query.filter(
                NfSubRelationalModel.subscription_name == current_sub.subscription_name,
                NfSubRelationalModel.nf_name == current_nf.nf_name).one_or_none()
            if existing_entry is None:
                new_nf_sub = NfSubRelationalModel(current_sub.subscription_name,
                                                  nf.nf_name, SubNfState.PENDING_CREATE.value)
                new_nf_sub.nf = current_nf
                current_sub.nfs.append(new_nf_sub)
                logger.debug(f'Network function {current_nf.nf_name} added to Subscription '
                             f'{current_sub.subscription_name}')
                db.session.add(current_sub)
                db.session.commit()
        except Exception as e:
            logger.debug(f'Failed to add nf {nf.nf_name} to subscription '
                         f'{current_sub.subscription_name}: {e}')
            logger.debug(f'Subscription {current_sub.subscription_name} now contains these XNFs:'
                         f'{Subscription.get_nf_names_per_sub(current_sub.subscription_name)}')

    @staticmethod
    def get(subscription_name):
        """ Retrieves a subscription

        Args:
            subscription_name (str): The subscription name

        Returns:
            Subscription object else None
        """
        return SubscriptionModel.query.filter(
            SubscriptionModel.subscription_name == subscription_name).one_or_none()

    @staticmethod
    def get_all():
        """ Retrieves a list of subscriptions

        Returns:
            list: Subscription list else empty
        """
        return SubscriptionModel.query.all()

    @staticmethod
    def get_nf_names_per_sub(subscription_name):
        """ Retrieves a list of network function names related to the subscription

        Args:
            subscription_name (str): The subscription name

        Returns:
            list: List of network function names
        """
        nf_sub_rel = NfSubRelationalModel.query.filter(
            NfSubRelationalModel.subscription_name == subscription_name).all()
        list_of_nfs = []
        for nf in nf_sub_rel:
            list_of_nfs.append(nf.nf_name)

        return list_of_nfs

    def update_subscription_status(self):
        """ Updates the status of subscription in subscription table """
        try:
            SubscriptionModel.query.filter(
                SubscriptionModel.subscription_name == self.subscriptionName)\
                .update({SubscriptionModel.status: self.administrativeState},
                        synchronize_session='evaluate')

            db.session.commit()
        except Exception as e:
            logger.debug(f'Failed to update status of subscription: {self.subscriptionName}: {e}')

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
            logger.debug(f'Failed to delete subscription: {self.subscriptionName} '
                         f'and it\'s relations from the DB: {e}')

    @retry(wait=wait_exponential(multiplier=1, min=30, max=120), stop=stop_after_attempt(3),
           retry=retry_if_exception_type(Exception))
    def process_subscription(self, nfs, mr_pub, app_conf):
        action = 'Deactivate'
        sub_nf_state = SubNfState.PENDING_DELETE.value
        self.update_subscription_status()

        if self.administrativeState == AdministrativeState.UNLOCKED.value:
            action = 'Activate'
            sub_nf_state = SubNfState.PENDING_CREATE.value
            logger.info(f'{action} subscription initiated for {self.subscriptionName}.')

        try:
            for nf in nfs:
                mr_pub.publish_subscription_event_data(self, nf.nf_name, app_conf)
                logger.debug(f'Publishing Event to {action} '
                             f'Sub: {self.subscriptionName} for the nf: {nf.nf_name}')
                self.add_network_function_to_subscription(nf)
                self.update_sub_nf_status(self.subscriptionName, sub_nf_state, nf.nf_name)
        except Exception as err:
            raise Exception(f'Error publishing activation event to MR: {err}')

    @staticmethod
    def get_all_nfs_subscription_relations():
        """ Retrieves all network function to subscription relations

        Returns:
            list: NetworkFunctions per Subscription list else empty
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
            logger.debug(f'Failed to update status of nf: {nf_name} for subscription: '
                         f'{subscription_name}: {e}')

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
