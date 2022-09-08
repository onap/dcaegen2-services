/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020-2021 Wipro Limited.
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
import java.util.List;

import javax.annotation.PostConstruct;

import org.onap.slice.analysis.ms.models.MeasurementObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** 
 * This class has utility methods for calculating the average of samples
 */
@Component
public class AverageCalculator {
	private static Logger log = LoggerFactory.getLogger(AverageCalculator.class);
	private List<String> pmNames;

	@PostConstruct
	public void init() {
		pmNames = new ArrayList<>();
		pmNames.add("PrbUsedDl");
		pmNames.add("PrbUsedUl");
	}

	/**
	 * Find average of samples
	 */
	public List<MeasurementObject> findAverageOfSamples(List<List<MeasurementObject>> samples) {
		log.debug("find average for samples {}", samples);
		int numOfSamples = samples.size();
		List<MeasurementObject> result = new ArrayList<>();
			for(List<MeasurementObject> sample : samples) {
				for(MeasurementObject cellMeasObj : sample) {
					int index = MeasurementObject.findIndex(cellMeasObj.getMeasurementObjectId(), result);
					if(index != -1) {
						result.set(index, findSum(result.get(index), cellMeasObj));
					}
					else { 
						result.add(cellMeasObj);
					}
				}
			}
		return findAvg(result, numOfSamples);
	}

	/**
	 * Calculate the sum
	 */
	public MeasurementObject findSum(MeasurementObject existing, MeasurementObject current) {
		pmNames.forEach(pmName -> {
			int newValue = current.getPmData().get(pmName) + existing.getPmData().get(pmName);
			existing.getPmData().put(pmName, newValue);
		});
		return existing;
	}

	/**
	 * Calculate the average
	 */
	public List<MeasurementObject> findAvg(List<MeasurementObject> result, int numOfSamples) {
		if(!result.isEmpty()) {
			result.forEach(cellMeasObj ->
			pmNames.forEach(pmName -> {
				int value = (cellMeasObj.getPmData().get(pmName))/numOfSamples;
				cellMeasObj.getPmData().put(pmName, value);
			})
			);
			log.debug("Average of measurement data samples {}",result);
		}
		return result;
	}

}
