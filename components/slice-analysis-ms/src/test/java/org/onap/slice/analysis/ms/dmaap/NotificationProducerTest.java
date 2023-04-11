/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
 *   Copyright (C) 2022 CTC, Inc.
 *   Copyright (C) 2023 Huawei Technologies Co., Ltd. All rights reserved.
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.gson.JsonPrimitive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NotificationProducerTest.class)
public class NotificationProducerTest {

    @Mock
    MessageRouterPublisher publisher;

    @Mock
    MessageRouterPublishRequest request;

    @InjectMocks
    NotificationProducer notificationProducer;

    @Test
    public void notificationProducerTest() throws IOException {
            io.vavr.collection.List<String> expectedItems = io.vavr.collection.List.of("I", "like", "pizza");
            MessageRouterPublishResponse expectedResponse = ImmutableMessageRouterPublishResponse
                    .builder().items(expectedItems.map(JsonPrimitive::new))
                    .build();
            Flux<MessageRouterPublishResponse> responses = Flux.just(expectedResponse);
            when(publisher.put(any(), any())).thenReturn(responses);
            notificationProducer.sendNotification("msg");
    }
}
