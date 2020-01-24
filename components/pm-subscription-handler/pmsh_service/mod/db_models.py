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

from sqlalchemy import Column, Integer, String, ForeignKey
from sqlalchemy.orm import relationship

from mod import db


class SubscriptionModel(db.Model):
    __tablename__ = 'subscriptions'
    id = Column(Integer, primary_key=True, autoincrement=True)
    subscription_name = Column(String(100), unique=True)
    status = Column(String(20))

    nfs = relationship(
        'NfSubRelationalModel',
        cascade='all, delete-orphan',
        backref='subscription')

    def __init__(self, subscription_name, status):
        self.subscription_name = subscription_name
        self.status = status

    def __repr__(self):
        return f'Subscription: {self.subscription_name}  {self.status}'

    def __eq__(self, other):
        if isinstance(self, other.__class__):
            return self.subscription_name == other.subscription_name
        return False


class NetworkFunctionModel(db.Model):
    __tablename__ = 'network_functions'
    id = Column(Integer, primary_key=True, autoincrement=True)
    nf_name = Column(String(100), unique=True)
    orchestration_status = Column(String(100))

    subscriptions = relationship(
        'NfSubRelationalModel',
        cascade='all, delete-orphan',
        backref='nf')

    def __init__(self, nf_name, orchestration_status):
        self.nf_name = nf_name
        self.orchestration_status = orchestration_status

    def __repr__(self):
        return f'NetworkFunctionModel: {self.nf_name}, {self.orchestration_status}'


class NfSubRelationalModel(db.Model):
    __tablename__ = 'nf_to_sub_rel'
    id = Column(Integer, primary_key=True, autoincrement=True)
    subscription_name = Column(
        String,
        ForeignKey(SubscriptionModel.subscription_name, ondelete='cascade', onupdate='cascade')
    )
    nf_name = Column(
        String,
        ForeignKey(NetworkFunctionModel.nf_name, ondelete='cascade', onupdate='cascade')
    )
    nf_sub_status = Column(String(20))

    def __init__(self, subscription_name, nf_name, nf_sub_status=None):
        self.subscription_name = subscription_name
        self.nf_name = nf_name
        self.nf_sub_status = nf_sub_status

    def __repr__(self):
        return f'NetworkFunctionSubscriptions: {self.subscription_name}, ' \
            f'{self.nf_name}, {self.nf_sub_status}'
