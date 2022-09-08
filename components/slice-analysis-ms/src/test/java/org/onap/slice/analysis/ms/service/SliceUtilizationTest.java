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
import static org.junit.Assert.assertTrue;

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
import org.onap.slice.analysis.ms.aai.AaiService;
import org.onap.slice.analysis.ms.models.AggregatedConfig;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.SliceConfigDetails;
import org.onap.slice.analysis.ms.models.SliceConfigRequest;
import org.onap.slice.analysis.ms.models.SliceConfigResponse;
import org.onap.slice.analysis.ms.restclients.AaiRestClient;
import org.onap.slice.analysis.ms.restclients.DesRestClient;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ SliceUtilization.class, Configuration.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*" })
@SpringBootTest(classes = SliceUtilizationTest.class)
public class SliceUtilizationTest {

	ObjectMapper objectMapper = new ObjectMapper();

	Configuration configuration = Configuration.getInstance();

	@InjectMocks
	PmDataQueue pmDataQueue;

	@InjectMocks
	SliceUtilization sliceUtilization;
	
	@InjectMocks
	AaiRestClient aaiRestClient;

	@Mock
	AaiService aaiService;

	@Mock
	DesRestClient desRestClient;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
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
		assertEquals(190857028, (int) actualResponse.getDLThptPerSlice());
		assertEquals(119285978, (int) actualResponse.getULThptPerSlice());
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
		List<String> snssaiList = new ArrayList<>();
		snssaiList.add("01-06E442");
		snssaiList.add("01-B989BD");
		configuration.setDesUrl("http://des:1681/datalake/v1/exposure/pm_data");
		configuration.setPmDataDurationInWeeks(4);

		PowerMockito.mockStatic(SliceUtilization.class);
		PowerMockito.mockStatic(Configuration.class);
		PowerMockito.when(Configuration.getInstance()).thenReturn(configuration);

		try {

			Mockito.when(aaiService.getSnssaiList(Mockito.any())).thenReturn(snssaiList);

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

	@Test
	public void AggregatedConfigcalculateSliceUtilization() {

		AggregatedConfig aggregatedConfig = new AggregatedConfig();

		SliceConfigDetails sliceConfigDetails = new SliceConfigDetails();
		sliceConfigDetails.setSliceIdentifiers("slice");
		sliceConfigDetails.setAggregatedConfig(aggregatedConfig);

		assertEquals("slice", sliceConfigDetails.getSliceIdentifiers());
		assertEquals(aggregatedConfig, sliceConfigDetails.getAggregatedConfig());

	}
	@Test
	public void fetchCurrentConfigurationOfSlice() throws Exception{
		configuration.setAaiUrl("http://aai:30233/aai/v21/business/customers/customer/");
		Map<String, Integer> responsemap = new HashMap<>();
		try {
			String serviceInstance = new String(
					Files.readAllBytes(Paths.get("src/test/resources/aaiDetailsList.json")));
			Mockito.when(aaiRestClient.sendGetRequest(Mockito.anyString(), Mockito.any()))
					.thenReturn(new ResponseEntity<Object>(serviceInstance, HttpStatus.OK));
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(responsemap, aaiService.fetchCurrentConfigurationOfSlice("001-010000"));
	}
	
}
