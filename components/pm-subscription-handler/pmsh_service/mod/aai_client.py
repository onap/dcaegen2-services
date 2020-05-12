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
import json
import uuid
from os import environ, getenv

import requests
from onaplogging.mdcContext import MDC
from requests.auth import HTTPBasicAuth

from mod import logger
from mod.network_function import NetworkFunction, NetworkFunctionFilter
from mod.pmsh_utils import MDCKeys
from mod.subscription import Subscription


def get_pmsh_subscription_data(cbs_data):
    """
    Returns the PMSH subscription data

    Args:
        cbs_data: json app config from the Config Binding Service.

    Returns:
        Subscription, set(NetworkFunctions): `Subscription` <Subscription> object,
        set of NetworkFunctions to be added.

    Raises:
        RuntimeError: if AAI data cannot be retrieved.
    """
    aai_nf_data = _get_all_aai_nf_data()
    if aai_nf_data:
        sub = Subscription(**cbs_data['policy']['subscription'])
        nfs = _filter_nf_data(aai_nf_data, NetworkFunctionFilter(**sub.nfFilter))
    else:
        raise RuntimeError('Failed to get data from AAI')
    return sub, nfs


def _get_all_aai_nf_data():
    """
    Return queried nf data from the AAI service.

    Returns:
        dict: the json response from AAI query, else None.
    """
    request_id = str(uuid.uuid1())
    invocation_id = str(uuid.uuid1())
    MDC.put(MDCKeys.SERVICE_NAME.value, getenv('HOSTNAME'))
    MDC.put(MDCKeys.INVOCATION_ID.value, invocation_id)
    MDC.put(MDCKeys.REQUEST_ID.value, request_id)
    nf_data = None
    try:
        session = requests.Session()
        aai_endpoint = f'{_get_aai_service_url()}{"/aai/v16/query"}'
        logger.info('Fetching XNFs from AAI.')
        headers = {'accept': 'application/json',
                   'content-type': 'application/json',
                   'x-fromappid': 'dcae-pmsh',
                   'x-transactionid': request_id,
                   MDCKeys.INVOCATION_ID.value: invocation_id,
                   MDCKeys.REQUEST_ID.value: request_id
                   }
        json_data = """
                    {'start':
                        ['network/pnfs',
                        'network/generic-vnfs']
                    }"""
        params = {'format': 'simple', 'nodesOnly': 'true'}
        response = session.put(aai_endpoint, headers=headers,
                               auth=HTTPBasicAuth('AAI', 'AAI'),
                               data=json_data, params=params, verify=False)
        response.raise_for_status()
        if response.ok:
            nf_data = json.loads(response.text)
            logger.info('Successfully fetched XNFs from AAI')
            logger.debug(f'XNFs from AAI: {nf_data}')
    except Exception as e:
        logger.error(f'Failed to get XNFs from AAI: {e}')
    return nf_data


def _get_aai_service_url():
    """
    Returns the URL of the AAI kubernetes service.

    Returns:
        str: the AAI k8s service URL.

    Raises:
        KeyError: if AAI env vars not found.
    """
    try:
        aai_service = environ['AAI_SERVICE_HOST']
        aai_ssl_port = environ['AAI_SERVICE_PORT']
        return f'https://{aai_service}:{aai_ssl_port}'
    except KeyError as e:
        logger.error(f'Failed to get AAI env vars: {e}')
        raise


def _filter_nf_data(nf_data, nf_filter):
    """
    Returns a list of filtered NetworkFunctions using the nf_filter.

    Args:
        nf_data : the nf json data from AAI.
        nf_filter: the `NetworkFunctionFilter <NetworkFunctionFilter>` to be applied.

    Returns:
        set: a set of filtered NetworkFunctions.

    Raises:
        KeyError: if AAI data cannot be parsed.
    """
    nf_set = set()
    try:
        for nf in nf_data['results']:
            name_identifier = 'pnf-name' if nf['node-type'] == 'pnf' else 'vnf-name'
            orchestration_status = nf['properties'].get('orchestration-status')
            if nf_filter.is_nf_in_filter(nf['properties'].get(name_identifier),
                                         orchestration_status):
                nf_set.add(NetworkFunction(
                    nf_name=nf['properties'].get(name_identifier),
                    orchestration_status=orchestration_status))
    except KeyError as e:
        logger.error(f'Failed to parse AAI data: {e}')
        raise
    return nf_set
