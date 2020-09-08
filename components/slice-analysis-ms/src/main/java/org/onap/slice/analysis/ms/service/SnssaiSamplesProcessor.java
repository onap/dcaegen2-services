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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.onap.slice.analysis.ms.configdb.IConfigDbService;
import org.onap.slice.analysis.ms.dmaap.PolicyDmaapClient;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.MeasurementObject;
import org.onap.slice.analysis.ms.models.SubCounter;
import org.onap.slice.analysis.ms.models.policy.AAI;
import org.onap.slice.analysis.ms.models.policy.AdditionalProperties;
import org.onap.slice.analysis.ms.models.policy.OnsetMessage;
import org.onap.slice.analysis.ms.models.policy.Payload;
import org.onap.slice.analysis.ms.utils.DmaapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Scope("Prototype")
public class SnssaiSamplesProcessor {
	
	@PostConstruct
    public void init() {
		Configuration configuration = Configuration.getInstance();
		samples = configuration.getSamples();
		pmsToCompute = new ArrayList<>();
		pmsToCompute.add("PrbUsedDl");
		pmsToCompute.add("PrbUsedUl");
		prbThroughputMapping = new HashMap<>();
		prbThroughputMapping.put("PrbUsedDl", "dLThptPerSlice");
		prbThroughputMapping.put("PrbUsedUl", "uLThptPerSlice");
		minPercentageChange = configuration.getMinPercentageChange();
		policyDmaapClient = new PolicyDmaapClient(new DmaapUtils(), configuration);
		}
	
	@Autowired
	private IConfigDbService configDbService;
	
	@Autowired
	private PmDataQueue pmDataQueue;
	
	@Autowired
	private AverageCalculator averageCalculator;
	
	private PolicyDmaapClient policyDmaapClient;
	
	private List<MeasurementObject> snssaiMeasurementList = new ArrayList<>();
	private Map<String, List<String>> ricToCellMapping = new HashMap<>();
	private Map<String, Map<String, Integer>> ricToPrbsMapping = new HashMap<>();
	private Map<String, Map<String, Integer>> ricToThroughputMapping = new HashMap<>();
	private int samples;
	private List<String> pmsToCompute;	
	private Map<String, String> prbThroughputMapping = new HashMap<>(); // get it from config
	private int minPercentageChange;
	
	
	public void processSamplesOfSnnsai(String snssai, List<String> networkFunctions) {
		networkFunctions.forEach(nf ->
			addToMeasurementList(averageCalculator.findAverageOfSamples(pmDataQueue.getSamplesFromQueue(new SubCounter(nf, snssai), samples)))
		);		
		ricToCellMapping = configDbService.fetchRICsOfSnssai(snssai);	
		Map<String, Map<String, Integer>> ricConfiguration = configDbService.fetchCurrentConfigurationOfRIC(snssai);
		Map<String, Integer> sliceConfiguration = configDbService.fetchCurrentConfigurationOfSlice(snssai);
		pmsToCompute.forEach(pm -> {
			sumOfPrbsAcrossCells(pm);
			int sum = computeSum(pm);
			computeThroughput(sliceConfiguration, sum, pm);
			calculatePercentageChange(ricConfiguration, pm);
			});
		updateConfiguration();	
		if(ricToThroughputMapping.size() > 0) {
			sendOnsetMessageToPolicy(snssai, configDbService.fetchServiceDetails(snssai));
		}
		
	}
	
	protected void updateConfiguration() {
		Iterator<Map.Entry<String, Map<String,Integer>>> it = ricToThroughputMapping.entrySet().iterator();
		Map.Entry<String, Map<String,Integer>> entry = null;
		while(it.hasNext()) {
			entry = it.next();
			if(entry.getValue().size() == 0) {
	        	it.remove();
	        }
		}
	}
	
	
	private void addToMeasurementList(List<MeasurementObject> sample) {
		snssaiMeasurementList.addAll(sample);
	}

	protected void calculatePercentageChange(Map<String, Map<String, Integer>> ricConfiguration, String pm) {
		Iterator<Map.Entry<String, Map<String,Integer>>> it = ricToThroughputMapping.entrySet().iterator();
		Map.Entry<String, Map<String,Integer>> entry = null;
		float existing = 0;
		float change = 0;
		while(it.hasNext()) {
			entry = it.next();
			existing = ricConfiguration.get(entry.getKey()).get(pm);
		    change = ((Math.abs(entry.getValue().get(pm) - existing))/existing)*100;
			if (change <= minPercentageChange) {
				ricToThroughputMapping.get(entry.getKey()).remove(pm);
			}
		}
	}
	/*
	 * ric1: [cell1, cell2, cell3]
	 * ric2: [cell5, cell6, cell4]
	 * 
	 */
	
	protected void sumOfPrbsAcrossCells(String pmName) {
		ricToCellMapping.forEach((ric,cells) -> {
			int sumOfPrbs = 0;
			for(String cell : cells) {
				int index = snssaiMeasurementList.indexOf(new MeasurementObject(cell));
				sumOfPrbs += snssaiMeasurementList.get(index).getPmData().get(pmName);
			}
			if(ricToPrbsMapping.containsKey(ric)) {
				ricToPrbsMapping.get(ric).put(pmName, sumOfPrbs);
			}
			else {
				Map<String, Integer> pmToPrbMapping  = new HashMap<>();
				pmToPrbMapping.put(pmName, sumOfPrbs);
				ricToPrbsMapping.put(ric, pmToPrbMapping);
			}
		});
	}
	
	protected Integer computeSum(String pm) {
		return ricToPrbsMapping.entrySet().stream().map(x -> x.getValue().get(pm)).reduce(0, Integer::sum);
	}
	
	private void computeThroughput(Map<String, Integer> sliceConfiguration, int sum, String pm) {
		Map<String, Integer> throughtputMap = new HashMap<>();
		ricToPrbsMapping.forEach((ric, prbMap) -> {
			int value = (prbMap.get(pm)/sum) * sliceConfiguration.get(prbThroughputMapping.get(pm));
			if(ricToThroughputMapping.containsKey(ric)) {
				ricToThroughputMapping.get(ric).put(prbThroughputMapping.get(pm), value);
			}
			else {
				throughtputMap.put(prbThroughputMapping.get(pm), value);
				ricToThroughputMapping.put(ric, throughtputMap);
			}
		});
	}
	
	protected OnsetMessage formPolicyOnsetMessage(String snssai, Map<String, String> serviceDetails) {
		OnsetMessage onsetmsg = new OnsetMessage();
		Payload payload = new Payload();
		payload.setGlobalSubscriberId(serviceDetails.get("globalSubscriberId"));
		payload.setSubscriptionServiceType(serviceDetails.get("subscriptionServiceType"));
		payload.setNetworkType("AN");
		payload.setName(serviceDetails.get("ranNFNSSIId"));
		payload.setServiceInstanceID(serviceDetails.get("ranNFNSSIId"));
		
		AdditionalProperties addProps = new AdditionalProperties();
		addProps.setModifyAction("");
		Map<String, String> nsiInfo = new HashMap<>();
		nsiInfo.put("nsiId", UUID.randomUUID().toString());
		nsiInfo.put("nsiName", "");
		addProps.setNsiInfo(nsiInfo);
		addProps.setResourceConfig(ricToThroughputMapping);
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
		onsetmsg.setTarget("service-instance.service-instance-name");
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
	
	private void sendOnsetMessageToPolicy(String snssai, Map<String, String> serviceDetails) {
		OnsetMessage onsetMessage = formPolicyOnsetMessage(snssai, serviceDetails);
		ObjectMapper obj = new ObjectMapper();
		String msg =  "";
        try { 
              msg = obj.writeValueAsString(onsetMessage);
              policyDmaapClient.sendNotificationToPolicy(msg);
        } 
        catch (IOException e) { 
            e.printStackTrace(); 
        } 		
	}
	
}
