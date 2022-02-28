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

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.restclients.AaiRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * 
 * Service for AAI interfaces
 *
 */
@Service
public class AaiService implements AaiInterface {

	private static Logger log = LoggerFactory.getLogger(Configuration.class);

	@Autowired
	AaiRestClient restclient;
	private static String globalSubscriberId;
	private static String subscriptionServiceType;
	private static String aaiBaseUrl = Configuration.getInstance().getAaiUrl();

	/**
	 * Fetches the details of a subscriber
	 */
	public Map<String, String> fetchServiceDetails(String snssai) {
		log.info("AAI fetch service: ");
		Map<String, String> responseMap = new HashMap<String, String>();

		log.info("AAI getGlobalSubscriberId: ");
		String subscriberReqUrl = aaiBaseUrl + "/business/customers";
		try {
			String subscriberReq = restclient
					.sendGetRequest(subscriberReqUrl, new ParameterizedTypeReference<String>() {
					}).getBody();
			JSONObject subscriberReqJson = new JSONObject(subscriberReq);
			JSONArray subscriberReqJsonList = subscriberReqJson.getJSONArray("customer");
			for (int i = 0; i < subscriberReqJsonList.length(); i++) {
				JSONObject subscriberReqObj = subscriberReqJsonList.getJSONObject(i);
				globalSubscriberId = subscriberReqObj.getString("global-customer-id");
				responseMap.put("globalSubscriberId", globalSubscriberId);
				break;
			}
		} catch (Exception e) {
			log.info("Exception while fetching getGlobalSubscriberId: " + e);
		}

		String subscriptionServiceReqUrl = aaiBaseUrl + "/business/customers/customer/" + globalSubscriberId
				+ "/service-subscriptions";
		try {
			String subscriptionService = restclient
					.sendGetRequest(subscriptionServiceReqUrl, new ParameterizedTypeReference<String>() {
					}).getBody();
			JSONObject subscriptionServiceJson = new JSONObject(subscriptionService);
			JSONArray subscriptionServiceListJson = subscriptionServiceJson.getJSONArray("service-subscription");
			for (int i = 0; i < subscriptionServiceListJson.length(); i++) {
				JSONObject subscriptionServiceObj = subscriptionServiceListJson.getJSONObject(i);
				subscriptionServiceType = subscriptionServiceObj.getString("service-type");
				responseMap.put("subscriptionServiceType", subscriptionServiceType);
				break;
			}
		} catch (Exception e) {
			log.info("Exception while fetching subscriptionService: " + e);
		}

		String serviceReqUrl = aaiBaseUrl + "/business/customers/customer/" + globalSubscriberId
				+ "/service-subscriptions/service-subscription/" + subscriptionServiceType + "/service-instances";
		String serviceRole = "AN-NF";
		try {
			String serviceInstance = restclient.sendGetRequest(serviceReqUrl, new ParameterizedTypeReference<String>() {
			}).getBody();
			JSONObject serviceInstanceJson = new JSONObject(serviceInstance);
			JSONArray serviceInstanceList = serviceInstanceJson.getJSONArray("service-instance");
			for (int i = 0; i < serviceInstanceList.length(); i++) {
				JSONObject serviceObj = serviceInstanceList.getJSONObject(i);
				if (serviceObj.getString("environment-context").equalsIgnoreCase(snssai)) {
					responseMap.put("sliceProfileId", serviceObj.getString("service-instance-id"));
				}
			}

			String serviceRoleReqUrl = aaiBaseUrl + "/business/customers/customer/" + globalSubscriberId
					+ "/service-subscriptions/service-subscription/" + subscriptionServiceType
					+ "/service-instances/?service-role=nssi&depth=2";

			String serviceInstanceForServiceRole = restclient
					.sendGetRequest(serviceRoleReqUrl, new ParameterizedTypeReference<String>() {
					}).getBody();
			JSONObject serviceInstanceForServiceRoleJson = new JSONObject(serviceInstanceForServiceRole);
			JSONArray serviceInstanceListForServiceRole = serviceInstanceForServiceRoleJson
					.getJSONArray("service-instance");
			for (int i = 0; i < serviceInstanceListForServiceRole.length(); i++) {
				JSONObject serviceObj = serviceInstanceListForServiceRole.getJSONObject(i);
				if (serviceObj.getString("workload-context").trim().equalsIgnoreCase(serviceRole)) {
					responseMap.put("ranNFNSSIId", serviceObj.getString("service-instance-id"));
				}
			}
		} catch (Exception e) {
			log.info("Exception while fetching serviceDetails: " + e);
		}
		responseMap.put("sNSSAI", snssai);
		log.info("subscriber details: " + responseMap);
		return responseMap;
	}

	/**
	 * Fetches the current configuration of an Slice from AAI
	 */
	public Map<String, Integer> fetchCurrentConfigurationOfSlice(String snssai) {
		log.info("AAI fetch config Slice: " + aaiBaseUrl);
		String serviceInstaneId = null;
		String serviceReqUrl = aaiBaseUrl + "/business/customers/customer/" + globalSubscriberId
				+ "/service-subscriptions/service-subscription/" + subscriptionServiceType + "/service-instances";
		Map<String, Integer> responseMap = new HashMap<String, Integer>();
		try {
			String serviceInstance = restclient.sendGetRequest(serviceReqUrl, new ParameterizedTypeReference<String>() {
			}).getBody();
			JSONObject serviceInstanceJson = new JSONObject(serviceInstance);

			JSONArray serviceInstanceList = serviceInstanceJson.getJSONArray("service-instance");
			for (int i = 0; i < serviceInstanceList.length(); i++) {
				JSONObject serviceObj = serviceInstanceList.getJSONObject(i);
				if (serviceObj.getString("environment-context").equalsIgnoreCase(snssai)) {
					serviceInstaneId = serviceObj.getString("service-instance-id");
				}
			}

			String sliceProfileReqUrl = aaiBaseUrl + "/business/customers/customer/" + globalSubscriberId
					+ "/service-subscriptions/service-subscription/" + subscriptionServiceType
					+ "/service-instances/service-instance/" + serviceInstaneId + "/slice-profiles";

			String sliceProfile = restclient
					.sendGetRequest(sliceProfileReqUrl, new ParameterizedTypeReference<String>() {
					}).getBody();
			JSONObject sliceProfileJson = new JSONObject(sliceProfile);
			JSONArray sliceProfileList = sliceProfileJson.getJSONArray("slice-profile");
			for (int i = 0; i < sliceProfileList.length(); i++) {
				JSONObject sliceProfileObj = sliceProfileList.getJSONObject(i);
				responseMap.put("dLThptPerSlice", sliceProfileObj.getInt("exp-data-rate-UL"));
				responseMap.put("uLThptPerSlice", sliceProfileObj.getInt("exp-data-rate-DL"));
				break;
			}
			log.info("Slice configuration: " + responseMap);
		} catch (Exception e) {
			log.info("AAI Slice: " + e);
		}
		return responseMap;
	}

	/**
	 * Get network policy of an ethernet service, the network policy have same network-policy-fqdn as service-instance-id
	 */
	public Map<String, Integer> fetchMaxBandwidthofService(String serviceId){
		log.info("Fetching max-bandwidth from AAI network-policy");
		String networkPolicyUrl = aaiBaseUrl + "/network/network-policies" + "?network-policy-fqdn="
				+ serviceId;
		Map<String, Integer> result = new HashMap<>();
		try {
			ResponseEntity<String> resp = restclient.sendGetRequest(networkPolicyUrl, new ParameterizedTypeReference<String>() {
			});
			if (resp.getStatusCodeValue() == 200){
				String networkPolicy = resp.getBody();
				JSONObject networkPolicyJson = new JSONObject(networkPolicy);
				JSONArray networkPolicyList	= networkPolicyJson.optJSONArray("network-policy");
				if (networkPolicyList != null){
					JSONObject networkPolicyOjb = networkPolicyList.getJSONObject(0);
					result.put("maxBandwidth", networkPolicyOjb.getInt("max-bandwidth"));
					return result;
				}
				log.info("Successfully fetched max bandwidth {}: {}", serviceId, result);
			}
		} catch (Exception e){
			log.warn("Error encountered when fetching maxbandwidth: " + e);

		}
		return null;
	}
}

