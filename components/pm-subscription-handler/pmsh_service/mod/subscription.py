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
import re

import mod.pmsh_logging as logger
from mod import db
from mod.db_models import SubscriptionModel, NfSubRelationalModel


class Subscription:
    def __init__(self, **kwargs):
        self.subscriptionName = kwargs.get('subscriptionName')
        self.administrativeState = kwargs.get('administrativeState')
        self.fileBasedGP = kwargs.get('fileBasedGP')
        self.fileLocation = kwargs.get('fileLocation')
        self.nfTypeModelInvariantId = kwargs.get('nfTypeModelInvariantId')
        self.nfFilter = kwargs.get('nfFilter')
        self.measurementGroups = kwargs.get('measurementGroups')

    def prepare_subscription_event(self, xnf_name):
        """Prepare the sub event for publishing

        Args:
            xnf_name: the AAI xnf name.

        Returns:
            dict: the Subscription event to be published.
        """
        clean_sub = {k: v for k, v in self.__dict__.items() if k != 'nfFilter'}
        clean_sub.update({'nfName': xnf_name, 'policyName': f'OP-{self.subscriptionName}'})
        return clean_sub

    def create(self):
        """ Creates a subscription database entry

        Returns:
            Subscription object
        """
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

    def add_network_functions_to_subscription(self, nf_list):
        """ Associates network functions to a Subscription

        Args:
            nf_list : A list of NetworkFunction objects.
        """
        current_sub = self.create()
        logger.debug(f'Adding network functions to subscription {current_sub.subscription_name}')

        for nf in nf_list:
            current_nf = nf.create()

            existing_entry = NfSubRelationalModel.query.filter(
                NfSubRelationalModel.subscription_name == current_sub.subscription_name,
                NfSubRelationalModel.nf_name == current_nf.nf_name).one_or_none()
            if existing_entry is None:
                new_nf_sub = NfSubRelationalModel(current_sub.subscription_name, nf.nf_name)
                new_nf_sub.nf = current_nf
                logger.debug(current_nf)
                current_sub.nfs.append(new_nf_sub)

        db.session.add(current_sub)
        db.session.commit()

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
    def get_all_nfs_subscription_relations():
        """ Retrieves all network function to subscription relations

        Returns:
            list: NetworkFunctions per Subscription list else empty
        """
        nf_per_subscriptions = NfSubRelationalModel.query.all()

        return nf_per_subscriptions


class NetworkFunctionFilter:
    def __init__(self, **kwargs):
        self.nf_sw_version = kwargs.get('swVersions')
        self.nf_names = kwargs.get('nfNames')
        self.regex_matcher = re.compile('|'.join(raw_regex for raw_regex in self.nf_names))

    def is_nf_in_filter(self, nf_name):
        """Match the nf name against regex values in Subscription.nfFilter.nfNames

        Args:
            nf_name: the AAI nf name.

        Returns:
            bool: True if matched, else False.
        """
        return self.regex_matcher.search(nf_name)
