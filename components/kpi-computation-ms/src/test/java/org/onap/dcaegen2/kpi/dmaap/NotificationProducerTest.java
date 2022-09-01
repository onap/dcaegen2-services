/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 China Mobile.
 *  Copyright (C) 2022 Wipro Limited.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.dcaegen2.kpi.computation.FileUtils;
import org.onap.dcaegen2.kpi.models.Configuration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NotificationProducerTest.class)
public class NotificationProducerTest {

    private static final String VES_MESSAGE_FILE = "kpi/ves_message.json";
    private static final String CBS_CONFIG_FILE = "kpi/cbs_config2.json";

    @Mock
    MessageRouterPublisher messageRouterPublisher;
	
    @Mock
    MessageRouterPublishRequest messageRouterPublishRequest;
    
    @InjectMocks
    NotificationProducer notificationProducer;

    @Test
    public void notificationProducerTest() throws IOException {
    	io.vavr.collection.List<String> expectedItems = io.vavr.collection.List.of("kpi-1", "kpi-2", "kpi-3");
    	MessageRouterPublishResponse expectedResponse = ImmutableMessageRouterPublishResponse
                .builder().items(expectedItems.map(JsonPrimitive::new))
                .build();    	
    	Flux<MessageRouterPublishResponse> responses = Flux.just(expectedResponse);
        when(messageRouterPublisher.put(Mockito.any(), Mockito.any())).thenReturn(responses);
        notificationProducer.sendNotification("msg");
    }
    
    @Test
    public void kpiResultWithoutConfigTest() {

        String vesMessage = FileUtils.getFileContents(VES_MESSAGE_FILE);
        KpiComputationCallBack callback = new KpiComputationCallBack();
        callback.activateCallBack(vesMessage);

    }

    @Test
    public void kpiResultWithConfigTest() {

        String vesMessage = FileUtils.getFileContents(VES_MESSAGE_FILE);
        String strCbsConfig = FileUtils.getFileContents(CBS_CONFIG_FILE);

        JsonObject jsonObject = new JsonParser().parse(strCbsConfig).getAsJsonObject().getAsJsonObject("config");
        Configuration config = new Configuration();
        config.updateConfigurationFromJsonObject(jsonObject);

        KpiComputationCallBack callback = new KpiComputationCallBack();
        callback.kpiComputation(vesMessage, config);

    }

}
