# ============LICENSE_START===================================================
#  Copyright (C) 2020-2022 Nordix Foundation.
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
    subscription_name = Column(String(100), unique=True, nullable=False)
    operational_policy_name = Column(String(80), nullable=False)
    control_loop_name = Column(String(80))
    status = Column(String(20))

    nfs = relationship(
        'NfSubRelationalModel',
        cascade='all, delete-orphan',
        backref='subscription')

    network_filter = relationship(
        'NetworkFunctionFilterModel',
        cascade='all, delete-orphan',
        backref='subscription', uselist=False)

    measurement_groups = relationship(
        'MeasurementGroupModel',
        cascade='all, delete-orphan',
        backref='subscription')

    def __init__(self, subscription_name, operational_policy_name, control_loop_name, status):
        self.subscription_name = subscription_name
        self.operational_policy_name = operational_policy_name
        self.control_loop_name = control_loop_name
        self.status = status

    def __repr__(self):
        return (f'subscription_name: {self.subscription_name}, '
                f'operational_policy_name: {self.operational_policy_name}, '
                f'control_loop_name: {self.control_loop_name}, '
                f'status: {self.status}')

    def __eq__(self, other):
        if isinstance(self, other.__class__):
            return self.subscription_name == other.subscription_name
        return False

    def serialize(self):
        return {'subscription': {'subscriptionName': self.subscription_name,
                                 'operationalPolicyName': self.operational_policy_name,
                                 'controlLoopName': self.control_loop_name,
                                 'nfFilter': self.network_filter.serialize(),
                                 'nfs': [nf.nf_name for nf in self.nfs],
                                 'measurementGroups':
                                     [mg.serialize() for mg in self.measurement_groups]}}


class NetworkFunctionModel(db.Model):
    __tablename__ = 'network_functions'
    id = Column(Integer, primary_key=True, autoincrement=True)
    nf_name = Column(String(100), unique=True)
    ipv4_address = Column(String(50))
    ipv6_address = Column(String(50))
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

    def __init__(self, nf_name, ipv4_address, ipv6_address, model_invariant_id,
                 model_version_id, model_name, sdnc_model_name,
                 sdnc_model_version, retry_count=0):
        self.nf_name = nf_name
        self.ipv4_address = ipv4_address
        self.ipv6_address = ipv6_address
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
                                  'ipv4_address': self.ipv4_address,
                                  'ipv6_address': self.ipv6_address,
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
        return (f'subscription_name: {self.subscription_name}, '
                f'nf_name: {self.nf_name}, nf_sub_status: {self.nf_sub_status}')

    def serialize(self):
        return {'subscription_name': self.subscription_name, 'nf_name': self.nf_name,
                'nf_sub_status': self.nf_sub_status}

    def serialize_nf(self):
        nf = NetworkFunctionModel.query.filter(
            NetworkFunctionModel.nf_name == self.nf_name).one_or_none()
        return {'nf_name': self.nf_name,
                'ipv4_address': nf.ipv4_address,
                'ipv6_address': nf.ipv6_address,
                'nf_sub_status': self.nf_sub_status,
                'model_invariant_id': nf.model_invariant_id,
                'model_version_id': nf.model_version_id,
                'model_name': nf.model_name,
                'sdnc_model_name': nf.sdnc_model_name,
                'sdnc_model_version': nf.sdnc_model_version}


class NetworkFunctionFilterModel(db.Model):
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
        return (f'subscription_name: {self.subscription_name}, '
                f'nf_names: {self.nf_names}, model_invariant_ids: {self.model_invariant_ids}, '
                f'model_version_ids: {self.model_version_ids}, model_names: {self.model_names}')

    def serialize(self):
        return {'nfNames': convert_db_string_to_list(self.nf_names),
                'modelInvariantIDs': convert_db_string_to_list(self.model_invariant_ids),
                'modelVersionIDs': convert_db_string_to_list(self.model_version_ids),
                'modelNames': convert_db_string_to_list(self.model_names)}


class MeasurementGroupModel(db.Model):
    __tablename__ = 'measurement_group'
    id = Column(Integer, primary_key=True, autoincrement=True)
    subscription_name = Column(
        String,
        ForeignKey(SubscriptionModel.subscription_name, ondelete='cascade', onupdate='cascade')
    )
    measurement_group_name = Column(String(100), unique=True)
    administrative_state = Column(String(20))
    file_based_gp = Column(Integer)
    file_location = Column(String(100))
    measurement_type = Column(JSON)
    managed_object_dns_basic = Column(JSON)

    def __init__(self, subscription_name, measurement_group_name,
                 administrative_state, file_based_gp, file_location,
                 measurement_type, managed_object_dns_basic):
        self.subscription_name = subscription_name
        self.measurement_group_name = measurement_group_name
        self.administrative_state = administrative_state
        self.file_based_gp = file_based_gp
        self.file_location = file_location
        self.measurement_type = measurement_type
        self.managed_object_dns_basic = managed_object_dns_basic

    def __repr__(self):
        return (f'subscription_name: {self.subscription_name}, '
                f'measurement_group_name: {self.measurement_group_name}, '
                f'administrative_state: {self.administrative_state}, '
                f'file_based_gp: {self.file_based_gp}, '
                f'file_location: {self.file_location}, '
                f'measurement_type: {self.measurement_type}, '
                f'managed_object_dns_basic: {self.managed_object_dns_basic}')

    def serialize(self):
        return {'measurementGroup': {'measurementGroupName': self.measurement_group_name,
                                     'administrativeState': self.administrative_state,
                                     'fileBasedGP': self.file_based_gp,
                                     'fileLocation': self.file_location,
                                     'measurementTypes': self.measurement_type,
                                     'managedObjectDNsBasic': self.managed_object_dns_basic}}

    def meas_group_with_nfs(self):
        """
        Generates the dictionary of subscription name, measurement group name, administrative state
        and network functions

        Returns:
           dict: of subscription name, measurement group name, administrative state
                 and network functions
        """
        meas_group_nfs = db.session.query(NfMeasureGroupRelationalModel).filter(
            NfMeasureGroupRelationalModel.measurement_grp_name == self.measurement_group_name).all()
        db.session.remove()
        return {'subscriptionName': self.subscription_name,
                'measurementGroupName': self.measurement_group_name,
                'administrativeState': self.administrative_state,
                'fileBasedGP': self.file_based_gp,
                'fileLocation': self.file_location,
                'measurementTypes': self.measurement_type,
                'managedObjectDNsBasic': self.managed_object_dns_basic,
                'networkFunctions':
                    [meas_group_nf.serialize_meas_group_nfs() for meas_group_nf in meas_group_nfs]}


class NfMeasureGroupRelationalModel(db.Model):
    __tablename__ = 'nf_to_measure_grp_rel'
    __mapper_args__ = {
        'confirm_deleted_rows': False
    }
    id = Column(Integer, primary_key=True, autoincrement=True)
    measurement_grp_name = Column(
        String,
        ForeignKey(MeasurementGroupModel.measurement_group_name, ondelete='cascade',
                   onupdate='cascade')
    )
    nf_name = Column(
        String,
        ForeignKey(NetworkFunctionModel.nf_name, ondelete='cascade', onupdate='cascade')
    )
    nf_measure_grp_status = Column(String(20))
    retry_count = Column(Integer)

    def __init__(self, measurement_grp_name, nf_name, nf_measure_grp_status=None,
                 retry_count=0):
        self.measurement_grp_name = measurement_grp_name
        self.nf_name = nf_name
        self.nf_measure_grp_status = nf_measure_grp_status
        self.retry_count = retry_count

    def __repr__(self):
        return (f'measurement_grp_name: {self.measurement_grp_name}, '
                f'nf_name: {self.nf_name}, nf_measure_grp_status: {self.nf_measure_grp_status}')

    def serialize_meas_group_nfs(self):
        """
        Generates the dictionary of all the network function properties

        Returns:
           dict: of network function properties
        """
        nf = db.session.query(NetworkFunctionModel).filter(
            NetworkFunctionModel.nf_name == self.nf_name).one_or_none()
        return {'nfName': self.nf_name,
                'ipv4Address': nf.ipv4_address,
                'ipv6Address': nf.ipv6_address,
                'nfMgStatus': self.nf_measure_grp_status,
                'modelInvariantId': nf.model_invariant_id,
                'modelVersionId': nf.model_version_id,
                'modelName': nf.model_name,
                'sdncModelName': nf.sdnc_model_name,
                'sdncModelVersion': nf.sdnc_model_version}


def convert_db_string_to_list(db_string):
    """
    Converts a db string to array and
    removes empty strings
    Args:
        db_string (string): The db string to convert into an array
    Returns:
        list[string]: converted list of strings else empty
    """
    array_format = db_string.strip('{}').split(',')
    return [x for x in array_format if x.strip() != ""]
