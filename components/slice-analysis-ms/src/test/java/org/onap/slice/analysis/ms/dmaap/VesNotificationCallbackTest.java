/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
 *   Copyright (C) 2022 Huawei Canada Limited.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.slice.analysis.ms.models.Configuration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VesNotificationCallbackTest.class)
public class VesNotificationCallbackTest {
    ObjectMapper obj = new ObjectMapper();

    @Spy
    @InjectMocks
    VesNotificationCallback vesNotificationCallback;

    @Before
    public void init() throws IOException {
        Configuration configuration = Configuration.getInstance();
        String configAllJson = new String(Files.readAllBytes(Paths.get("src/test/resources/config_all.json")));
        JsonObject configAll = new Gson().fromJson(configAllJson, JsonObject.class);
        JsonObject config = configAll.getAsJsonObject("config");
        configuration.updateConfigurationFromJsonObject(config);
        vesNotificationCallback.init();
    }

    @Test
    public void initTest() {
        Mockito.verify(vesNotificationCallback, Mockito.atLeastOnce()).init();
    }

    @Test
    public void activateCallBackTest() throws Exception{
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/vesCCVPNNotiModel.json")));
        vesNotificationCallback.activateCallBack(input);
        Mockito.verify(vesNotificationCallback, Mockito.atLeastOnce()).activateCallBack(Mockito.anyString());
    }

}
