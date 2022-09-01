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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.dcaegen2.kpi.models.Configuration;
import org.onap.dcaegen2.kpi.utils.DmaapUtils;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = KpiDmaapClient.class)
public class KpiDmaapClientTest {

    @Mock
    Configuration configurationMock;

    @Mock
    DmaapUtils dmaapUtilsMock;    
    
    @Mock
    MessageRouterPublisher messageRouterPublisher;

    @Mock
    MessageRouterPublishRequest messageRouterPublishRequest;

    @Mock
    KpiDmaapClient kpiDmaapClient;

    @Mock
    NotificationProducer notificationProducerMock;

    @Test
    public void sendNotificationToPolicyTest() throws IOException {
        Map<String, Object> streamsPublishes = new HashMap<>();
        Map<String, String> topics = new HashMap<>();
        Map<String, Object> dmaapInfo = new HashMap<>();
        topics.put("topic_url", "https://message-router.onap.svc.cluster.local:3905/events/DCAE_KPI_OUTPUT");
        dmaapInfo.put("dmaap_info", topics);
        streamsPublishes.put("kpi_topic", dmaapInfo);
        Mockito.when(configurationMock.getStreamsPublishes()).thenReturn(streamsPublishes);	
        Mockito.doNothing().when(notificationProducerMock).sendNotification(Mockito.anyString());
        io.vavr.collection.List<String> expectedItems = io.vavr.collection.List.of("kpi-1", "kpi-2", "kpi-3");
        MessageRouterPublishResponse expectedResponse = ImmutableMessageRouterPublishResponse
             .builder().items(expectedItems.map(JsonPrimitive::new))
             .build();    	
        Flux<MessageRouterPublishResponse> responses = Flux.just(expectedResponse);
        when(messageRouterPublisher.put(Mockito.any(), Mockito.any())).thenReturn(responses);
        when(kpiDmaapClient.sendNotificationToDmaap(Mockito.anyString())).thenReturn(Boolean.TRUE);
        Boolean response = kpiDmaapClient.sendNotificationToDmaap(Mockito.anyString());
        assertEquals(Boolean.TRUE, response);
        }
}
