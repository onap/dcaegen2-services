/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 China Mobile.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.kpi.dmaap;

import static org.mockito.Mockito.when;

import com.att.nsa.cambria.client.CambriaTopicManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.dcaegen2.kpi.models.Configuration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DmaapClientTest.class)
public class DmaapClientTest {

    @Mock
    private CambriaTopicManager topicManager;

    @InjectMocks
    DmaapClient client;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getAllTopicsTest() {
        Set<String> topics = new HashSet<String>();
        topics.add("topic1");
        topics.add("topic2");
        Configuration configuration = Configuration.getInstance();
        List<String> list = new ArrayList<String>();
        list.add("server");
        configuration.setDmaapServers(list);
        configuration.setCg("cg");
        configuration.setCid("cid");
        configuration.setPollingInterval(30);
        configuration.setPollingTimeout(100);

        try {
            when(topicManager.getTopics()).thenReturn(topics);

            client = Mockito.mock(DmaapClient.class);
            client.initClient();
            Mockito.verify(client).initClient();
            // Mockito.verifycreateAndConfigureTopics();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void startClientTest() {
        try {
            Configuration configuration = Configuration.getInstance();
            String configAllJson = readFromFile("src/test/resources/config_all.json");

            JsonObject configAll = new Gson().fromJson(configAllJson, JsonObject.class);

            JsonObject config = configAll.getAsJsonObject("config");
            System.out.println(configuration);
            configuration.updateConfigurationFromJsonObject(config);
            DmaapClient client = new DmaapClient();
            client.initClient();
            // Mockito.verify(client).startClient();
            // Mockito.verifycreateAndConfigureTopics();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readFromFile(String file) {
        String content = "";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            content = bufferedReader.readLine();
            String temp;
            while ((temp = bufferedReader.readLine()) != null) {
                content = content.concat(temp);
            }
            content = content.trim();
        } catch (Exception e) {
            content = null;
        }
        return content;
    }
}
