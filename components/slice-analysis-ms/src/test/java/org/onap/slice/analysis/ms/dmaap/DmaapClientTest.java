/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
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


package org.onap.slice.analysis.ms.dmaap;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileReader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.slice.analysis.ms.models.Configuration;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DmaapClientTest.class)
public class DmaapClientTest {

    @InjectMocks
    DmaapClient client;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void startClientTest() {
        Configuration configuration = Configuration.getInstance();
        String configAllJson = readFromFile("src/test/resources/config_all.json");

        JsonObject configAll = new Gson().fromJson(configAllJson, JsonObject.class);

        JsonObject config = configAll.getAsJsonObject("config");
        System.out.println(configuration);
        configuration.updateConfigurationFromJsonObject(config);
        MRTopicMonitor mrTopicMonitor = Mockito.mock(MRTopicMonitor.class);
        DmaapClient spy = PowerMockito.spy(client);
        doReturn(mrTopicMonitor).when(spy).getMRTopicMonitor();
        doNothing().when(mrTopicMonitor).start();
        spy.initClient();
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
