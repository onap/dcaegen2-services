# ============LICENSE_START===================================================
#  Copyright (C) 2020 Nordix Foundation.
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

from mod import logger
from mod.network_function import NetworkFunction


class XNFType(Enum):
    PNF = 'pnf'
    VNF = 'vnf'


class AAIEvent(Enum):
    DELETE = 'DELETE'
    UPDATE = 'UPDATE'


def process_aai_events(mr_sub, mr_pub, app, app_conf):
    """
    Processes AAI UPDATE events for each filtered xNFs where orchestration status is set to Active.

    Args:
        mr_sub (_MrSub): MR subscriber
        mr_pub (_MrPub): MR publisher
        app (db): DB application
        app_conf (AppConfig): the application configuration.
    """
    app.app_context().push()
    logger.info('Polling MR for XNF AAI events.')
    try:
        aai_events = mr_sub.get_from_topic('dcae_pmsh_aai_event')
        if aai_events is not None and len(aai_events) != 0:
            aai_events = [json.loads(e) for e in aai_events]
            xnf_events = [e for e in aai_events if e['event-header']['entity-type'] == (
                XNFType.PNF.value or XNFType.VNF.value)]
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
                if not nf.set_nf_model_params(app_conf):
                    continue
                if app_conf.nf_filter.is_nf_in_filter(nf):
                    _process_event(action, nf, mr_pub, app_conf)
    except Exception as e:
        logger.error(f'Failed to process AAI event: {e}', exc_info=True)


def _process_event(action, nf, mr_pub, app_conf):
    if action == AAIEvent.UPDATE.value:
        logger.info(f'Update event found for network function {nf.nf_name}')
        app_conf.subscription.create_subscription_on_nfs([nf], mr_pub, app_conf)
    elif action == AAIEvent.DELETE.value:
        logger.info(f'Delete event found for network function {nf.nf_name}')
        NetworkFunction.delete(nf_name=nf.nf_name)
        logger.info(f'{nf.nf_name} successfully deleted.')
