/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Wipro Limited.
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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.slice.analysis.ms.exception.DesClientException;
import org.onap.slice.analysis.ms.models.AggregatedConfig;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.SliceConfigRequest;
import org.onap.slice.analysis.ms.models.SliceConfigResponse;
import org.onap.slice.analysis.ms.restclients.CpsRestClient;
import org.onap.slice.analysis.ms.restclients.DesRestClient;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({SliceUtilization.class, Configuration.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@SpringBootTest(classes = SliceUtilizationTest.class)
public class SliceUtilizationTest {
    ObjectMapper objectMapper = new ObjectMapper();

    Configuration configuration = Configuration.getInstance();

    @InjectMocks
    SliceUtilization sliceUtilization;

    @Mock
    CpsRestClient cpsRestClient;

    @Mock
    DesRestClient desRestClient;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getSnssaiListTest() throws Exception {
        configuration.setCpsUrl("http://cps:8080/execute/cps-schemaset");
        configuration.setRannfnssiDetailsTemplateId("get-rannfnssi-details");

        PowerMockito.mockStatic(SliceUtilization.class);
        PowerMockito.mockStatic(Configuration.class);
        PowerMockito.when(Configuration.getInstance()).thenReturn(configuration);
        String cpsUrl = configuration.getCpsUrl();
        String rannfnssiDetailsTemplateId = configuration.getRannfnssiDetailsTemplateId();
        Map<String, String> inputParameters1 = new HashMap<>();
        inputParameters1.put("rannfnssiid", "14559ead-f4fe-4c1c-a94c-8015fad3ea35");
        Map<String, Map<String, String>> requestBody1 = new HashMap<>();
        requestBody1.put("inputParameters", inputParameters1);
        String jsonRequestBody1 = objectMapper.writeValueAsString(requestBody1);
        Map<String, String> inputParameters2 = new HashMap<>();
        inputParameters2.put("rannfnssiid", "14559ead-f4fe-4c1c-a94c-8015fad3ea36");
        Map<String, Map<String, String>> requestBody2 = new HashMap<>();
        requestBody2.put("inputParameters", inputParameters2);
        String jsonRequestBody2 = objectMapper.writeValueAsString(requestBody2);

        try {
            String rannfnssiDetails1 =
                    new String(Files.readAllBytes(Paths.get("src/test/resources/rannfNssiDetails1.json")));
            Mockito.when(cpsRestClient.sendPostRequest(cpsUrl + "/" + rannfnssiDetailsTemplateId, jsonRequestBody1,
                    new ParameterizedTypeReference<String>() {}))
                    .thenReturn(new ResponseEntity<String>(rannfnssiDetails1, HttpStatus.OK));
            String rannfnssiDetails2 =
                    new String(Files.readAllBytes(Paths.get("src/test/resources/rannfNssiDetails2.json")));
            Mockito.when(cpsRestClient.sendPostRequest(cpsUrl + "/" + rannfnssiDetailsTemplateId, jsonRequestBody2,
                    new ParameterizedTypeReference<String>() {}))
                    .thenReturn(new ResponseEntity<String>(rannfnssiDetails2, HttpStatus.OK));

        } catch (Exception e) {
            e.printStackTrace();

        }
        List<String> actualResponse;
        List<String> expectedResponse = new ArrayList<>();
        expectedResponse.add("001-11000");
        expectedResponse.add("001-11001");
        expectedResponse.add("001-11002");
        actualResponse = sliceUtilization.getSnssaiList("14559ead-f4fe-4c1c-a94c-8015fad3ea35");
        expectedResponse.clear();
        expectedResponse.add("001-11003");
        expectedResponse.add("001-11004");
        expectedResponse.add("001-11005");
        actualResponse = sliceUtilization.getSnssaiList("14559ead-f4fe-4c1c-a94c-8015fad3ea36");
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void getPmDataTest() throws Exception {
        configuration.setDesUrl("http://des:1681/datalake/v1/exposure/pm_data");
        configuration.setPmDataDurationInWeeks(4);
        PowerMockito.mockStatic(SliceUtilization.class);
        PowerMockito.mockStatic(Configuration.class);
        PowerMockito.when(Configuration.getInstance()).thenReturn(configuration);
        String pmData = null;
        try {
            pmData = new String(Files.readAllBytes(Paths.get("src/test/resources/pm_data.json")));
        } catch (Exception e) {
            e.printStackTrace();

        }
        Mockito.when(desRestClient.sendPostRequest(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new ResponseEntity<>(pmData, HttpStatus.OK));
        JSONObject actualResponse = sliceUtilization.getPMData("001-1100");
        assertTrue(actualResponse.has("result"));

    }

    @Test
    public void calculateSliceUtilizationTest() throws Exception {

        PowerMockito.mockStatic(SliceUtilization.class);
        List<JSONObject> pmDataList = new ArrayList<>();
        String pmData;
        try {
            pmData = new String(Files.readAllBytes(Paths.get("src/test/resources/pm_data.json")));
            JSONObject pmDataObj = new JSONObject(pmData);
            pmDataList.add(pmDataObj);
            pmDataList.add(pmDataObj);
            pmDataList.add(pmDataObj);
        } catch (Exception e) {
            e.printStackTrace();

        }
        AggregatedConfig actualResponse = sliceUtilization.calculateSliceUtilization(pmDataList);
        assertSame(16, actualResponse.getDLThptPerSlice());
    }

    @Test
    public void getSliceUtilizationDataTest() throws Exception {

        PowerMockito.mockStatic(SliceUtilization.class);
        SliceConfigRequest sliceConfigRequest = new SliceConfigRequest();
        List<String> sliceIdentifiersList = new ArrayList<>();
        sliceIdentifiersList.add("14559ead-f4fe-4c1c-a94c-8015fad3ea35");
        sliceIdentifiersList.add("14559ead-f4fe-4c1c-a94c-8015fad3ea36");
        sliceConfigRequest.setSliceIdentifiers(sliceIdentifiersList);
        List<String> configParamsList = new ArrayList<>();
        configParamsList.add("dLThptPerSlice");
        configParamsList.add("uLThptPerSlice");
        sliceConfigRequest.setConfigParams(configParamsList);

        configuration.setCpsUrl("http://cps:8080/execute/cps-schemaset");
        configuration.setRannfnssiDetailsTemplateId("get-rannfnssi-details");
        configuration.setDesUrl("http://des:1681/datalake/v1/exposure/pm_data");
        configuration.setPmDataDurationInWeeks(4);

        PowerMockito.mockStatic(SliceUtilization.class);
        PowerMockito.mockStatic(Configuration.class);
        PowerMockito.when(Configuration.getInstance()).thenReturn(configuration);
        String cpsUrl = configuration.getCpsUrl();
        String rannfnssiDetailsTemplateId = configuration.getRannfnssiDetailsTemplateId();
        Map<String, String> inputParameters1 = new HashMap<>();
        inputParameters1.put("rannfnssiid", "14559ead-f4fe-4c1c-a94c-8015fad3ea35");
        Map<String, Map<String, String>> requestBody1 = new HashMap<>();
        requestBody1.put("inputParameters", inputParameters1);
        String rannfnssiRequestBody1 = objectMapper.writeValueAsString(requestBody1);
        Map<String, String> inputParameters2 = new HashMap<>();
        inputParameters2.put("rannfnssiid", "14559ead-f4fe-4c1c-a94c-8015fad3ea36");
        Map<String, Map<String, String>> requestBody2 = new HashMap<>();
        requestBody2.put("inputParameters", inputParameters2);
        String rannfnssiRequestBody2 = objectMapper.writeValueAsString(requestBody2);

        try {
            String rannfnssiDetails1 =
                    new String(Files.readAllBytes(Paths.get("src/test/resources/rannfNssiDetails1.json")));
            Mockito.when(cpsRestClient.sendPostRequest(cpsUrl + "/" + rannfnssiDetailsTemplateId, rannfnssiRequestBody1,
                    new ParameterizedTypeReference<String>() {}))
                    .thenReturn(new ResponseEntity<String>(rannfnssiDetails1, HttpStatus.OK));
            String rannfnssiDetails2 =
                    new String(Files.readAllBytes(Paths.get("src/test/resources/rannfNssiDetails2.json")));
            Mockito.when(cpsRestClient.sendPostRequest(cpsUrl + "/" + rannfnssiDetailsTemplateId, rannfnssiRequestBody2,
                    new ParameterizedTypeReference<String>() {}))
                    .thenReturn(new ResponseEntity<String>(rannfnssiDetails2, HttpStatus.OK));

            String pmData = new String(Files.readAllBytes(Paths.get("src/test/resources/pm_data.json")));
            Mockito.when(desRestClient.sendPostRequest(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ResponseEntity<>(pmData, HttpStatus.OK));
        } catch (Exception e) {
            e.printStackTrace();

        }
        SliceConfigResponse actualResponse = sliceUtilization.getSliceUtilizationData(sliceConfigRequest);
        String actualResponseString = objectMapper.writeValueAsString(actualResponse);
        SliceConfigResponse sliceConfigResponse = objectMapper
                .readValue(new File("src/test/resources/sliceConfigResponse.json"), SliceConfigResponse.class);
        assertEquals(objectMapper.writeValueAsString(sliceConfigResponse), actualResponseString);
    }
}
