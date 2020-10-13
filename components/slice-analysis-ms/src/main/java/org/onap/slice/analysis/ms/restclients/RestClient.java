/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
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

package org.onap.slice.analysis.ms.restclients;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/** 
 * This class is for base rest client 
 */
@Component
public class RestClient {

	private static final String EXCEPTION_MSG = "Exception caught during request {}";
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(RestClient.class);
	
	@Autowired
	private RestTemplate restTemplate;
	protected RestClient() {

	}

	/**
	 * Post Request Template.
	 */

	public <T> ResponseEntity<T> sendPostRequest(HttpHeaders headers, String requestUrl, String requestBody,
			ParameterizedTypeReference<T> responseType) {
		HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
		try {
			return restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, responseType);
		} catch (Exception e) {
			log.debug(EXCEPTION_MSG, e.getMessage());
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Get Request Template.
	 */

	public <T> ResponseEntity<T> sendGetRequest(HttpHeaders headers, String requestUrl, ParameterizedTypeReference<T> responseType) {
		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
		try {
			return restTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, responseType);
		} catch (Exception e) {
			log.debug(EXCEPTION_MSG, e.getMessage());
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}


}
