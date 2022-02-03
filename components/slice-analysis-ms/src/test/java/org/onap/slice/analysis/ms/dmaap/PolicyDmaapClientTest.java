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

import static org.junit.Assert.assertTrue;

import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import com.att.nsa.cambria.client.CambriaConsumer;

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
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.utils.DmaapUtils;
import org.springframework.boot.test.context.SpringBootTest;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = PolicyDmaapClient.class)
public class PolicyDmaapClientTest {

    @Mock
    Configuration configurationMock;

    @Mock
    DmaapUtils dmaapUtilsMock;

    @InjectMocks
    PolicyDmaapClient policyDmaapClient;

    @Mock
    CambriaConsumer policyResponseCambriaConsumerMock;

    @Mock
    CambriaBatchingPublisher cambriaBatchingPublisherMock;

    @Mock
    NotificationProducer notificationProducerMock;

    @Before
    public void setup() {
        policyDmaapClient = new PolicyDmaapClient(dmaapUtilsMock, configurationMock);
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
        Mockito.when(dmaapUtilsMock.buildPublisher(configurationMock, "DCAE_CL_OUTPUT"))
                .thenReturn(cambriaBatchingPublisherMock);
        try {
            Mockito.when(cambriaBatchingPublisherMock.send("", "hello")).thenReturn(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertTrue(policyDmaapClient.sendNotificationToPolicy("hello"));

    }
}
