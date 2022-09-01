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

import java.time.Duration;

import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * Consume Notifications from DMAAP events.
 */
public class NotificationConsumer implements Runnable {

    private static Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private MessageRouterSubscriber messageSubscriber;
    private MessageRouterSubscribeRequest subscriberRequest;
    private NotificationCallback notificationCallback;

    /**
     * Parameterized Constructor.
     */
    public NotificationConsumer(MessageRouterSubscriber messageSubscriber, MessageRouterSubscribeRequest subscriberRequest, NotificationCallback notificationCallback) {
        super();
        this.messageSubscriber = messageSubscriber;
        this.subscriberRequest = subscriberRequest;
        this.notificationCallback = notificationCallback;
    }

    /**
     * starts fetching msgs from dmaap events.
     */
    @Override
    public void run() {
        	messageSubscriber.subscribeForElements(subscriberRequest, Duration.ofMinutes(1))
            .map(JsonElement::getAsString)
            .subscribe(msg -> {
            	log.info(msg);
            	notificationCallback.activateCallBack(msg);
            },
                    ex -> {
                        log.warn("An unexpected error while receiving messages from DMaaP", ex);
                    });
    }
}
