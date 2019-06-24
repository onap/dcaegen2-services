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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

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

    private final static String KIBANA_DASHBOARD_IMPORT = "KibanaDashboardImport";

    private final static String ELASTICSEARCH_MAPPING_TEMPLATE = "ElasticsearchMappingTemplate";

    public static boolean sendPostHttpClient(String url, String json, String postFlag){

        boolean flag = true;
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        String response = null;
        try {
            StringEntity s = new StringEntity(json);
            s.setContentEncoding("UTF-8");
            s.setContentType("application/json");
            post.setEntity(s);
            post.setHeader("kbn-xsrf","true");
            post.setHeader("Accept", "*/*");
            post.setHeader("Connection", "Keep-Alive");
            HttpResponse res = client.execute(post);
            if(res.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                HttpEntity entity = res.getEntity();
                String result = EntityUtils.toString(res.getEntity());
                response = result;
            }
            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<>();
            map = gson.fromJson(response, map.getClass());
            switch (postFlag) {
                case KIBANA_DASHBOARD_IMPORT:
                    flag = flagOfKibanaDashboardImport(map);
                    break;
                case ELASTICSEARCH_MAPPING_TEMPLATE :
                    flag = flagOfPostEsMappingTemplate(map);
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return flag;
    }


    private static boolean flagOfKibanaDashboardImport(Map<String, Object> map) {

        boolean flag = true;
        List objectsList = (List) map.get("objects");

        if (objectsList != null && objectsList.size() > 0) {
            Map<String, Object> map2 = new HashMap<>();
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
            if ("acknowledged".equals(key) && (boolean) map.get("acknowledged") == true) {
                break;
            } else {
                flag = false;
            }
        }
        return flag;
    }

}
