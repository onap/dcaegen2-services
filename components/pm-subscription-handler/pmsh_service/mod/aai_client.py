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
from os import environ

import requests
from requests.auth import HTTPBasicAuth

from mod import logger
from mod.network_function import NetworkFunction
from mod.pmsh_utils import mdc_handler


def get_pmsh_nfs_from_aai(app_conf):
    """
    Returns the Network Functions from AAI related to the Subscription.

    Args:
        app_conf (AppConfig): the AppConfig object.

    Returns:
        set(NetworkFunctions): set of NetworkFunctions.

    Raises:
        RuntimeError: if AAI Network Function data cannot be retrieved.
    """
    aai_nf_data = _get_all_aai_nf_data(app_conf)
    if aai_nf_data:
        nfs = _filter_nf_data(aai_nf_data, app_conf.nf_filter)
    else:
        raise RuntimeError('Failed to get data from AAI')
    return nfs


@mdc_handler
def _get_all_aai_nf_data(app_conf, **kwargs):
    """
    Return queried nf data from the AAI service.

    Args:
        app_conf (AppConfig): the AppConfig object.

    Returns:
        dict: the json response from AAI query, else None.
    """
    nf_data = None
    try:
        session = requests.Session()
        aai_endpoint = f'{_get_aai_service_url()}{"/aai/v19/query"}'
        logger.info('Fetching XNFs from AAI.')
        headers = {'accept': 'application/json',
                   'content-type': 'application/json',
                   'x-fromappid': 'dcae-pmsh',
                   'x-transactionid': kwargs['request_id'],
                   'InvocationID': kwargs['invocation_id'],
                   'RequestID': kwargs['request_id']}
        json_data = """
                    {'start':
                        ['network/pnfs',
                        'network/generic-vnfs']
                    }"""
        params = {'format': 'simple', 'nodesOnly': 'true'}
        response = session.put(aai_endpoint, headers=headers,
                               auth=HTTPBasicAuth(app_conf.aaf_creds.get('aaf_id'),
                                                  app_conf.aaf_creds.get('aaf_pass')),
                               data=json_data, params=params,
                               verify=(app_conf.ca_cert_path if app_conf.enable_tls else False),
                               cert=(app_conf.cert_params if app_conf.enable_tls else None))
        response.raise_for_status()
        if response.ok:
            nf_data = json.loads(response.text)
            logger.info('Successfully fetched XNFs from AAI')
            logger.debug(f'XNFs from AAI: {nf_data}')
    except Exception as e:
        logger.error(f'Failed to get XNFs from AAI: {e}', exc_info=True)
    return nf_data


def _get_aai_service_url():
    """
    Returns the URL of the AAI kubernetes service.

    Returns:
        str: the AAI k8s service URL.

    Raises:
        KeyError: if AAI env var not found.
    """
    try:
        aai_ssl_port = environ['AAI_SERVICE_PORT']
        return f'https://aai:{aai_ssl_port}'
    except KeyError as e:
        logger.error(f'Failed to get AAI env var: {e}', exc_info=True)
        raise


def _filter_nf_data(nf_data, nf_filter):
    """
    Returns a list of filtered NetworkFunctions using the nf_filter.

    Args:
        nf_data(dict): the nf json data from AAI.
        nf_filter(NetworkFunctionFilter): the NetworkFunctionFilter to be applied.

    Returns:
        set(NetworkFunctions): a set of filtered NetworkFunctions.

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
        logger.error(f'Failed to parse AAI data: {e}', exc_info=True)
        raise
    return nf_set
