/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2021 Wipro Limited.
 *   ==============================================================================
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     ============LICENSE_END=========================================================
 *
 *******************************************************************************/
package org.onap.slice.analysis.ms.configdb;

import java.util.List;
import java.util.Map;

import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.restclients.CpsRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Service for CPS interfaces
 *
 */
@Service
public class CpsService implements CpsInterface {

	private static Logger log = LoggerFactory.getLogger(CpsService.class);

	@Autowired
	CpsRestClient restclient;
	private static String cpsBaseUrl = Configuration.getInstance().getCpsUrl();

	/**
	 * Fetches the current configuration of RIC from CPS
	 */
	public Map<String, Map<String, Object>> fetchCurrentConfigurationOfRIC(String snssai) {
		return null;
	}

	/**
	 * Fetches all the network functions of an S-NSSAI from CPS
	 */
	public List<String> fetchNetworkFunctionsOfSnssai(String snssai) {
		return null;
	}

	/**
	 * Fetches the RICS of an S-NSSAI from CPS
	 */
	public Map<String, List<String>> fetchRICsOfSnssai(String snssai) {
		return null;
	}

}

