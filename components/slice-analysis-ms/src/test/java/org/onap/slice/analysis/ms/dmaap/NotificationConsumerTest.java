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

import static org.mockito.Mockito.when;

import com.google.gson.JsonPrimitive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NotificationConsumerTest.class)
public class NotificationConsumerTest {
    
    @Mock
    NotificationCallback notificationCallback;

    @Mock
    MessageRouterSubscriber subscriber;

    @Mock
    MessageRouterSubscribeRequest request;

    @InjectMocks
    NotificationConsumer notificationConsumer;

    @Test
    public void testNotificationConsumer() {
        try {
            io.vavr.collection.List<String> expectedItems = io.vavr.collection.List.of("I", "like", "pizza");
            MessageRouterSubscribeResponse expectedResponse = ImmutableMessageRouterSubscribeResponse
                    .builder()
                    .items(expectedItems.map(JsonPrimitive::new))
                    .build();

            Mono<MessageRouterSubscribeResponse> responses = Mono.just(expectedResponse);
            when(subscriber.get(request)).thenReturn(responses);
            Mockito.doNothing().when(notificationCallback).activateCallBack(Mockito.anyString());
            notificationConsumer.run();
            
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

}
