/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 China Mobile.
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

import java.io.IOException;

import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;

/**
 * Produces Notification on DMAAP events.
 */
public class NotificationProducer {

	private static Logger logger = LoggerFactory.getLogger(NotificationProducer.class);
	private MessageRouterPublisher messageRouterPublisher;
	private MessageRouterPublishRequest messageRouterPublishRequest;

	/**
     * Parameterized constructor.
     */
    public NotificationProducer(MessageRouterPublisher messageRouterPublisher, MessageRouterPublishRequest messageRouterPublishRequest) {
        super();
        this.messageRouterPublisher = messageRouterPublisher;
        this.messageRouterPublishRequest = messageRouterPublishRequest;
    }

	/**
	 * sends notification to dmaap.
	 */
	public void sendNotification(String msg) throws IOException {
		
		Flux.just(1, 2, 3)
        .map(JsonPrimitive::new)
        .transform(input -> messageRouterPublisher.put(messageRouterPublishRequest, input))
        .subscribe(resp -> {
                    if (resp.successful()) {
                        logger.debug("Sent a batch of messages to the MR");
                    } else {
                        logger.warn("Message sending has failed: {}", resp.failReason());
                    }
                },
                ex -> {
                    logger.warn("An unexpected error while sending messages to DMaaP", ex);
                });
	}

}
