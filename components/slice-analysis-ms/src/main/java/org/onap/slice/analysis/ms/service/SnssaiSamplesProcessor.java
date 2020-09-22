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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.onap.slice.analysis.ms.configdb.IConfigDbService;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.MeasurementObject;
import org.onap.slice.analysis.ms.models.SubCounter;
import org.onap.slice.analysis.ms.models.policy.AdditionalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** 
 * This class process the measurement data of an S-NSSAI
 */
@Component
@Scope("Prototype")
public class SnssaiSamplesProcessor {
	private static Logger log = LoggerFactory.getLogger(SnssaiSamplesProcessor.class);

	@Autowired
	private PolicyService policyService;
	
	@Autowired
	private IConfigDbService configDbService;
	
	@Autowired
	private PmDataQueue pmDataQueue;
	
	@Autowired
	private AverageCalculator averageCalculator;
	
	private List<MeasurementObject> snssaiMeasurementList = new ArrayList<>();
	private Map<String, List<String>> ricToCellMapping = new HashMap<>();
	private Map<String, Map<String, Integer>> ricToPrbsMapping = new HashMap<>();
	private Map<String, Map<String, Integer>> ricToThroughputMapping = new HashMap<>();
	private int samples;
	private List<String> pmsToCompute;	
	private Map<String, String> prbThroughputMapping = new HashMap<>(); 
	private int minPercentageChange;
	
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
	}

	/**
	 * process the measurement data of an S-NSSAI
	 */
	public void processSamplesOfSnnsai(String snssai, List<String> networkFunctions) {
		networkFunctions.forEach(nf -> {
			log.debug("Average of samples for {}:", snssai);
			addToMeasurementList(averageCalculator.findAverageOfSamples(pmDataQueue.getSamplesFromQueue(new SubCounter(nf, snssai), samples)));
		});		
		ricToCellMapping = configDbService.fetchRICsOfSnssai(snssai);	
		log.debug("RIC to Cell Mapping for {} S-NSSAI: {}", snssai, ricToCellMapping);
		Map<String, Map<String, Integer>> ricConfiguration = configDbService.fetchCurrentConfigurationOfRIC(snssai);
		Map<String, Integer> sliceConfiguration = configDbService.fetchCurrentConfigurationOfSlice(snssai);
		log.debug("RIC Configuration: {}", ricConfiguration);
		log.debug("Slice Configuration: {}", sliceConfiguration);
		pmsToCompute.forEach(pm -> {
			sumOfPrbsAcrossCells(pm);
			int sum = computeSum(pm);
			computeThroughput(sliceConfiguration, sum, pm);
			calculatePercentageChange(ricConfiguration, pm);
		});
		updateConfiguration();	
		if(ricToThroughputMapping.size() > 0) {
			AdditionalProperties<Map<String, Map<String, Integer>>> addProps = new AdditionalProperties<>();
			addProps.setResourceConfig(ricToThroughputMapping);
			policyService.sendOnsetMessageToPolicy(snssai, addProps, configDbService.fetchServiceDetails(snssai));
		}

	}

	/**
	 * process the measurement data of an S-NSSAI
	 */
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

	/**
	 * Calculate the change in the configuration value and keep the configuration only if it is greater than a
	 * specific limit 
	 */
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

	protected void computeThroughput(Map<String, Integer> sliceConfiguration, int sum, String pm) {
		Iterator<Map.Entry<String, Map<String,Integer>>> it = ricToPrbsMapping.entrySet().iterator();
		Map.Entry<String, Map<String,Integer>> entry = null;
		Map<String, Integer> throughtputMap = null;
		String ric = "";
		int value = 0;
		while(it.hasNext()) {
			entry = it.next();
			ric = entry.getKey();
			value = Math.round(((float)entry.getValue().get(pm)/sum)*(float)sliceConfiguration.get(prbThroughputMapping.get(pm)));
			if(ricToThroughputMapping.containsKey(ric)) {
				ricToThroughputMapping.get(ric).put(prbThroughputMapping.get(pm), value);
			}
			else {
				throughtputMap = new HashMap<>();
				throughtputMap.put(prbThroughputMapping.get(pm), value);
				ricToThroughputMapping.put(ric, throughtputMap);
			}
		}

	}

}
