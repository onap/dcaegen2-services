/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2019 China Mobile
 *=================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.datalake.feeder.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.when;

import static org.junit.Assert.*;

/**
 * Test HtpClient
 *
 * @author guochunmeng
 */
public class HttpClientUtilTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpEntity httpEntity;

    @Mock
    private ResponseEntity responseEntity;

//    @Before
//    public void before() {
//        responseEntity = restTemplate.postForEntity("", httpEntity, String.class);
//    }

    @Test
    public void testSendHttpClientPost() {

        String templateName = "unauthenticated.test";
        String testUrl = "http://localhost:9200/_template/"+templateName;
        String testJson = "{\n" +
                "\t\"template\":\"unauthenticated.test\",\n" +
                "\t\"order\":1,\n" +
                "\t\"mappings\":{\n" +
                "\t\t\"_default_\":{\n" +
                "\t\t\t\"properties\":{\n" +
                "\t\t\t\t\"datalake_ts_\":{\n" +
                "\t\t\t\t\t\"type\":\"date\",\n" +
                "\t\t\t\t\t\"format\":\"epoch_millis\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"event.commonEventHeader.startEpochMicrosec\":{\n" +
                "\t\t\t\t\t\"type\":\"date\",\n" +
                "\t\t\t\t\t\"format\":\"epoch_millis\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"event.commonEventHeader.lastEpochMicrosec\":{\n" +
                "\t\t\t\t\t\"type\":\"date\",\n" +
                "\t\t\t\t\t\"format\":\"epoch_millis\"\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
        String testFlag = "ElasticsearchMappingTemplate";
        String testUrlFlag = "Elasticsearch";
//        when(restTemplate.postForEntity(testUrl, httpEntity, String.class)).thenReturn(responseEntity);
//        when(responseEntity.getStatusCodeValue()).thenReturn(200);
//        when(responseEntity.getBody()).thenReturn("{ \"acknowledged\": true }");

        assertEquals(false, HttpClientUtil.sendHttpClientPost(testUrl, testJson, testFlag, testUrlFlag));
    }
}