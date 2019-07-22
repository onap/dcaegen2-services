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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test HtpClient
 *
 * @author guochunmeng
 */

public class HttpClientUtilTest {

    @Mock
    private HttpClient  httpClient;

    @Mock
    private HttpPost httpPost;

    @Mock
    private HttpResponse httpResponse;

    @Test(expected = RuntimeException.class)
    public void testSendPostHttpClient() {

        String testUrl = "http://localhost:5601/api/kibana/dashboards/import?exclude=index-pattern";
        String testJson = "{\n" +
                "  \"objects\": [\n" +
                "    {\n" +
                "      \"id\": \"80b956f0-b2cd-11e8-ad8e-85441f0c2e5c\",\n" +
                "      \"type\": \"visualization\",\n" +
                "      \"updated_at\": \"2018-09-07T18:40:33.247Z\",\n" +
                "      \"version\": 1,\n" +
                "      \"attributes\": {\n" +
                "        \"title\": \"Count Example\",\n" +
                "        \"visState\": \"{\\\"title\\\":\\\"Count Example\\\",\\\"type\\\":\\\"metric\\\",\\\"params\\\":{\\\"addTooltip\\\":true,\\\"addLegend\\\":false,\\\"type\\\":\\\"metric\\\",\\\"metric\\\":{\\\"percentageMode\\\":false,\\\"useRanges\\\":false,\\\"colorSchema\\\":\\\"Green to Red\\\",\\\"metricColorMode\\\":\\\"None\\\",\\\"colorsRange\\\":[{\\\"from\\\":0,\\\"to\\\":10000}],\\\"labels\\\":{\\\"show\\\":true},\\\"invertColors\\\":false,\\\"style\\\":{\\\"bgFill\\\":\\\"#000\\\",\\\"bgColor\\\":false,\\\"labelColor\\\":false,\\\"subText\\\":\\\"\\\",\\\"fontSize\\\":60}}},\\\"aggs\\\":[{\\\"id\\\":\\\"1\\\",\\\"enabled\\\":true,\\\"type\\\":\\\"count\\\",\\\"schema\\\":\\\"metric\\\",\\\"params\\\":{}}]}\",\n" +
                "        \"uiStateJSON\": \"{}\",\n" +
                "        \"description\": \"\",\n" +
                "        \"version\": 1,\n" +
                "        \"kibanaSavedObjectMeta\": {\n" +
                "          \"searchSourceJSON\": \"{\\\"index\\\":\\\"90943e30-9a47-11e8-b64d-95841ca0b247\\\",\\\"query\\\":{\\\"query\\\":\\\"\\\",\\\"language\\\":\\\"lucene\\\"},\\\"filter\\\":[]}\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"90943e30-9a47-11e8-b64d-95841ca0b247\",\n" +
                "      \"type\": \"index-pattern\",\n" +
                "      \"updated_at\": \"2018-09-07T18:39:47.683Z\",\n" +
                "      \"version\": 1,\n" +
                "      \"attributes\": {\n" +
                "        \"title\": \"kibana_sample_data_logs\",\n" +
                "        \"timeFieldName\": \"timestamp\",\n" +
                "        \"fields\": \"<truncated for example>\",\n" +
                "        \"fieldFormatMap\": \"{\\\"hour_of_day\\\":{}}\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"942dcef0-b2cd-11e8-ad8e-85441f0c2e5c\",\n" +
                "      \"type\": \"dashboard\",\n" +
                "      \"updated_at\": \"2018-09-07T18:41:05.887Z\",\n" +
                "      \"version\": 1,\n" +
                "      \"attributes\": {\n" +
                "        \"title\": \"Example Dashboard\",\n" +
                "        \"hits\": 0,\n" +
                "        \"description\": \"\",\n" +
                "        \"panelsJSON\": \"[{\\\"gridData\\\":{\\\"w\\\":24,\\\"h\\\":15,\\\"x\\\":0,\\\"y\\\":0,\\\"i\\\":\\\"1\\\"},\\\"version\\\":\\\"7.0.0-alpha1\\\",\\\"panelIndex\\\":\\\"1\\\",\\\"type\\\":\\\"visualization\\\",\\\"id\\\":\\\"80b956f0-b2cd-11e8-ad8e-85441f0c2e5c\\\",\\\"embeddableConfig\\\":{}}]\",\n" +
                "        \"optionsJSON\": \"{\\\"darkTheme\\\":false,\\\"useMargins\\\":true,\\\"hidePanelTitles\\\":false}\",\n" +
                "        \"version\": 1,\n" +
                "        \"timeRestore\": false,\n" +
                "        \"kibanaSavedObjectMeta\": {\n" +
                "          \"searchSourceJSON\": \"{\\\"query\\\":{\\\"query\\\":\\\"\\\",\\\"language\\\":\\\"lucene\\\"},\\\"filter\\\":[]}\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        String testFlag = "KibanaDashboardImport";
        try {
            when(httpClient.execute(httpPost)).thenReturn(httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, HttpClientUtil.sendPostHttpClient(testUrl, testJson, testFlag));

    }
}