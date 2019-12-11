# ============LICENSE_START===================================================
#  Copyright (C) 2019 Nordix Foundation.
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
import os
import uuid

import requests
from requests.auth import HTTPBasicAuth

import mod.pmsh_logging as logger
from subscription import Subscription


def get_pmsh_subscription_data(cbs_data):
    """
    Return the PMSH subscription data

    Args:
        cbs_data: json app config from the Config Binding Service.

    Returns:
        Subscription, set(Xnf): `Subscription` <Subscription> object, set of XNFs to be added.

    Raises:
        RuntimeError: if AAI data cannot be retrieved.
    """
    aai_xnf_data = _get_all_aai_xnf_data()
    if aai_xnf_data:
        sub = Subscription(**cbs_data['policy']['subscription'])
        xnfs = _filter_xnf_data(aai_xnf_data, sub)
    else:
        raise RuntimeError('Failed to get data from AAI')
    return sub, xnfs


def _get_all_aai_xnf_data():
    """
    Return queried xnf data from the AAI service.

    Returns:
        json: the json response from AAI query, else None.
    """
    xnf_data = None
    try:
        session = requests.Session()
        session.verify = False
        aai_endpoint = f'{_get_aai_service_url()}{"/aai/v16/query"}'
        headers = {'accept': 'application/json',
                   'content-type': 'application/json',
                   'x-fromappid': 'dcae-pmsh',
                   'x-transactionid': str(uuid.uuid1())}
        json_data = """
                    {'start':
                        ['network/pnfs',
                        'network/generic-vnfs']
                    }"""
        params = {'format': 'simple', 'nodesOnly': 'true'}
        response = session.put(aai_endpoint, headers=headers,
                               auth=HTTPBasicAuth('AAI', 'AAI'),
                               data=json_data, params=params)
        response.raise_for_status()
        if response.ok:
            xnf_data = json.loads(response.text)
    except Exception as e:
        logger.debug(e)
    return xnf_data


def _get_aai_service_url():
    """
    Returns the URL of the AAI kubernetes service.

    Returns:
        str: the AAI k8s service URL.

    Raises:
        KeyError: if AAI env vars not found.
    """
    try:
        aai_service = os.environ['AAI_SERVICE_HOST']
        aai_ssl_port = os.environ['AAI_SERVICE_PORT_AAI_SSL']
        return f'https://{aai_service}:{aai_ssl_port}'
    except KeyError as e:
        logger.debug(f'Failed to get AAI env vars: {e}')
        raise


def _filter_xnf_data(xnf_data, sub):
    """
    Returns a list of filtered xnfs using the subscription nfFilter.

    Args:
        xnf_data: the xnf json data from AAI.
        sub: the `Subscription <Subscription>` object defined.

    Returns:
        set: a set of filtered xnfs.

    Raises:
        KeyError: if AAI data cannot be parsed.
    """
    xnf_set = set()
    try:
        for xnf in xnf_data['results']:
            if xnf['node-type'] == 'pnf':
                if sub.is_xnf_in_filter(xnf['properties']['pnf-name']):
                    if 'orchestration-status' in xnf['properties']:
                        pnf_obj = Xnf(
                            xnf_name=xnf['properties']['pnf-name'],
                            orchestration_status=xnf['properties']['orchestration-status'])
                    else:
                        pnf_obj = Xnf(xnf_name=xnf['properties']['pnf-name'])
                    xnf_set.add(pnf_obj)
            elif xnf['node-type'] == 'generic-vnf':
                if sub.is_xnf_in_filter(xnf['properties']['vnf-name']):
                    vnf_obj = Xnf(xnf_name=xnf['properties']['vnf-name'],
                                  orchestration_status=xnf['properties']['orchestration-status'])
                    xnf_set.add(vnf_obj)
    except KeyError as e:
        logger.debug(f'Failed to parse AAI data: {e}')
        raise
    return xnf_set


class Xnf:
    def __init__(self, **kwargs):
        """
        Object representation of the XNF.
        """
        self.xnf_name = kwargs.get('xnf_name')
        self.orchestration_status = kwargs.get('orchestration_status')

    @classmethod
    def xnf_def(cls):
        return cls(xnf_name=None, orchestration_status=None)

    def __str__(self):
        return f'xnf-name: {self.xnf_name}, orchestration-status: {self.orchestration_status}'
