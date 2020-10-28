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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** 
 * This class represents the data structure for storing the pm events 
 */
@Component
public class PmDataQueue {
    private static Logger log = LoggerFactory.getLogger(PmDataQueue.class);

	private Map<SubCounter, Queue<List<MeasurementObject>>> subCounterMap = Collections.synchronizedMap(new LinkedHashMap<SubCounter, Queue<List<MeasurementObject>>>());
	private Queue<String> snssaiList = new LinkedBlockingQueue<>();

	/**
	 * put the measurement data for (an S-NSSAI from a network function) in the queue
	 */
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
		log.debug("Queue: {}", subCounterMap);
	}

	/**
	 * get the measurement data for (an S-NSSAI from a network function) from the queue
	 * returns the specified number of samples
	 */
	public List<List<MeasurementObject>> getSamplesFromQueue(SubCounter subCounter, int samples) {
		List<List<MeasurementObject>> sampleList = null;
		if (subCounterMap.containsKey(subCounter)){
			Queue<List<MeasurementObject>> measQueue = subCounterMap.get(subCounter);
			if(measQueue.size() >= samples) {
				sampleList = new LinkedList<>();
				while(samples > 0) {
					sampleList.add(measQueue.remove());
					samples --;
				}
			}
		}
		return sampleList;
	}

	/**
	 * put S-NSSAI to the queue
	 */
	public void putSnssaiToQueue(String snssai) {
		if (!snssaiList.contains(snssai)) 
			snssaiList.add(snssai);
	}

	/**
	 * get S-NSSAI from the queue
	 */
	public String getSnnsaiFromQueue() {
		String snssai = "";
		try {
			if(!snssaiList.isEmpty()){
				snssai = snssaiList.remove();
			}
		}
		catch(Exception e) {
			log.error("Problem fetching from the Queue, {}", e.getMessage());
		}
		return snssai;
	}
}
