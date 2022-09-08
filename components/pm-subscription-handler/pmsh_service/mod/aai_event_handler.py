# ============LICENSE_START===================================================
#  Copyright (C) 2020-2021 Nordix Foundation.
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
from mod import logger, db
from mod.network_function import NetworkFunction
from mod.pmsh_config import AppConfig, MRTopic
from mod.api.db_models import SubscriptionModel
from mod.network_function import NetworkFunctionFilter
from mod.api.services import subscription_service


class XNFType(Enum):
    PNF = 'pnf'
    VNF = 'vnf'


class AAIEvent(Enum):
    DELETE = 'DELETE'
    UPDATE = 'UPDATE'


def is_pnf_xnf(entity_type):
    """
    Applies measurement groups to network functions identified by AAI event
    Args:
        entity_type (string): The type of network function
    """
    return entity_type == (XNFType.PNF.value or XNFType.VNF.value)


class AAIEventHandler:
    """ Responsible for handling AAI update events in PMSH """

    def __init__(self, app):
        self.app = app

    def execute(self):
        """
        Processes AAI UPDATE events for each filtered xNFs where
        orchestration status is set to Active.
        """
        self.app.app_context().push()
        logger.info('Polling MR for XNF AAI events.')
        try:
            aai_events = AppConfig.get_instance().get_from_topic(MRTopic.AAI_SUBSCRIBER.value,
                                                                 'dcae_pmsh_aai_event')
            if aai_events is not None and len(aai_events) != 0:
                pmsh_nf_names = list(nf.nf_name for nf in NetworkFunction.get_all())
                aai_events = [json.loads(e) for e in aai_events]
                xnf_events = [e for e in aai_events if is_pnf_xnf(e['event-header']['entity-type'])]
                new_nfs = []
                for entry in xnf_events:
                    logger.debug(f'AAI-EVENT entry: {entry}')
                    aai_entity = entry['entity']
                    action = entry['event-header']['action']
                    entity_type = entry['event-header']['entity-type']
                    xnf_name = aai_entity['pnf-name'] if entity_type == XNFType.PNF.value \
                        else aai_entity['vnf-name']
                    if aai_entity['orchestration-status'] != 'Active':
                        logger.info(f'Skipping XNF {xnf_name} as its orchestration-status '
                                    f'is not "Active"')
                        continue
                    nf = NetworkFunction(nf_name=xnf_name,
                                         ipv4_address=aai_entity['ipaddress-v4-oam'],
                                         ipv6_address=aai_entity['ipaddress-v6-oam'],
                                         model_invariant_id=aai_entity['model-invariant-id'],
                                         model_version_id=aai_entity['model-version-id'])
                    if action == AAIEvent.DELETE.value and xnf_name in pmsh_nf_names:
                        logger.info(f'Delete event found for network function {nf.nf_name}')
                        NetworkFunction.delete(nf_name=nf.nf_name)
                        logger.info(f'{nf.nf_name} successfully deleted.')
                    elif action == AAIEvent.UPDATE.value and \
                            xnf_name not in pmsh_nf_names and \
                            nf.set_nf_model_params(AppConfig.get_instance()):
                        new_nfs.append(nf)
                if new_nfs:
                    self.apply_nfs_to_subscriptions(new_nfs)
        except Exception as e:
            logger.error(f'Failed to process AAI event due to: {e}')

    @staticmethod
    def apply_nfs_to_subscriptions(new_nfs):
        """
        Applies measurement groups to network functions identified by AAI event
        Args:
            new_nfs (list[NetworkFunction]): new network functions identified
        """
        subscriptions = db.session.query(SubscriptionModel).all()
        if subscriptions:
            for subscription in subscriptions:
                try:
                    nf_filter = NetworkFunctionFilter(**subscription.network_filter.serialize())
                    filtered_nfs = []
                    for nf in new_nfs:
                        if nf_filter.is_nf_in_filter(nf):
                            filtered_nfs.append(nf)
                    if filtered_nfs:
                        subscription_service.save_filtered_nfs(filtered_nfs)
                        subscription_service. \
                            apply_subscription_to_nfs(filtered_nfs, subscription.subscription_name)
                        unlocked_meas_grp = subscription_service. \
                            apply_measurement_grp_to_nfs(filtered_nfs,
                                                         subscription.measurement_groups)
                        if unlocked_meas_grp:
                            subscription_service. \
                                publish_measurement_grp_to_nfs(subscription, filtered_nfs,
                                                               unlocked_meas_grp)
                        else:
                            logger.error(f'All measurement groups are locked for subscription: '
                                         f'{subscription.subscription_name}, '
                                         f'please verify/check measurement groups.')
                        db.session.commit()
                except Exception as e:
                    logger.error(f'Failed to process AAI event for subscription: '
                                 f'{subscription.subscription_name} due to: {e}')
                    db.session.remove()
