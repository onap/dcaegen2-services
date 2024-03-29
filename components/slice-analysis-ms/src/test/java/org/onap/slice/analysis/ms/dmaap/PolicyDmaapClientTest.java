/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
 *   Copyright (C) 2022 CTC, Inc.
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

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonPrimitive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.utils.DcaeDmaapUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ DcaeDmaapUtil.class})
@SpringBootTest(classes = PolicyDmaapClientTest.class)
public class PolicyDmaapClientTest {
    
    @Mock
    Configuration configurationMock;
    
    @InjectMocks
    PolicyDmaapClient policyDmaapClient;
    
    @Before
    public void setup() {
        policyDmaapClient = new PolicyDmaapClient(configurationMock);
    }
    
    @Test
    public void sendNotificationToPolicyTest() {
        Map<String, Object> streamsPublishes = new HashMap<>();
        Map<String, String> topics = new HashMap<>();
        Map<String, Object> dmaapInfo = new HashMap<>();
        topics.put("topic_url", "https://message-router.onap.svc.cluster.local:3905/events/DCAE_CL_OUTPUT");
        dmaapInfo.put("dmaap_info", topics);
        streamsPublishes.put("CL_topic", dmaapInfo);
        Mockito.when(configurationMock.getStreamsPublishes()).thenReturn(streamsPublishes);

        PowerMockito.mockStatic(DcaeDmaapUtil.class);
        MessageRouterPublisher publisher = PowerMockito.mock(MessageRouterPublisher.class);
        PowerMockito.when(DcaeDmaapUtil.buildPublisher()).thenReturn(publisher);

        MessageRouterPublishRequest request = PowerMockito.mock(MessageRouterPublishRequest.class);
        PowerMockito.when(DcaeDmaapUtil.buildPublisherRequest(any(),any())).thenReturn(request);

        io.vavr.collection.List<String> expectedItems = io.vavr.collection.List.of("I", "like", "pizza");
        MessageRouterPublishResponse expectedResponse = ImmutableMessageRouterPublishResponse
                .builder().items(expectedItems.map(JsonPrimitive::new))
                .build();
        Flux<MessageRouterPublishResponse> responses = Flux.just(expectedResponse);
        when(publisher.put(any(), any())).thenReturn(responses);

        policyDmaapClient.sendNotificationToPolicy("msg");
    }
}
