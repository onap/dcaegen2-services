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
import org.onap.slice.analysis.ms.cps.CpsInterface;
import org.onap.slice.analysis.ms.cps.CpsService;
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
@PrepareForTest({CpsService.class, Configuration.class,CpsInterface.class, CpsRestClient.class})
@SpringBootTest(classes = CpsRestClientTest.class)
public class CpsRestClientTest {
	Configuration configuration = Configuration.getInstance();
	@Mock
	RestTemplate restTemplate;

	@InjectMocks
	CpsService cpsService;

	@InjectMocks
	CpsRestClient cpsRestClient;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testSendPostRequestStringStringParameterizedTypeReferenceOfT() {
		ParameterizedTypeReference<String> responseType = ParameterizedTypeReference.forType(getClass());;
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		String requestUrl = "Url";
		String requestBody = null;
		HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
		when(restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, responseType))
				.thenReturn(new ResponseEntity(HttpStatus.OK));
		ResponseEntity<String> resp = cpsRestClient.sendPostRequest(headers, requestUrl, requestBody, responseType);
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testSendGetRequestStringParameterizedTypeReferenceOfT() {
		String requestUrl = "";
		ParameterizedTypeReference<String> responseType = ParameterizedTypeReference.forType(getClass());;
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
		when(restTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, responseType))
				.thenReturn(new ResponseEntity(HttpStatus.NOT_FOUND));
		assertEquals(cpsRestClient.sendGetRequest(headers, requestUrl, responseType).getStatusCode(),
				HttpStatus.NOT_FOUND);
	}
	
    @Test
    public void fetchCurrentConfigurationOfRICTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("dLThptPerSlice", 10);
        map.put("uLThptPerSlice", 10);
        map.put("maxNumberOfConns", 10);
        Map<String, Map<String, Object>> responseMap = new HashMap<>();
        try {
            String serviceInstance = new String(Files.readAllBytes(Paths.get("src/test/resources/sliceConfig.json")));
            Mockito.when(cpsRestClient.sendPostRequest(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(new ResponseEntity<>(serviceInstance, HttpStatus.OK));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(responseMap, cpsService.fetchCurrentConfigurationOfRIC("111-1111"));
    }
    @Test
    public void fetchNetworkFunctionsOfSnssaiTest() {
        List<String> responseList = new ArrayList<>();
        try {
            String serviceInstance = new String(Files.readAllBytes(Paths.get("src/test/resources/DUList.json")));
            Mockito.when(cpsRestClient.sendPostRequest(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(new ResponseEntity<>(serviceInstance, HttpStatus.OK));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(responseList, cpsService.fetchNetworkFunctionsOfSnssai("111-1111"));
    }
    @Test
    public void fetchRICsOfSnssaiTest() {
        Map<String, List<String>> responseMap = new HashMap<>();
        List<String> cellslist = new ArrayList<>();
        cellslist.add("1599");
        cellslist.add("1598");
        try {
            String serviceInstance = new String(Files.readAllBytes(Paths.get("src/test/resources/DUCellsList.json")));
            Mockito.when(cpsRestClient.sendPostRequest(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(new ResponseEntity<>(serviceInstance, HttpStatus.OK));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(responseMap, cpsService.fetchRICsOfSnssai("111-1111"));
    }
    @Test
    public void  fetchnrCellCUsOfSnssaiTest() {
        Map<String, List<String>> responseMap = new HashMap<>();
        List<String> cellslist = new ArrayList<>();
        cellslist.add("15199");
        try {
            String serviceInstance = new String(Files.readAllBytes(Paths.get("src/test/resources/DUCellsList.json")));
            Mockito.when(cpsRestClient.sendPostRequest(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(new ResponseEntity<>(serviceInstance, HttpStatus.OK));
        } catch (Exception e) {
            e.printStackTrace();
        }
       assertEquals(responseMap, cpsService.fetchnrCellCUsOfSnssai("111-1111"));
    }


}
