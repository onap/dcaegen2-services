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

from sqlalchemy import Column, Integer, String, ForeignKey, JSON
from sqlalchemy.orm import relationship

from mod import db


class SubscriptionModel(db.Model):
    __tablename__ = 'subscriptions'
    id = Column(Integer, primary_key=True, autoincrement=True)
    subscription_name = Column(String(100), unique=True)
    nfFilter = Column(JSON)
    status = Column(String(20))

    nfs = relationship(
        'NfSubRelationalModel',
        cascade='all, delete-orphan',
        backref='subscription')

    def __init__(self, subscription_name, nfFilter, status):
        self.subscription_name = subscription_name
        self.nfFilter = nfFilter
        self.status = status

    def __repr__(self):
        return f'subscription_name: {self.subscription_name}, ' \
               f'nfFilter: {self.nfFilter}, ' \
               f'status: {self.status}'

    def __eq__(self, other):
        if isinstance(self, other.__class__):
            return self.subscription_name == other.subscription_name
        return False

    def serialize(self):
        sub_nfs = NfSubRelationalModel.query.filter(
            NfSubRelationalModel.subscription_name == self.subscription_name).all()
        db.session.remove()
        return {'subscription_name': self.subscription_name,
                'nfFilter': self.nfFilter,
                'subscription_status': self.status,
                'network_functions': [sub_nf.serialize_nf() for sub_nf in sub_nfs]}


class NetworkFunctionModel(db.Model):
    __tablename__ = 'network_functions'
    id = Column(Integer, primary_key=True, autoincrement=True)
    nf_name = Column(String(100), unique=True)
    ip_address = Column(String(50))
    model_invariant_id = Column(String(100))
    model_version_id = Column(String(100))
    model_name = Column(String(100))
    sdnc_model_name = Column(String(100))
    sdnc_model_version = Column(String(100))
    retry_count = Column(Integer)

    subscriptions = relationship(
        'NfSubRelationalModel',
        cascade='all, delete-orphan',
        backref='nf')

    def __init__(self, nf_name, ip_address, model_invariant_id,
                 model_version_id, model_name, sdnc_model_name,
                 sdnc_model_version, retry_count=0):
        self.nf_name = nf_name
        self.ip_address = ip_address
        self.model_invariant_id = model_invariant_id
        self.model_version_id = model_version_id
        self.model_name = model_name
        self.sdnc_model_name = sdnc_model_name
        self.sdnc_model_version = sdnc_model_version
        self.retry_count = retry_count

    def __repr__(self):
        return str(self.to_nf())

    def to_nf(self):
        from mod.network_function import NetworkFunction
        return NetworkFunction(sdnc_model_name=self.sdnc_model_name,
                               sdnc_model_version=self.sdnc_model_version,
                               **{'nf_name': self.nf_name,
                                  'ip_address': self.ip_address,
                                  'model_invariant_id': self.model_invariant_id,
                                  'model_version_id': self.model_version_id})


class NfSubRelationalModel(db.Model):
    __tablename__ = 'nf_to_sub_rel'
    __mapper_args__ = {
        'confirm_deleted_rows': False
    }
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
        return f'subscription_name: {self.subscription_name}, ' \
            f'nf_name: {self.nf_name}, nf_sub_status: {self.nf_sub_status}'

    def serialize(self):
        return {'subscription_name': self.subscription_name, 'nf_name': self.nf_name,
                'nf_sub_status': self.nf_sub_status}

    def serialize_nf(self):
        nf = NetworkFunctionModel.query.filter(
            NetworkFunctionModel.nf_name == self.nf_name).one_or_none()
        db.session.remove()
        return {'nf_name': self.nf_name,
                'ip_address': nf.ip_address,
                'nf_sub_status': self.nf_sub_status,
                'model_invariant_id': nf.model_invariant_id,
                'model_version_id': nf.model_version_id,
                'model_name': nf.model_name,
                'sdnc_model_name': nf.sdnc_model_name,
                'sdnc_model_version': nf.sdnc_model_version}
