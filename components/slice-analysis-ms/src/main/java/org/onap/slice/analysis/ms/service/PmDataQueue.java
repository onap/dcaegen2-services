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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.onap.slice.analysis.ms.models.MeasurementObject;
import org.onap.slice.analysis.ms.models.SubCounter;
import org.springframework.stereotype.Component;

@Component
public class PmDataQueue {
	private Map<SubCounter, Queue<List<MeasurementObject>>> subCounterMap = Collections.synchronizedMap(new LinkedHashMap<SubCounter, Queue<List<MeasurementObject>>>());
	private Queue<String> snssaiList = new LinkedBlockingQueue<>();
	
	public void putDataToQueue(SubCounter subCounter, List<MeasurementObject> measurementObjectData) {
		Queue<List<MeasurementObject>> measQueue;
		if (subCounterMap.containsKey(subCounter)){
			subCounterMap.get(subCounter).add(measurementObjectData);
		}
		else {
			measQueue = new LinkedBlockingQueue<>();
			measQueue.add(measurementObjectData);
			subCounterMap.put(subCounter, measQueue);
		}
	}
	
	public List<List<MeasurementObject>> getSamplesFromQueue(SubCounter subCounter, int samples) {
		List<List<MeasurementObject>> sampleList = new LinkedList<>();
		if (subCounterMap.containsKey(subCounter)){
			Queue<List<MeasurementObject>> measQueue = subCounterMap.get(subCounter);
			while(samples > 0) {
				sampleList.add(measQueue.remove());
				samples --;
			}
		}
		return sampleList;
	}
		
	public void putSnssaiToQueue(String snssai) {
		if (!snssaiList.contains(snssai)) 
			snssaiList.add(snssai);
	}
	
	public String getSnnsaiFromQueue() {
		return snssaiList.remove();
	}
}
