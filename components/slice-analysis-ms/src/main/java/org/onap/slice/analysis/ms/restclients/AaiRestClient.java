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

package org.onap.slice.analysis.ms.restclients;

import java.util.Collections;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * This class represents rest client for AAI interfaces
 */
@Component
public class AaiRestClient extends RestClient {

	public AaiRestClient() {
		super();
	}

	/**
	 * Sends post request to Aai
	 */
	public <T> ResponseEntity<T> sendPostRequest(String requestUrl, String requestBody,
			ParameterizedTypeReference<T> responseType) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		return super.sendPostRequest(headers, requestUrl, requestBody, responseType);
	}

	/**
	 * Sends get request to Aai
	 */
	public <T> ResponseEntity<T> sendGetRequest(String requestUrl, ParameterizedTypeReference<T> responseType) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-FromAppId", "AAI");
		headers.set("X-TransactionId", "get_aai_subscr");
		return super.sendGetRequest(headers, requestUrl, responseType);
	}

}

