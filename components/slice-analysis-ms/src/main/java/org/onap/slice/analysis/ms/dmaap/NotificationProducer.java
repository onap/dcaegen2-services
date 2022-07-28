/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020-2021 Wipro Limited.
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

import com.google.gson.JsonPrimitive;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import reactor.core.publisher.Flux;

import java.io.IOException;

/**
 * Produces Notification on DMAAP events
 */
public class NotificationProducer {

    private MessageRouterPublisher publisher;
    private MessageRouterPublishRequest request;
     
    /**
     * Parameterized constructor.
     */
    public NotificationProducer(MessageRouterPublisher publisher, MessageRouterPublishRequest request) {
        super();
        this.publisher = publisher;
        this.request = request;
    }

    /**
     * sends notification to dmaap.
     */
    public void sendNotification(String msg) throws IOException {
        Flux<JsonPrimitive> singleMessage = Flux.just(msg).map(JsonPrimitive::new);
        Flux<MessageRouterPublishResponse> result = this.publisher.put(request, singleMessage);
        result.then().block();
    }

}
