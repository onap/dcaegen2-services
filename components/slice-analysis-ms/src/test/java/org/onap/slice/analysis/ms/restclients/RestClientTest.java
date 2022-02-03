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

package org.onap.slice.analysis.ms.restclients;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.slice.analysis.ms.service.SnssaiSamplesProcessorTest;
import org.onap.slice.analysis.ms.utils.BeanUtil;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
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
@SpringBootTest(classes = RestClientTest.class)
public class RestClientTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    RestClient restclient;

    @SuppressWarnings({"static-access"})
    @Test
    public void sendGetRequestTest() {
        String requestUrl = "";
        ParameterizedTypeReference<String> responseType = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        when(restTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, responseType))
                .thenReturn(new ResponseEntity(HttpStatus.NOT_FOUND));
        assertEquals(restclient.sendGetRequest(headers, requestUrl, responseType).getStatusCode(),
                HttpStatus.NOT_FOUND);
    }

    @SuppressWarnings({"static-access", "unchecked", "rawtypes"})
    @Test
    public void sendPostRequestTest() {
        ParameterizedTypeReference<String> responseType = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestUrl = "Url";
        String requestBody = null;
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
        when(restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, responseType))
                .thenReturn(new ResponseEntity(HttpStatus.OK));
        ResponseEntity<String> resp = restclient.sendPostRequest(headers, requestUrl, requestBody, responseType);
        assertEquals(resp.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void sendPostRequestTest2() {
        ParameterizedTypeReference<String> responseType = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestUrl = "Url";
        String requestBody = null;
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
        when(restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, responseType))
                .thenReturn(new ResponseEntity(HttpStatus.NOT_FOUND));
        ResponseEntity<String> resp = restclient.sendPostRequest(headers, requestUrl, requestBody, responseType);
        assertEquals(resp.getStatusCode(), HttpStatus.NOT_FOUND);
    }

}
