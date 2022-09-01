/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 China Mobile.
 *  Copyright (c) 2021-2022 Wipro Limited.
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

package org.onap.dcaegen2.kpi.utils;

import org.onap.dcaegen2.kpi.models.Configuration;
import org.onap.dcaegen2.services.sdk.model.streams.ImmutableAafCredentials;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSource;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DmaapClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;

/**
 * Utility class to perform actions related to Dmaap.
 *
 * @author Kai Lu
 *
 */
public class DmaapUtils {

    public MessageRouterPublisher buildPublisher() {
        final MessageRouterPublisher publisher = DmaapClientFactory
             .createMessageRouterPublisher(MessageRouterPublisherConfig.createDefault());
        return publisher;
    }

    public MessageRouterPublishRequest buildPublisherRequest(Configuration config, String topicUrl) {
        MessageRouterSink sinkDefinition = ImmutableMessageRouterSink.builder().topicUrl(topicUrl)
             .aafCredentials(ImmutableAafCredentials.builder().username(config.getAafUsername())
             .password(config.getAafPassword()).build())
             .build();
        MessageRouterPublishRequest request = ImmutableMessageRouterPublishRequest.builder()
             .sinkDefinition(sinkDefinition).build();
        return request;
    }

    public MessageRouterSubscriber buildSubscriber() {
        MessageRouterSubscriber subscriber = DmaapClientFactory
            .createMessageRouterSubscriber(MessageRouterSubscriberConfig.createDefault());
        return subscriber;
    }

    public MessageRouterSubscribeRequest buildSubscriberRequest(Configuration config, String topicUrl) {
        MessageRouterSource sourceDefinition = ImmutableMessageRouterSource.builder().topicUrl(topicUrl)
             .aafCredentials(ImmutableAafCredentials.builder().username(config.getAafUsername())
             .password(config.getAafPassword()).build())
             .build();
        MessageRouterSubscribeRequest request = ImmutableMessageRouterSubscribeRequest.builder()
             .consumerGroup(config.getCg()).consumerId(config.getCid()).sourceDefinition(sourceDefinition).build();
        return request;
    }
}
