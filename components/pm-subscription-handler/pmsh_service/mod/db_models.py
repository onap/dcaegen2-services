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

from pmsh_service.mod.db_config import db


class Subscription(db.Model):
    __tablename__ = 'subscriptions'
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    subscription_name = db.Column(db.String(100), unique=True)
    status = db.Column(db.String(20))

    nfs = db.relationship(
        'NetworkFunctionSubscription',
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


class NetworkFunction(db.Model):
    __tablename__ = 'network_functions'
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    nf_name = db.Column(db.String(100), unique=True)

    subscriptions = db.relationship(
        'NetworkFunctionSubscription',
        cascade='all, delete-orphan',
        backref='nf')

    def __init__(self, nf_name):
        self.nf_name = nf_name

    def __repr__(self):
        return f'NetworkFunction: {self.nf_name}'


class NetworkFunctionSubscription(db.Model):
    __tablename__ = 'NetworkFunctionSubscription'
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    subscription_name = db.Column(
        db.String,
        db.ForeignKey(Subscription.subscription_name,
                      ondelete='cascade',
                      onupdate='cascade')
    )
    nf_name = db.Column(
        db.String,
        db.ForeignKey(NetworkFunction.nf_name,
                      ondelete='cascade',
                      onupdate='cascade')
    )
    status = db.Column(db.String(20))

    def __init__(self, subscription_name, nf_name, status):
        self.subscription_name = subscription_name
        self.nf_name = nf_name
        self.status = status

    def __repr__(self):
        return f'NetworkFunctionSubscriptions: {self.subscription_name},' \
            f'{self.nf_name}, {self.status}'
