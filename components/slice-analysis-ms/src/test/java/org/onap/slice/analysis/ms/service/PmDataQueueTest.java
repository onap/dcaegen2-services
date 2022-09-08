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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.onap.slice.analysis.ms.models.MeasurementObject;
import org.onap.slice.analysis.ms.models.SubCounter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PmDataQueueTest.class)
public class PmDataQueueTest {
	ObjectMapper obj = new ObjectMapper();
	
	@InjectMocks
	PmDataQueue pmDataQueue;

	@Before
	public void setup() {
		Queue<List<MeasurementObject>> measList = null;
        try { 
              measList = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/measurementObjectList.json"))), new TypeReference<Queue<List<MeasurementObject>>>(){});
        } 
        catch (IOException e) { 
            e.printStackTrace(); 
        } 
        SubCounter sub1 = new SubCounter("nf1", "nssai1");
    	Map<SubCounter, Queue<List<MeasurementObject>>> subCounterMap = Collections.synchronizedMap(new LinkedHashMap<SubCounter, Queue<List<MeasurementObject>>>());
    	subCounterMap.put(sub1, measList);
		ReflectionTestUtils.setField(pmDataQueue, "subCounterMap", subCounterMap);
		
		Queue<String> snssaiList = new LinkedBlockingQueue<>();
		snssaiList.add("nssai1");
		snssaiList.add("nssai2");
		snssaiList.add("nssai3");
		ReflectionTestUtils.setField(pmDataQueue, "snssaiList", snssaiList);

	}
	
	@Test
	public void putDataToQueueSameNssaiTest() {
        SubCounter sub1 = new SubCounter("nf1", "nssai1");
    	Map<SubCounter, Queue<List<MeasurementObject>>> subCounterMapExp = Collections.synchronizedMap(new LinkedHashMap<SubCounter, Queue<List<MeasurementObject>>>());
        List<MeasurementObject> measObj = null;
        Queue<List<MeasurementObject>> measObjExp = null;
        try { 
              measObj = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/average.json"))), new TypeReference<List<MeasurementObject>>(){});
              measObjExp = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/appendData.json"))), new TypeReference<Queue<List<MeasurementObject>>>(){});

        } 
        catch (IOException e) { 
            e.printStackTrace(); 
        } 
        subCounterMapExp.put(sub1, measObjExp);
        pmDataQueue.putDataToQueue(sub1, measObj);
        assertEquals(subCounterMapExp, ReflectionTestUtils.getField(pmDataQueue, "subCounterMap"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void putDataToQueueDiffNssaiTest() {
		SubCounter sub = new SubCounter("nf1", "nssai1");
		SubCounter sub1 = new SubCounter("nf1", "nssai2");
    	Map<SubCounter, Queue<List<MeasurementObject>>> subCounterMapExp = Collections.synchronizedMap(new LinkedHashMap<SubCounter, Queue<List<MeasurementObject>>>());
        List<MeasurementObject> measObj = null;
        Queue<List<MeasurementObject>> measObjExp = null;
        Queue<List<MeasurementObject>> measObjExp1 = new LinkedBlockingQueue<>();
        try { 
              measObj = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/average.json"))), new TypeReference<List<MeasurementObject>>(){});
              measObjExp = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/measurementObjectList.json"))), new TypeReference<Queue<List<MeasurementObject>>>(){});
        } 
        catch (IOException e) { 
            e.printStackTrace(); 
        } 
        measObjExp1.add(measObj);
        subCounterMapExp.put(sub, measObjExp);
        subCounterMapExp.put(sub1, measObjExp1);
        pmDataQueue.putDataToQueue(sub1, measObj);
        assertEquals(subCounterMapExp.get(sub), ((Map<SubCounter,Queue<List<MeasurementObject>>>) ReflectionTestUtils.getField(pmDataQueue, "subCounterMap")).get(sub));
        assertEquals(subCounterMapExp.get(sub1).contains(measObj), ((Map<SubCounter,Queue<List<MeasurementObject>>>) ReflectionTestUtils.getField(pmDataQueue, "subCounterMap")).get(sub1).contains(measObj));
	}
	
	@Test
	public void getSamplesFromQueueTest() {
		SubCounter sub = new SubCounter("nf1", "nssai1");
        List<List<MeasurementObject>> measObj = null;
        try { 
            measObj = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/getResponse.json"))), new TypeReference<List<List<MeasurementObject>>>(){});
      } 
      catch (IOException e) { 
          e.printStackTrace(); 
      } 
		assertEquals(measObj, pmDataQueue.getSamplesFromQueue(sub, 1));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void putSnssaiToQueueTest() {
		pmDataQueue.putSnssaiToQueue("nssai1");
		assertEquals(3, ((Queue<String>)ReflectionTestUtils.getField(pmDataQueue, "snssaiList")).size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void putNewSnssaiToQueueTest() {
		pmDataQueue.putSnssaiToQueue("nssai9");
		assertEquals(4, ((Queue<String>)ReflectionTestUtils.getField(pmDataQueue, "snssaiList")).size());
	}
	
	@Test
	public void getSnnsaiFromQueueTest() {
		assertEquals("nssai1", pmDataQueue.getSnnsaiFromQueue());
	}
}
