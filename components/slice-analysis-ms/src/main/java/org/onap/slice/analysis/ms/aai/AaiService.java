/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2021-2022 Wipro Limited.
 *   Copyright (C) 2022 Huawei Canada Limited.
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

package org.onap.slice.analysis.ms.aai;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.aai.ServiceInstance;
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

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static String globalSubscriberId;
    private static String subscriptionServiceType;
    private static String aaiBaseUrl = Configuration.getInstance().getAaiUrl();

    /**
     * Fetches the details of a service
     *
     * @param snssai SNSSAI ID
     * @return responseMap contains service details
     */
    public Map<String, String> fetchServiceDetails(String snssai) {
        Map<String, String> responseMap = fetchSubscriberAndSubscriptionServiceType();
        String serviceReqUrl = aaiBaseUrl + "/business/customers/customer/" + globalSubscriberId
                + "/service-subscriptions/service-subscription/" + subscriptionServiceType + "/service-instances";
        String serviceRole = "AN-NF";
        try {
            String serviceInstance =
                    restclient.sendGetRequest(serviceReqUrl, new ParameterizedTypeReference<String>() {}).getBody();
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

            String serviceInstanceForServiceRole =
                    restclient.sendGetRequest(serviceRoleReqUrl, new ParameterizedTypeReference<String>() {}).getBody();
            JSONObject serviceInstanceForServiceRoleJson = new JSONObject(serviceInstanceForServiceRole);
            JSONArray serviceInstanceListForServiceRole =
                    serviceInstanceForServiceRoleJson.getJSONArray("service-instance");
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
     * Fetches the current configuration of a Slice from AAI
     *
     * @param snssai SNSSAI ID
     * @return responseMap contains slice configuration
     */
    public Map<String, Integer> fetchCurrentConfigurationOfSlice(String snssai) {
        log.info("AAI fetch config Slice: " + aaiBaseUrl);
        String serviceInstaneId = null;
        String serviceReqUrl = aaiBaseUrl + "/business/customers/customer/" + globalSubscriberId
                + "/service-subscriptions/service-subscription/" + subscriptionServiceType + "/service-instances";
        Map<String, Integer> responseMap = new HashMap<String, Integer>();
        try {
            String serviceInstance =
                    restclient.sendGetRequest(serviceReqUrl, new ParameterizedTypeReference<String>() {}).getBody();
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
                    .sendGetRequest(sliceProfileReqUrl, new ParameterizedTypeReference<String>() {}).getBody();
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
     * Fetches the details of a subscriber and subscription Service
     *
     * @return responseMap contains details of subscriber and subscription service
     */
    public Map<String, String> fetchSubscriberAndSubscriptionServiceType() {

        Map<String, String> responseMap = new HashMap<String, String>();

        log.info("Get GlobalSubscriberId");
        String subscriberReqUrl = aaiBaseUrl + "/business/customers";
        try {
            String subscriberReq =
                    restclient.sendGetRequest(subscriberReqUrl, new ParameterizedTypeReference<String>() {}).getBody();
            JSONObject subscriberReqJson = new JSONObject(subscriberReq);
            JSONArray subscriberReqJsonList = subscriberReqJson.getJSONArray("customer");
            for (int i = 0; i < subscriberReqJsonList.length(); i++) {
                JSONObject subscriberReqObj = subscriberReqJsonList.getJSONObject(i);
                globalSubscriberId = subscriberReqObj.getString("global-customer-id");
                responseMap.put("globalSubscriberId", globalSubscriberId);
                break;
            }

            log.info("Get subscriptionServiceType");
            String subscriptionServiceReqUrl =
                    aaiBaseUrl + "/business/customers/customer/" + globalSubscriberId + "/service-subscriptions";

            String subscriptionService = restclient
                    .sendGetRequest(subscriptionServiceReqUrl, new ParameterizedTypeReference<String>() {}).getBody();
            JSONObject subscriptionServiceJson = new JSONObject(subscriptionService);
            JSONArray subscriptionServiceListJson = subscriptionServiceJson.getJSONArray("service-subscription");
            for (int i = 0; i < subscriptionServiceListJson.length(); i++) {
                JSONObject subscriptionServiceObj = subscriptionServiceListJson.getJSONObject(i);
                subscriptionServiceType = subscriptionServiceObj.getString("service-type");
                responseMap.put("subscriptionServiceType", subscriptionServiceType);
                break;
            }
        } catch (Exception e) {
            log.info("Exception while fetching subscriber and subscription: " + e);
        }

        log.info("responseMap: " + responseMap);
        return responseMap;

    }

    /**
     * Fetches the SNSSIs of a serviceInstanceId
     *
     * @param serviceInstanceId service instance ID
     * @return snssaiList contains list of SNSSAIs
     */
    public List<String> getSnssaiList(String sliceInstanceId) {
        fetchSubscriberAndSubscriptionServiceType();
        List<String> allotedResource = new ArrayList<>();
        List<String> sliceProfileList = new ArrayList<>();
        List<String> snssaiList = new ArrayList<>();

        log.info("fetch slice instance details");
        String serviceReqUrl = aaiBaseUrl + "/business/customers/customer/" + globalSubscriberId
                + "/service-subscriptions/service-subscription/" + subscriptionServiceType
                + "/service-instances/service-instance/" + sliceInstanceId;

        try {
            String serviceInstanceString =
                    restclient.sendGetRequest(serviceReqUrl, new ParameterizedTypeReference<String>() {}).getBody();
            ServiceInstance serviceInstance = objectMapper.readValue(serviceInstanceString, ServiceInstance.class);
            if (serviceInstance.getServiceRole().equalsIgnoreCase("nsi")) {
                serviceInstance.getRelationshipList().getRelationship().forEach(relationship -> {
                    if (relationship.getRelatedTo().equalsIgnoreCase("allotted-resource")) {
                        relationship.getRelationshipData().forEach(data -> {
                            if (data.get("relationship-key").equalsIgnoreCase("service-instance.service-instance-id")) {
                                allotedResource.add(data.get("relationship-value"));
                            }

                        });
                    }
                });

                return fetchSnssaiOfSliceProfile(fetchSliceProfilesOfAllotedResourceData(allotedResource));

            }
            if (serviceInstance.getServiceRole().equalsIgnoreCase("nssi")) {
                serviceInstance.getRelationshipList().getRelationship().forEach(relationship -> {
                    if (Objects.nonNull(relationship.getRelatedToProperty())) {
                        relationship.getRelatedToProperty().forEach(property -> {
                            if (property.get("property-value").contains("sliceprofile")) {
                                relationship.getRelationshipData().forEach(data -> {
                                    if (data.get("relationship-key")
                                            .equalsIgnoreCase("service-instance.service-instance-id")) {
                                        sliceProfileList.add(data.get("relationship-value"));
                                    }

                                });

                            }

                        });
                    }
                });
                return fetchSnssaiOfSliceProfile(sliceProfileList);
            }

        } catch (Exception e) {
            log.info("Exception while fetching snssaiList: " + e);
        }

        return snssaiList;

    }

    /**
     * Fetches the sliceProfileList of a AllotedResource
     *
     * @param allotedResourceList contains list of allotedResource IDs
     * @return sliceProfilesList contains list of SliceProfiles
     */
    public List<String> fetchSliceProfilesOfAllotedResourceData(List<String> allotedResourceList) {

        List<String> sliceProfileList = new ArrayList<>();

        log.info("fetch Alloted Resource Data");

        allotedResourceList.forEach(serviceInstanceId -> {
            try {
                String serviceReqUrl = aaiBaseUrl + "/business/customers/customer/" + globalSubscriberId
                        + "/service-subscriptions/service-subscription/" + subscriptionServiceType
                        + "/service-instances/service-instance/" + serviceInstanceId;
                String serviceInstanceString =
                        restclient.sendGetRequest(serviceReqUrl, new ParameterizedTypeReference<String>() {}).getBody();
                ServiceInstance serviceInstance = objectMapper.readValue(serviceInstanceString, ServiceInstance.class);

                serviceInstance.getRelationshipList().getRelationship().forEach(relationship -> {
                    relationship.getRelatedToProperty().forEach(property -> {
                        if (property.get("property-value").contains("sliceprofile")) {
                            relationship.getRelationshipData().forEach(data -> {
                                if (data.get("relationship-key")
                                        .equalsIgnoreCase("service-instance.service-instance-id")) {
                                    sliceProfileList.add(data.get("relationship-value"));
                                }

                            });

                        }

                    });

                });
            } catch (Exception e) {
                log.info("Exception while fetching AllotedResourceData: " + e);
            }

        });

        log.info("sliceProfileList: " + sliceProfileList);

        return sliceProfileList;

    }

    /**
     * Fetches the snssaiList of a SliceProfile
     *
     * @param sliceProfileList contains list of sliceProfile IDs
     * @return snssaiList contains list of SNSSAIs
     */
    public List<String> fetchSnssaiOfSliceProfile(List<String> sliceProfileList) {
        List<String> snssaiList = new ArrayList<>();

        log.info("fetch SliceProfile");
        sliceProfileList.forEach(serviceInstanceId -> {
            String serviceReqUrl = aaiBaseUrl + "/business/customers/customer/" + globalSubscriberId
                    + "/service-subscriptions/service-subscription/" + subscriptionServiceType + "/service-instances/"
                    + "service-instance/" + serviceInstanceId;

            try {
                String serviceInstanceString =
                        restclient.sendGetRequest(serviceReqUrl, new ParameterizedTypeReference<String>() {}).getBody();
                ServiceInstance serviceInstance = objectMapper.readValue(serviceInstanceString, ServiceInstance.class);

                if (serviceInstance.getServiceRole().equalsIgnoreCase("slice-profile")) {
                    if (!snssaiList.contains(serviceInstance.getEnvironmentContext())) {
                        snssaiList.add(serviceInstance.getEnvironmentContext());
                    }
                }

            } catch (Exception e) {
                log.info("Exception while fetching sliceProfile data: " + e);
            }

        });
        log.info("snssaiList: " + snssaiList);

        return snssaiList;

    }

    /**
     * Get network bandwidth attribute of an ethernet service. These data is inside a network policy whose
     * etwork-policy-fqdn equals to provided service-instance-id
     * @param serviceId target service instance id
     * @return Map contains maxBandwidth value of given service-instance
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
