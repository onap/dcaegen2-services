/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
 *   Copyright (C) 2022 CTC, Inc.
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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.slice.analysis.ms.aai.AaiInterface;
import org.onap.slice.analysis.ms.configdb.IConfigDbService;
import org.onap.slice.analysis.ms.cps.CpsInterface;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.MeasurementObject;
import org.onap.slice.analysis.ms.models.SubCounter;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@PrepareForTest(SnssaiSamplesProcessor.class)
@SpringBootTest(classes = SnssaiSamplesProcessorTest.class)
public class SnssaiSamplesProcessorTest {
    ObjectMapper obj = new ObjectMapper();

    @InjectMocks
    SnssaiSamplesProcessor snssaiSamplesProcessor;
    @Mock
    private PolicyService policyService;

    @Mock
    private PmDataQueue pmDataQueue;

    @Mock
    private AverageCalculator averageCalculator;

    @Mock
    private IConfigDbService configDbService;

    @Mock
    private AaiInterface aaiInterface;

    @Mock
    private CpsInterface cpsInterface;

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
    public void processSamplesOfSnnsaiTest() {
        List<List<MeasurementObject>> samples = new ArrayList<>();
        when(pmDataQueue.getSamplesFromQueue(any(SubCounter.class),anyInt())).thenReturn(samples);
        List<MeasurementObject> sample = new ArrayList<>();
        when(averageCalculator.findAverageOfSamples(samples)).thenReturn(sample);

        Map<String, List<String>> ricToCellMapping = new HashMap<>();
        when(configDbService.fetchRICsOfSnssai(any())).thenReturn(ricToCellMapping);
        Map<String, Map<String, Object>> ricConfiguration = new HashMap<>();
        when(configDbService.fetchCurrentConfigurationOfRIC(any())).thenReturn(ricConfiguration);
        Map<String, Integer> sliceConfiguration = new HashMap<>();
        when(configDbService.fetchCurrentConfigurationOfSlice(any())).thenReturn(sliceConfiguration);
        Map<String, String> serviceDetails = new HashMap<>();
        when(configDbService.fetchServiceDetails(any())).thenReturn(serviceDetails);

        List<String> networkFunctions = new ArrayList<>();
        networkFunctions.add("nf1");
        SnssaiSamplesProcessor spy = PowerMockito.spy(snssaiSamplesProcessor);
        doNothing().when(spy).sumOfPrbsAcrossCells(anyString());
        doReturn(1).when(spy).computeSum(any());
        doNothing().when(spy).computeThroughput(any(),anyInt(),any());
        doNothing().when(spy).calculatePercentageChange(any(),any());
        doNothing().when(spy).updateConfiguration();

        Map<String, List<Map<String, Integer>>> map = new HashMap<>();
        doReturn(map).when(spy).getChangedRIConfigFormat(any());
        doNothing().when(policyService).sendOnsetMessageToPolicy(any(),any(), any());

        spy.init();
        boolean b = spy.processSamplesOfSnnsai("", networkFunctions);
        assertTrue(b);


    }
    @Test
    public void processSamplesOfSnnsaiFalseTest() {
        Configuration.getInstance().setConfigDbEnabled(false);
        List<List<MeasurementObject>> samples = new ArrayList<>();
        when(pmDataQueue.getSamplesFromQueue(any(SubCounter.class),anyInt())).thenReturn(samples);
        List<MeasurementObject> sample = new ArrayList<>();
        when(averageCalculator.findAverageOfSamples(samples)).thenReturn(sample);

        Map<String, List<String>> ricToCellMapping = new HashMap<>();
        when(cpsInterface.fetchRICsOfSnssai(any())).thenReturn(ricToCellMapping);
        Map<String, Map<String, Object>> ricConfiguration = new HashMap<>();
        when(cpsInterface.fetchCurrentConfigurationOfRIC(any())).thenReturn(ricConfiguration);
        Map<String, Integer> sliceConfiguration = new HashMap<>();
        when(aaiInterface.fetchCurrentConfigurationOfSlice(any())).thenReturn(sliceConfiguration);
        Map<String, String> serviceDetails = new HashMap<>();
        when(aaiInterface.fetchServiceDetails(any())).thenReturn(serviceDetails);


        List<String> networkFunctions = new ArrayList<>();
        networkFunctions.add("nf1");
        SnssaiSamplesProcessor spy = PowerMockito.spy(snssaiSamplesProcessor);
        doNothing().when(spy).sumOfPrbsAcrossCells(anyString());
        doReturn(1).when(spy).computeSum(any());
        doNothing().when(spy).computeThroughput(any(),anyInt(),any());
        doNothing().when(spy).calculatePercentageChange(any(),any());
        doNothing().when(spy).updateConfiguration();

        Map<String, List<Map<String, Integer>>> map = new HashMap<>();
        doReturn(map).when(spy).getChangedRIConfigFormat(any());
        doNothing().when(policyService).sendOnsetMessageToPolicy(any(),any(), any());

        spy.init();
        boolean b = spy.processSamplesOfSnnsai("", networkFunctions);
        assertTrue(b);
    }
    @Test
    public void getChangedRIConfigFormatTest() {
        Map<String, Map<String, Integer>> ricToThroughputMapping = new HashMap<>();
        Map<String, Integer> newConfigMap = new HashMap<>();
        ricToThroughputMapping.put("1", newConfigMap);
        Map<String, List<Map<String, Integer>>> riConfigFormat = snssaiSamplesProcessor.getChangedRIConfigFormat(ricToThroughputMapping);
        assertEquals(1, riConfigFormat.size());

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
        Map<String, Map<String, Object>> ricConfiguration =  null;
        Map<String, Map<String, Integer>> exp = new HashMap<>();
        Map<String, Integer> ric1 = new HashMap<>();
        Map<String, Integer> ric2 = new HashMap<>();
        ric1.put("dLThptPerSlice", 50);
        ric2.put("dLThptPerSlice", 50);
        ric2.put("uLThptPerSlice", 30);
        exp.put("1", ric1);
        exp.put("2", ric2);    
        try { 
            ricConfiguration = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/ricConfiguration.json"))), new TypeReference<Map<String, Map<String, Object>>>(){});
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
