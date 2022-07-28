/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *  Copyright (C) 2022 Huawei Canada Limited.
 *  Copyright (C) 2022 CTC, Inc.
 *  ==============================================================================
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.service.ccvpn.BandwidthEvaluator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.doNothing;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = VesNotificationCallbackTest.class)
public class AaiEventNotificationCallbackTest {

    @Spy
    @InjectMocks
    AaiEventNotificationCallback aaiEventNotificationCallback;

    @Mock
    BandwidthEvaluator bandwidthEvaluator;

    @Before
    public void initConfiguration() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("sliceanalysisms.aaiNotif.targetAction", "UPDATE");
        jsonObject.addProperty("sliceanalysisms.aaiNotif.targetEntity", "service-instance");
        jsonObject.addProperty("sliceanalysisms.aaiNotif.targetSource", "UUI");
        jsonObject.addProperty("postgres.port", "1");
        jsonObject.addProperty("sliceanalysisms.pollingInterval", "1");
        jsonObject.addProperty("postgres.password", "1");
        jsonObject.addProperty("postgres.username", "1");
        jsonObject.addProperty("postgres.host", "1");
        jsonObject.addProperty("sliceanalysisms.cg", "1");
        jsonObject.addProperty("sliceanalysisms.cid", "1");
        jsonObject.addProperty("sliceanalysisms.configDb.service", "1");
        jsonObject.addProperty("sliceanalysisms.configDbEnabled", "1");
        jsonObject.addProperty("sliceanalysisms.pollingTimeout", "1");
        jsonObject.addProperty("sliceanalysisms.samples", "1");
        jsonObject.addProperty("sliceanalysisms.minPercentageChange", "1");
        jsonObject.addProperty("sliceanalysisms.initialDelaySeconds", "1");
        jsonObject.addProperty("sliceanalysisms.rannfnssiDetailsTemplateId", "1");
        jsonObject.addProperty("sliceanalysisms.desUrl", "1");
        jsonObject.addProperty("sliceanalysisms.pmDataDurationInWeeks", "1");
        jsonObject.addProperty("sliceanalysisms.pollingInterval", "1");
        jsonObject.addProperty("sliceanalysisms.vesNotifChangeIdentifier", "1");
        jsonObject.addProperty("sliceanalysisms.vesNotifChangeType", "1");
        jsonObject.addProperty("sliceanalysisms.vesNotifPollingInterval", "1");
        jsonObject.addProperty("sliceanalysisms.ccvpnEvalInterval", "1");
        jsonObject.addProperty("sliceanalysisms.ccvpnEvalThreshold", "1");
        jsonObject.addProperty("sliceanalysisms.ccvpnEvalPrecision", "1");
        jsonObject.addProperty("sliceanalysisms.ccvpnEvalPeriodicCheckOn", "1");
        jsonObject.addProperty("sliceanalysisms.ccvpnEvalOnDemandCheckOn", "1");
        Configuration configuration = Configuration.getInstance();
        configuration.updateConfigurationFromJsonObject(jsonObject);
        doNothing().when(bandwidthEvaluator).post(any());
    }

    @Test
    public void initTest() {
        aaiEventNotificationCallback.init();
        Mockito.verify(aaiEventNotificationCallback, Mockito.atLeastOnce()).init();
    }

    @Test
    public void activateCallBackTest() {
        aaiEventNotificationCallback.init();
        String input = null;
        try {
            input = new String(Files.readAllBytes(Paths.get("src/test/resources/aaiEventDmaapMsg.json")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        aaiEventNotificationCallback.activateCallBack(input);
        Mockito.verify(aaiEventNotificationCallback, Mockito.atLeastOnce()).activateCallBack(Mockito.anyString());
    }
    @Test
    public void activateCallBackArrayTest() {
        aaiEventNotificationCallback.init();
        String input = null;
        JsonArray jsonArray = new JsonArray();
        try {
            input = new String(Files.readAllBytes(Paths.get("src/test/resources/aaiEventDmaapMsg.json")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonPrimitive jsonPrimitive = new JsonPrimitive(input);
        jsonArray.add(jsonPrimitive);
        aaiEventNotificationCallback.activateCallBack(jsonArray.toString());
        Mockito.verify(aaiEventNotificationCallback, Mockito.atLeastOnce()).activateCallBack(Mockito.anyString());
    }
}
