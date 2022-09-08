# ============LICENSE_START===================================================
#  Copyright (C) 2019-2022 Nordix Foundation.
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
import mod.network_function
from mod import logger, mdc_handler


def get_pmsh_nfs_from_aai(app_conf, nf_filter):
    """
    Returns the Network Functions from AAI related to the Subscription.

    Args:
        app_conf (AppConfig): the AppConfig object.
        nf_filter (NetworkFunctionFilter): the filter to apply on nf from aai
    Returns:
        NetworkFunctions (list): list of NetworkFunctions.

    Raises:
        RuntimeError: if AAI Network Function data cannot be retrieved.
    """
    aai_nf_data = _get_all_aai_nf_data(app_conf)
    if aai_nf_data:
        nfs = _filter_nf_data(aai_nf_data, app_conf, nf_filter)
    else:
        raise RuntimeError('Failed to get data from AAI')
    return nfs


def _get_all_aai_nf_data(app_conf):
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
        aai_named_query_endpoint = f'{_get_aai_service_url()}{"/query"}'
        logger.info('Fetching XNFs from AAI.')
        headers = _get_aai_request_headers()
        data = """
                {'start':
                    ['network/pnfs',
                    'network/generic-vnfs']
                }"""
        params = {'format': 'simple', 'nodesOnly': 'true'}
        response = session.put(aai_named_query_endpoint, headers=headers,
                               auth=HTTPBasicAuth(app_conf.aaf_id,
                                                  app_conf.aaf_pass),
                               data=data, params=params,
                               verify=(app_conf.ca_cert_path if app_conf.enable_tls else False),
                               cert=((app_conf.cert_path,
                                     app_conf.key_path) if app_conf.enable_tls else None))
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
        return f'https://aai:{aai_ssl_port}/aai/v21'
    except KeyError as e:
        logger.error(f'Failed to get AAI env var: {e}', exc_info=True)
        raise


@mdc_handler
def _get_aai_request_headers(**kwargs):
    return {'accept': 'application/json',
            'content-type': 'application/json',
            'x-fromappid': 'dcae-pmsh',
            'x-transactionid': kwargs['request_id'],
            'InvocationID': kwargs['invocation_id'],
            'RequestID': kwargs['request_id']}


def _filter_nf_data(nf_data, app_conf, nf_filter):
    """
    Returns a list of filtered NetworkFunctions using the nf_filter.

    Args:
        nf_data (dict): the nf json data from AAI.
        app_conf (AppConfig): the AppConfig object.
        nf_filter (NetworkFunctionFilter): filter data to apply on network functions
    Returns:
        NetworkFunction (list): a list of filtered NetworkFunction Objects.

    Raises:
        KeyError: if AAI data cannot be parsed.
    """
    nf_list = []
    try:
        for nf in nf_data['results']:
            if nf['properties'].get('orchestration-status') != 'Active':
                continue
            name_identifier = 'pnf-name' if nf['node-type'] == 'pnf' else 'vnf-name'
            new_nf = mod.network_function.NetworkFunction(
                nf_name=nf['properties'].get(name_identifier),
                ipv4_address=nf['properties'].get('ipaddress-v4-oam'),
                ipv6_address=nf['properties'].get('ipaddress-v6-oam'),
                model_invariant_id=nf['properties'].get('model-invariant-id'),
                model_version_id=nf['properties'].get('model-version-id'))
            if not new_nf.set_nf_model_params(app_conf):
                continue
            if nf_filter.is_nf_in_filter(new_nf):
                nf_list.append(new_nf)
    except KeyError as e:
        logger.error(f'Failed to parse AAI data: {e}', exc_info=True)
        raise
    return nf_list


def get_aai_model_data(app_conf, model_invariant_id, model_version_id, nf_name):
    """
    Get the sdnc_model info for the xNF from AAI.

    Args:
        model_invariant_id (str): the AAI model-invariant-id
        model_version_id (str): the AAI model-version-id
        app_conf (AppConfig): the AppConfig object.
        nf_name (str): The xNF name.

    Returns:
        json (dict): the sdnc_model json object.
    """
    session = requests.Session()
    aai_model_ver_endpoint = \
        f'{_get_aai_service_url()}/service-design-and-creation/models/model/' \
        f'{model_invariant_id}/model-vers/model-ver/{model_version_id}'

    logger.info(f'Fetching sdnc-model info for xNF: {nf_name} from AAI.')
    headers = _get_aai_request_headers()
    response = session.get(aai_model_ver_endpoint, headers=headers,
                           auth=HTTPBasicAuth(app_conf.aaf_id,
                                              app_conf.aaf_pass),
                           verify=(app_conf.ca_cert_path if app_conf.enable_tls else False),
                           cert=((app_conf.cert_path,
                                  app_conf.key_path) if app_conf.enable_tls else None))
    response.raise_for_status()
    if response.ok:
        data = json.loads(response.text)
        logger.debug(f'Successfully fetched sdnc-model info from AAI: {data}')
        return data
