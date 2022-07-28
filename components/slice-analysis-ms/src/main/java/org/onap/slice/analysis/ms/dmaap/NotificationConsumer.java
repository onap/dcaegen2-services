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

import com.google.gson.JsonElement;
import io.vavr.collection.List;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Consume Notifications from DMAAP events
 */
public class NotificationConsumer implements Runnable {

    private static Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private NotificationCallback notificationCallback;
    private MessageRouterSubscriber subscriber;
    private MessageRouterSubscribeRequest request;

    /**
     * Parameterized Constructor.
     */
    public NotificationConsumer(MessageRouterSubscriber subscriber, MessageRouterSubscribeRequest request, NotificationCallback notificationCallback) {
        super();
        this.subscriber = subscriber;
        this.request = request;
        this.notificationCallback = notificationCallback;
    }

    /**
     * starts fetching msgs from dmaap events
     */
    @Override
    public void run() {
        try {
            Mono<MessageRouterSubscribeResponse> responses = this.subscriber.get(this.request);

            MessageRouterSubscribeResponse resp = responses.block();
            log.debug(resp.toString());

            List<JsonElement> list = resp.items();
            for(int i=0; i<list.size(); i++){
                String msg = list.get(i).toString();
                notificationCallback.activateCallBack(msg);
            }
        } catch (Exception e) {
            log.debug("exception when fetching msgs from dmaap", e);
        }

    }
}
