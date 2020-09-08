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
import java.util.List;

import javax.annotation.PostConstruct;

import org.onap.slice.analysis.ms.models.MeasurementObject;
import org.springframework.stereotype.Component;

@Component
public class AverageCalculator {
	private List<String> pmNames;
	
	@PostConstruct
    public void init() {
		pmNames = new ArrayList<>();
		pmNames.add("PrbUsedDl");
		pmNames.add("PrbUsedUl");
    }
	
	public List<MeasurementObject> findAverageOfSamples(List<List<MeasurementObject>> samples) {
		int numOfSamples = samples.size();
		List<MeasurementObject> result = new ArrayList<>();
		samples.forEach(sample -> 
			sample.forEach(cellMeasObj -> {
				int index = result.indexOf(cellMeasObj);
				if(index != -1) {
					result.set(index, findSum(result.get(index), cellMeasObj));
				}
				else { 
					result.add(cellMeasObj);
				}
			})
		);
		return findAvg(result, numOfSamples);
	}
	
	public MeasurementObject findSum(MeasurementObject existing, MeasurementObject current) {
		pmNames.forEach(pmName -> {
			int newValue = current.getPmData().get(pmName) + existing.getPmData().get(pmName);
			existing.getPmData().put(pmName, newValue);
		});
		return existing;
	}
		
	public List<MeasurementObject> findAvg(List<MeasurementObject> result, int numOfSamples) {
		result.forEach(cellMeasObj ->
			pmNames.forEach(pmName -> {
				int value = (cellMeasObj.getPmData().get(pmName))/numOfSamples;
				cellMeasObj.getPmData().put(pmName, value);
			})
		);
		return result;
	}
}

