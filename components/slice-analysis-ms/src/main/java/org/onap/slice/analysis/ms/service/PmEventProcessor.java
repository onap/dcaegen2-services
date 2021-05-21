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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.onap.slice.analysis.ms.models.MeasurementObject;
import org.onap.slice.analysis.ms.models.pmnotification.Event;
import org.onap.slice.analysis.ms.models.pmnotification.MeasInfoList;
import org.onap.slice.analysis.ms.models.pmnotification.MeasResult;
import org.onap.slice.analysis.ms.models.pmnotification.MeasValuesList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** 
 * This class is responsible for processing PmEvent
 */
@Component
@Scope("prototype")
public class PmEventProcessor implements IPmEventProcessor{
	protected Map<String, List<MeasurementObject>> instanceMap = new HashMap<>();
	private static Logger log = LoggerFactory.getLogger(PmEventProcessor.class);

	/**
	 * Process the PM event
	 */
	public Map<String, List<MeasurementObject>> processEvent(Event event) {
		List<MeasInfoList> measurements = event.getPerf3gppFields().getMeasDataCollection().getMeasInfoList();
		measurements.forEach(measurement -> {
			List<String> collectedSubCounters = measurement.getMeasTypes().getsMeasTypesList();
			List<MeasValuesList> subCounterMeasurements = measurement.getMeasValuesList();	
			subCounterMeasurements.forEach(subCounterMeasurement -> processMeasurementObjectData(collectedSubCounters, subCounterMeasurement));
		});
		log.info("Processed Event: {}", instanceMap);
		return instanceMap;
	}

	/**
	 * Process the measurement data from every measurement object. eg cell
	 */
	public void processMeasurementObjectData(List<String> collectedSubCounters, MeasValuesList subCounterMeasurement) {
		List<MeasResult> measResultList = subCounterMeasurement.getMeasResults();
		String measObjId = subCounterMeasurement.getMeasObjInstId();
		measResultList.forEach(measResult -> {
			String pmName = collectedSubCounters.get(measResult.getP()-1);
			Integer pmValue = Integer.valueOf(measResult.getsValue());
			if(pmName.contains("PrbUsedUl") || pmName.contains("PrbUsedDl")) {
				Map<String,String> pmMapping = getMapKey(pmName);
				String snssai = pmMapping.get("snssai");
				String pm = pmMapping.get("pm"); 
				Map<String, Integer> pmData = new HashMap<>();
				pmData.put(pm, pmValue);
				if (instanceMap.containsKey(snssai)) {
					int index = MeasurementObject.findIndex(measObjId, instanceMap.get(snssai));
					if (index == -1) {
						instanceMap.get(snssai).add(new MeasurementObject(measObjId,pmData));
					}
					else {
						instanceMap.get(snssai).get(index).getPmData().put(pm, pmValue);
					}		    	
				}
				else {
					List<MeasurementObject> l = new LinkedList<>();
					l.add(new MeasurementObject(measObjId,pmData));
					instanceMap.put(snssai, l);
				}
			}
		});	
	}

	/**
	 * Fetch pm name and S-NSSAI
	 */
	public Map<String, String> getMapKey(String pmName) {
		String [] pmNameArr = pmName.split("\\.");
		String pm = pmNameArr[1];
		Map<String, String> result = new HashMap<>();
		result.put("pm", pm);
		result.put("snssai", pmNameArr[2]);
		return result;
	}

}