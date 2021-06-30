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
    status = Column(String(20))
    administrative_state = Column(String(20))
    file_based_gp = Column(Integer)
    file_location = Column(String(100))

    nfs = relationship(
        'NfSubRelationalModel',
        cascade='all, delete-orphan',
        backref='subscription')

    network_filter = relationship(
        'Network_Function_Filter',
        cascade='all, delete-orphan',
        backref='subscription')

    def __init__(self, subscription_name, status, **kwargs):
        self.subscription_name = subscription_name
        self.status = status
        self.administrative_state = kwargs.get('administrative_state', None)
        self.file_based_gp = kwargs.get('file_based_gp', None)
        self.file_location = kwargs.get('file_location', None)

    def __repr__(self):
        return f'subscription_name: {self.subscription_name}, status: {self.status},' \
               f'administrative_state: {self.administrative_state},' \
               f'file_based_gp: {self.file_based_gp}, file_location: {self.file_location},'

    def __eq__(self, other):
        if isinstance(self, other.__class__):
            return self.subscription_name == other.subscription_name
        return False

    def serialize(self):
        sub_nfs = NfSubRelationalModel.query.filter(
            NfSubRelationalModel.subscription_name == self.subscription_name).all()
        db.session.remove()
        return {'subscription_name': self.subscription_name, 'subscription_status': self.status,
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


class Network_Function_Filter(db.Model):
    __tablename__ = 'nf_filter'
    id = Column(Integer, primary_key=True, autoincrement=True)
    subscription_name = Column(
        String,
        ForeignKey(SubscriptionModel.subscription_name, ondelete='cascade', onupdate='cascade'),
        unique=True
    )
    nf_names = Column(String(100))
    model_invariant_ids = Column(String(100))
    model_version_ids = Column(String(100))
    model_names = Column(String(100))

    def __init__(self, subscription_name, nf_names, model_invariant_ids, model_version_ids,
                 model_names):
        self.subscription_name = subscription_name
        self.nf_names = nf_names
        self.model_invariant_ids = model_invariant_ids
        self.model_version_ids = model_version_ids
        self.model_names = model_names

    def __repr__(self):
        return f'subscription_name: {self.subscription_name}, ' \
            f'nf_names: {self.nf_names}, model_invariant_ids: {self.model_invariant_ids}' \
               f'model_version_ids: {self.model_version_ids}, model_names: {self.model_names}'

    def serialize(self):
        return {'subscription_name': self.subscription_name, 'nf_names': self.nf_names,
                'model_invariant_ids': self.model_invariant_ids,
                'model_version_ids': self.model_version_ids, 'model_names': self.model_names}


class Measurement_Group(db.Model):
    __tablename__ = 'measurement_group'
    id = Column(Integer, primary_key=True, autoincrement=True)
    subscription_name = Column(
        String,
        ForeignKey(SubscriptionModel.subscription_name, ondelete='cascade', onupdate='cascade'),
        unique=True
    )
    measurement_group_name = Column(String(100))
    measurement_type = Column(JSON)
    managed_object_dns_basic = Column(JSON)

    def __init__(self, subscription_name, measurement_group_name,
                 measurement_type, managed_object_dns_basic):
        self.subscription_name = subscription_name
        self.measurement_group_name = measurement_group_name
        self.measurement_type = measurement_type
        self.managed_object_dns_basic = managed_object_dns_basic

    def __repr__(self):
        return f'subscription_name: {self.subscription_name}, ' \
               f'measurement_group_name: {self.measurement_group_name},' \
               f'measurement_type: {self.measurement_type}' \
               f'managed_object_dns_basic: {self.managed_object_dns_basic}'

    def serialize(self):
        return {'subscription_name': self.subscription_name,
                'measurement_group_name': self.measurement_group_name,
                'measurement_type': self.measurement_type,
                'managed_object_dns_basic': self.managed_object_dns_basic}
