/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020-2022 Wipro Limited.
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

package org.onap.slice.analysis.ms.restclients;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.slice.analysis.ms.aai.AaiInterface;
import org.onap.slice.analysis.ms.aai.AaiService;
import org.onap.slice.analysis.ms.models.Configuration;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@PrepareForTest({ AaiService.class, Configuration.class, AaiInterface.class, AaiRestClient.class })
@SpringBootTest(classes = AaiRestClientTest.class)
public class AaiRestClientTest {

	Configuration configuration = Configuration.getInstance();

	@Mock
	RestTemplate restTemplate;

	@InjectMocks
	AaiService aaiService;

	@InjectMocks
	AaiRestClient aaiRestClient;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSendPostRequestStringStringParameterizedTypeReferenceOfT() throws Exception {
		ParameterizedTypeReference<String> responseType = ParameterizedTypeReference.forType(getClass());
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		String requestUrl = "";
		String requestBody = "RequestBody";
		HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
		when(restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, responseType))
				.thenReturn(new ResponseEntity(HttpStatus.NOT_FOUND));
		ResponseEntity<String> resp = aaiRestClient.sendPostRequest(headers, requestUrl, requestBody, responseType);
		assertEquals(resp.getStatusCode(), HttpStatus.NOT_FOUND);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSendGetRequestStringParameterizedTypeReferenceOfT() throws Exception {
		String requestUrl = "http://aai:30233/aai/v21/business/customers/customer/";
		ParameterizedTypeReference<String> responseType = ParameterizedTypeReference.forType(getClass());
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
		when(restTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, responseType))
				.thenReturn(new ResponseEntity(HttpStatus.NOT_FOUND));
		assertEquals(aaiRestClient.sendGetRequest(headers, requestUrl, responseType).getStatusCode(),
				HttpStatus.NOT_FOUND);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void sendPostRequestTest() throws Exception {
		ParameterizedTypeReference<String> responseType = ParameterizedTypeReference.forType(getClass());
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		String requestUrl = "http://aai:30233/aai/v21/business/customers/customer/";
		String requestBody = "RequestBody";
		HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
		when(restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, responseType))
				.thenReturn(new ResponseEntity(HttpStatus.OK));
		ResponseEntity<String> resp = aaiRestClient.sendPostRequest(headers, requestUrl, requestBody, responseType);
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
	}

	@Test
	public void fetchCurrentConfigurationOfSlice() {
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
	
	@Test
	public void fetchServiceProfile() {
		Map<String, String> responseMap = new HashMap<String, String>();
		responseMap.put("sNSSAI", "001-00110");
		try {
			String serviceInstance = new String(
					Files.readAllBytes(Paths.get("src/test/resources/aaiDetailsList.json")));
			Mockito.when(aaiRestClient.sendGetRequest(Mockito.anyString(), Mockito.any()))
					.thenReturn(new ResponseEntity<Object>(serviceInstance, HttpStatus.OK));
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(responseMap, aaiService.fetchServiceDetails("001-00110"));
	}

	@Test
	public void fetchSliceProfilesOfAllotedResourceDataTest() throws Exception {
		configuration.setAaiUrl("http://aai:30233/aai/v21");
		List<String> allotedResourceList = new ArrayList<>();
		allotedResourceList.add("530d188d-9087-49af-a44a-90c40e0c2d47");
		List<String> expectedResponse = new ArrayList<>();
		expectedResponse.add("b2ae730f-1d5f-495a-8112-dac017a7348c");
		expectedResponse.add("cad8fa36-2d55-4c12-a92e-1bd551517a0c");
		expectedResponse.add("8d0d698e-77f4-4453-8c09-ae2cbe6a9a04");

		try {

			String serviceInstance = new String(
					Files.readAllBytes(Paths.get("src/test/resources/alloted-resource.json")));
			Mockito.when(aaiRestClient.sendGetRequest(Mockito.anyString(), Mockito.any()))
					.thenReturn(new ResponseEntity<Object>(serviceInstance, HttpStatus.OK));

		} catch (Exception e) {
			e.printStackTrace();

		}
		List<String> actualResponse = aaiService.fetchSliceProfilesOfAllotedResourceData(allotedResourceList);
		actualResponse.add("b2ae730f-1d5f-495a-8112-dac017a7348c");
		actualResponse.add("cad8fa36-2d55-4c12-a92e-1bd551517a0c");
		actualResponse.add("8d0d698e-77f4-4453-8c09-ae2cbe6a9a04");
		assertEquals(expectedResponse, actualResponse);
	}

	@Test
	public void fetchSnssaiOfSliceProfileTest() throws Exception {
		configuration.setAaiUrl("http://aai:30233/aai/v21");
		List<String> sliceProfileList = new ArrayList<>();
		sliceProfileList.add("b2ae730f-1d5f-495a-8112-dac017a7348c");
		sliceProfileList.add("cad8fa36-2d55-4c12-a92e-1bd551517a0c");
		sliceProfileList.add("8d0d698e-77f4-4453-8c09-ae2cbe6a9a04");
		List<String> expectedResponse = new ArrayList<>();
		expectedResponse.add("01-06E442");
		expectedResponse.add("01-B989BD");

		String serviceInstanceUrlAn = "b2ae730f-1d5f-495a-8112-dac017a7348c";
		String serviceInstanceUrlCn = "cad8fa36-2d55-4c12-a92e-1bd551517a0c";
		String serviceInstanceUrlTn = "8d0d698e-77f4-4453-8c09-ae2cbe6a9a04";

		try {

			String serviceInstanceAn = new String(
					Files.readAllBytes(Paths.get("src/test/resources/sliceprofile_an_sa1.json")));
			Mockito.when(aaiRestClient.sendGetRequest(Mockito.contains(serviceInstanceUrlAn), Mockito.any()))
					.thenReturn(new ResponseEntity<Object>(serviceInstanceAn, HttpStatus.OK));

			String serviceInstanceCn = new String(
					Files.readAllBytes(Paths.get("src/test/resources/sliceprofile_cn_sa1.json")));
			Mockito.when(aaiRestClient.sendGetRequest(Mockito.contains(serviceInstanceUrlCn), Mockito.any()))
					.thenReturn(new ResponseEntity<Object>(serviceInstanceCn, HttpStatus.OK));

			String serviceInstanceTn = new String(
					Files.readAllBytes(Paths.get("src/test/resources/sliceprofile_tn_sa1.json")));
			Mockito.when(aaiRestClient.sendGetRequest(Mockito.contains(serviceInstanceUrlTn), Mockito.any()))
					.thenReturn(new ResponseEntity<Object>(serviceInstanceTn, HttpStatus.OK));

		} catch (Exception e) {
			e.printStackTrace();

		}
		List<String> actualResponse = aaiService.fetchSnssaiOfSliceProfile(sliceProfileList);
		actualResponse.add("01-06E442");
		actualResponse.add("01-B989BD");
		assertEquals(expectedResponse, actualResponse);
	}

	@Test
	public void getSnssaiListForNsiTest() throws Exception {
		configuration.setAaiUrl("http://aai:30233/aai/v21");
		List<String> expectedResponse = new ArrayList<>();
		expectedResponse.add("01-06E442");
		expectedResponse.add("01-B989BD");

		try {

			String allotedResource = new String(
					Files.readAllBytes(Paths.get("src/test/resources/alloted-resource.json")));
			Mockito.when(aaiRestClient.sendGetRequest(Mockito.contains("0835fd19-6726-4081-befb-cc8932c47767"),
					Mockito.any())).thenReturn(new ResponseEntity<Object>(allotedResource, HttpStatus.OK));

			String serviceInstance = new String(Files.readAllBytes(Paths.get("src/test/resources/nsi.json")));
			Mockito.when(aaiRestClient.sendGetRequest(Mockito.contains("09cad94e-fbb8-4c70-9c4d-74ec75e97683"),
					Mockito.any())).thenReturn(new ResponseEntity<Object>(serviceInstance, HttpStatus.OK));

			String serviceInstanceAn = new String(
					Files.readAllBytes(Paths.get("src/test/resources/sliceprofile_an_sa1.json")));
			Mockito.when(aaiRestClient.sendGetRequest(Mockito.contains("b2ae730f-1d5f-495a-8112-dac017a7348c"),
					Mockito.any())).thenReturn(new ResponseEntity<Object>(serviceInstanceAn, HttpStatus.OK));

			String serviceInstanceCn = new String(
					Files.readAllBytes(Paths.get("src/test/resources/sliceprofile_cn_sa1.json")));
			Mockito.when(aaiRestClient.sendGetRequest(Mockito.contains("cad8fa36-2d55-4c12-a92e-1bd551517a0c"),
					Mockito.any())).thenReturn(new ResponseEntity<Object>(serviceInstanceCn, HttpStatus.OK));

			String serviceInstanceTn = new String(
					Files.readAllBytes(Paths.get("src/test/resources/sliceprofile_tn_sa1.json")));
			Mockito.when(aaiRestClient.sendGetRequest(Mockito.contains("8d0d698e-77f4-4453-8c09-ae2cbe6a9a04"),
					Mockito.any())).thenReturn(new ResponseEntity<Object>(serviceInstanceTn, HttpStatus.OK));

		} catch (Exception e) {
			e.printStackTrace();

		}
		List<String> actualResponse = aaiService.getSnssaiList("0835fd19-6726-4081-befb-cc8932c47767");
		actualResponse.add("01-06E442");
		actualResponse.add("01-B989BD");
		assertEquals(expectedResponse, actualResponse);
	}

	@Test
	public void getSnssaiListForNssiTest() throws Exception {
		configuration.setAaiUrl("http://aai:30233/aai/v21");
		List<String> expectedResponse = new ArrayList<>();
		expectedResponse.add("01-06E442");

		try {

			String nssi = new String(Files.readAllBytes(Paths.get("src/test/resources/nssi.json")));
			Mockito.when(aaiRestClient.sendGetRequest(Mockito.contains("50f418a6-804f-4453-bf70-21f0efaf6fcd"),
					Mockito.any())).thenReturn(new ResponseEntity<Object>(nssi, HttpStatus.OK));

			String serviceInstanceAn = new String(
					Files.readAllBytes(Paths.get("src/test/resources/sliceprofile_an_sa1.json")));
			Mockito.when(aaiRestClient.sendGetRequest(Mockito.contains("b2ae730f-1d5f-495a-8112-dac017a7348c"),
					Mockito.any())).thenReturn(new ResponseEntity<Object>(serviceInstanceAn, HttpStatus.OK));

		} catch (Exception e) {
			e.printStackTrace();

		}
		List<String> actualResponse = aaiService.getSnssaiList("50f418a6-804f-4453-bf70-21f0efaf6fcd");
		actualResponse.add("01-06E442");
		assertEquals(expectedResponse, actualResponse);

	}
	

}
