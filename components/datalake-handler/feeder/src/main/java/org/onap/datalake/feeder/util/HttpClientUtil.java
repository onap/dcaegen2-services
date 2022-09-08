/*
 * ============LICENSE_START=======================================================
 * ONAP : DCAE
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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HttpClient
 *
 * @author guochunmeng
 *
 */
public class HttpClientUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);

    private static final String KIBANA = "Kibana";

    private static final String KIBANA_DASHBOARD_IMPORT = "KibanaDashboardImport";

    private static final String ELASTICSEARCH_MAPPING_TEMPLATE = "ElasticsearchMappingTemplate";

    private HttpClientUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean sendHttpClientPost(String url, String json, String postFlag, String urlFlag) {
        boolean flag = false;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        if (urlFlag.equals(KIBANA)) {
            log.info("urlFlag is Kibana, add header");
            headers.add("kbn-xsrf","true");
        }
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.postForEntity(url, request, String.class);
            if (responseEntity.getStatusCodeValue() != 200)
                throw new RestClientException("Resquest failed");
            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<>();
            map = gson.fromJson(responseEntity.getBody(), map.getClass());
            switch (postFlag) {
                case KIBANA_DASHBOARD_IMPORT:
                    flag = flagOfKibanaDashboardImport(map);
                    break;
                case ELASTICSEARCH_MAPPING_TEMPLATE :
                    flag = flagOfPostEsMappingTemplate(map);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.debug("Resquest failed: " + e.getMessage());
        }
        return flag;
    }

    private static boolean flagOfKibanaDashboardImport(Map<String, Object> map) {

        boolean flag = true;
        List objectsList = (List) map.get("objects");

        if (!objectsList.isEmpty()) {
            Map<String, Object> map2 = null;
            for (int i = 0; i < objectsList.size(); i++){
                map2 = (Map<String, Object>)objectsList.get(i);
                for(String key : map2.keySet()){
                    if ("error".equals(key)) {
                        return false;
                    }
                }
            }
        }
        return flag;
    }

    private static boolean flagOfPostEsMappingTemplate(Map<String, Object> map) {

        boolean flag = true;
        for(String key : map.keySet()){
            if ("acknowledged".equals(key) && (boolean) map.get("acknowledged")) {
                break;
            } else {
                flag = false;
            }
        }
        return flag;
    }
}
