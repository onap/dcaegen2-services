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

package org.onap.slice.analysis.ms.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.onap.slice.analysis.ms.dmaap.PolicyDmaapClient;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.policy.AAI;
import org.onap.slice.analysis.ms.models.policy.AdditionalProperties;
import org.onap.slice.analysis.ms.models.policy.OnsetMessage;
import org.onap.slice.analysis.ms.models.policy.Payload;
import org.onap.slice.analysis.ms.utils.DmaapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PolicyService {
	private PolicyDmaapClient policyDmaapClient;
	private static Logger log = LoggerFactory.getLogger(PolicyService.class);

	@PostConstruct
	public void init() {
		Configuration configuration = Configuration.getInstance();
		policyDmaapClient = new PolicyDmaapClient(new DmaapUtils(), configuration);
	}
	
	protected <T> OnsetMessage formPolicyOnsetMessage(String snssai, AdditionalProperties<T> addProps, Map<String, String> serviceDetails) {
		OnsetMessage onsetmsg = new OnsetMessage();
		Payload payload = new Payload();
		payload.setGlobalSubscriberId(serviceDetails.get("globalSubscriberId"));
		payload.setSubscriptionServiceType(serviceDetails.get("subscriptionServiceType"));
		payload.setNetworkType("AN");
		payload.setName(serviceDetails.get("ranNFNSSIId"));
		payload.setServiceInstanceID(serviceDetails.get("ranNFNSSIId"));

		addProps.setModifyAction("");
		Map<String, String> nsiInfo = new HashMap<>();
		nsiInfo.put("nsiId", UUID.randomUUID().toString());
		nsiInfo.put("nsiName", "");
		addProps.setNsiInfo(nsiInfo);
		addProps.setScriptName("AN");
		addProps.setSliceProfileId(serviceDetails.get("sliceProfileId"));
		addProps.setModifyAction("reconfigure");
		List<String> snssaiList = new ArrayList<>();
		snssaiList.add(snssai);
		addProps.setSnssaiList(snssaiList);

		payload.setAdditionalProperties(addProps);
		onsetmsg.setPayload(payload);

		onsetmsg.setClosedLoopControlName("ControlLoop-Slicing-116d7b00-dbeb-4d03-8719-d0a658fa735b");
		onsetmsg.setClosedLoopAlarmStart(System.currentTimeMillis());
		onsetmsg.setClosedLoopEventClient("microservice.sliceAnalysisMS");
		onsetmsg.setClosedLoopEventStatus("ONSET");
		onsetmsg.setRequestID(UUID.randomUUID().toString());
		onsetmsg.setTarget("vserver.vserver-name");
		onsetmsg.setTargetType("VNF");
		onsetmsg.setFrom("DCAE");
		onsetmsg.setVersion("1.0.2");
		AAI aai = new AAI();
		aai.setVserverIsClosedLoopDisabled("false");
		aai.setVserverProvStatus("ACTIVE");
		aai.setVserverVserverName(serviceDetails.get("ranNFNSSIId"));
		onsetmsg.setAai(aai); 
		return onsetmsg;
	}

	protected <T> void sendOnsetMessageToPolicy(String snssai, AdditionalProperties<T> addProps, Map<String, String> serviceDetails) {
		OnsetMessage onsetMessage = formPolicyOnsetMessage(snssai, addProps, serviceDetails);
		ObjectMapper obj = new ObjectMapper();
		String msg =  "";
		try { 
			msg = obj.writeValueAsString(onsetMessage);
			log.info("Policy onset message for S-NSSAI: {} is {}", snssai, msg);
			policyDmaapClient.sendNotificationToPolicy(msg);
		} 
		catch (Exception e) { 
			log.error("Error sending notification to policy, {}",e.getMessage());
		} 		
	}

}
