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
                new_status = aai_entity['orchestration-status']

                if app_conf.nf_filter.is_nf_in_filter(xnf_name, new_status):
                    _process_event(action, new_status, xnf_name, mr_pub, app_conf)
    except Exception as e:
        logger.error(f'Failed to process AAI event: {e}', exc_info=True)


def _process_event(action, new_status, xnf_name, mr_pub, app_conf):
    if action == AAIEvent.UPDATE.value:
        logger.info(f'Update event found for network function {xnf_name}')
        local_xnf = NetworkFunction.get(xnf_name)

        if local_xnf is None:
            app_conf.subscription.activate_subscription([NetworkFunction(
                nf_name=xnf_name, orchestration_status=new_status)], mr_pub, app_conf)
        else:
            logger.debug(f"Update Event for network function {xnf_name} will not be processed "
                         f" as it's state is set to {local_xnf.orchestration_status}.")
    elif action == AAIEvent.DELETE.value:
        logger.info(f'Delete event found for network function {xnf_name}')
        NetworkFunction.delete(nf_name=xnf_name)
        logger.info(f'{xnf_name} successfully deleted.')
