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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.onap.slice.analysis.ms.models.MeasurementObject;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SnssaiSamplesProcessorTest.class)
public class SnssaiSamplesProcessorTest {
	ObjectMapper obj = new ObjectMapper();

	@InjectMocks
	SnssaiSamplesProcessor snssaiSamplesProcessor;
	
	@Before
	public void setup() {
		Map<String, Map<String, Integer>> ricToThroughputMapping = new HashMap<>();
		Map<String, Integer> ric1 = new HashMap<>();
		Map<String, Integer> ric2 = new HashMap<>();
		ric1.put("dLThptPerSlice",50);
		ric1.put("uLThptPerSlice",40);
		ric2.put("dLThptPerSlice",50);
		ric2.put("uLThptPerSlice",30);		
		ricToThroughputMapping.put("1", ric1);
		ricToThroughputMapping.put("2", ric2);	
		ReflectionTestUtils.setField(snssaiSamplesProcessor, "ricToThroughputMapping", ricToThroughputMapping);
	
		Map<String, Map<String, Integer>> ricToPrbsMapping = null;
		List<MeasurementObject> sliceMeasList = null;
		Map<String, List<String>> ricToCellMapping = null;
		Map<String, String> prbThroughputMapping = new HashMap<>(); 
		prbThroughputMapping = new HashMap<>();
		prbThroughputMapping.put("PrbUsedDl", "dLThptPerSlice");
		prbThroughputMapping.put("PrbUsedUl", "uLThptPerSlice");

		try { 
			ricToPrbsMapping = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/ricToPrbMap.json"))), new TypeReference<Map<String, Map<String, Integer>>>(){});
        	sliceMeasList = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/sliceMeasurementList.json"))), new TypeReference<List<MeasurementObject>>(){});
        	ricToCellMapping = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/ricToCellMapping.json"))), new TypeReference<Map<String, List<String>>>(){});
		} 
       catch (IOException e) { 
            e.printStackTrace(); 
       }
		ReflectionTestUtils.setField(snssaiSamplesProcessor, "ricToPrbsMapping", ricToPrbsMapping);
		ReflectionTestUtils.setField(snssaiSamplesProcessor, "minPercentageChange", 6);
		ReflectionTestUtils.setField(snssaiSamplesProcessor, "snssaiMeasurementList", sliceMeasList);
		ReflectionTestUtils.setField(snssaiSamplesProcessor, "ricToCellMapping", ricToCellMapping);
		ReflectionTestUtils.setField(snssaiSamplesProcessor, "prbThroughputMapping", prbThroughputMapping);
	}
	
	@Test
	public void computeSumTest() {
		assertEquals(Integer.valueOf(100), snssaiSamplesProcessor.computeSum("PrbUsedDl"));
	}
	
	@Test
	public void updateConfigurationTest() {
		Map<String, Map<String, Integer>> ricToThroughputMappingExp = new HashMap<>();
		Map<String, Integer> ric1 = new HashMap<>();
		Map<String, Integer> ric2 = new HashMap<>();
		ric1.put("dLThptPerSlice",50);
		ric1.put("uLThptPerSlice",40);
		ric2.put("dLThptPerSlice",50);
		ric2.put("uLThptPerSlice",30);		
		ricToThroughputMappingExp.put("1", ric1);
		ricToThroughputMappingExp.put("2", ric2);	
		snssaiSamplesProcessor.updateConfiguration();
		assertEquals(ricToThroughputMappingExp,ReflectionTestUtils.getField(snssaiSamplesProcessor, "ricToThroughputMapping"));
	}
	
	@Test
	public void updateConfigurationTrueTest() {
		Map<String, Map<String, Integer>> ricToThroughputMappingExp = new HashMap<>();
		Map<String, Integer> ric2 = new HashMap<>();
		ric2.put("dLThptPerSlice",50);
		ric2.put("uLThptPerSlice",30);		
		ricToThroughputMappingExp.put("2", ric2);	
		
		Map<String, Map<String, Integer>> ricToThroughputMapping = new HashMap<>();
		Map<String, Integer> ric1 = new HashMap<>();
		ric2 = new HashMap<>();
		ric2.put("dLThptPerSlice",50);
		ric2.put("uLThptPerSlice",30);	
		ricToThroughputMapping.put("1", ric1);	
		ricToThroughputMapping.put("2", ric2);	
		ReflectionTestUtils.setField(snssaiSamplesProcessor, "ricToThroughputMapping", ricToThroughputMapping);

		snssaiSamplesProcessor.updateConfiguration();
		System.out.println();
		assertEquals(ricToThroughputMappingExp, ReflectionTestUtils.getField(snssaiSamplesProcessor, "ricToThroughputMapping"));
	}
	
	@Test
	public void calculatePercentageChangeTest() {
		Map<String, Map<String, Integer>> ricConfiguration =  null;
		Map<String, Map<String, Integer>> exp = new HashMap<>();
		Map<String, Integer> ric1 = new HashMap<>();
		Map<String, Integer> ric2 = new HashMap<>();
		ric1.put("dLThptPerSlice", 50);
		ric2.put("dLThptPerSlice", 50);
		ric2.put("uLThptPerSlice", 30);
		exp.put("1", ric1);
		exp.put("2", ric2);	
		try { 
			ricConfiguration = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/ricConfiguration.json"))), new TypeReference<Map<String, Map<String, Integer>>>(){});
       } 
       catch (IOException e) { 
            e.printStackTrace(); 
       }
	   snssaiSamplesProcessor.calculatePercentageChange(ricConfiguration, "uLThptPerSlice");
	   assertEquals(exp,ReflectionTestUtils.getField(snssaiSamplesProcessor, "ricToThroughputMapping"));
	   
	   ricConfiguration.get("2").put("dLThptPerSlice",60);
	   exp.get("1").remove("dLThptPerSlice");
	   snssaiSamplesProcessor.calculatePercentageChange(ricConfiguration, "dLThptPerSlice");
	   assertEquals(exp,ReflectionTestUtils.getField(snssaiSamplesProcessor, "ricToThroughputMapping"));
	}
	
	@Test
	public void sumOfPrbsAcrossCellsTest() {
		Map<String, Map<String, Integer>> ricToPrbsMapping = new HashMap<>();
		Map<String, Map<String, Integer>> ricToPrbsMappingExp = new HashMap<>();

		ReflectionTestUtils.setField(snssaiSamplesProcessor, "ricToPrbsMapping", ricToPrbsMapping);

        try { 
        	ricToPrbsMappingExp = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/ricToPrbOutput.json"))), new TypeReference<Map<String, Map<String, Integer>>>(){});
        } 
        catch (IOException e) { 
            e.printStackTrace(); 
        } 
        snssaiSamplesProcessor.sumOfPrbsAcrossCells("PrbUsedDl");
        assertEquals(ricToPrbsMappingExp, ReflectionTestUtils.getField(snssaiSamplesProcessor, "ricToPrbsMapping"));
	}
	
	@Test
	public void computeThroughputTest() {
		Map<String, Map<String, Integer>> ricToThroughputMapping = new HashMap<>();
		ReflectionTestUtils.setField(snssaiSamplesProcessor, "ricToThroughputMapping", ricToThroughputMapping);

		Map<String, Map<String, Integer>> ricToThroughputMappingExp = new HashMap<>();
		try { 
			ricToThroughputMappingExp = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/ricToThroughputMappingOutput.json"))), new TypeReference<Map<String, Map<String, Integer>>>(){});
        } 
        catch (IOException e) { 
            e.printStackTrace(); 
        } 
		Map<String, Integer> sliceConfiguration = new HashMap<String, Integer>();
		sliceConfiguration.put("dLThptPerSlice",120);
		sliceConfiguration.put("uLThptPerSlice",100);
		snssaiSamplesProcessor.computeThroughput(sliceConfiguration, 100, "PrbUsedDl");
		snssaiSamplesProcessor.computeThroughput(sliceConfiguration, 70, "PrbUsedUl");
		assertEquals(ricToThroughputMappingExp, ReflectionTestUtils.getField(snssaiSamplesProcessor, "ricToThroughputMapping"));
	}
}
