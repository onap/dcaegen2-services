/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 CTC.
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

package org.onap.slice.analysis.ms.utils;

import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSource;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.ContentType;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DmaapClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.ImmutableDmaapConnectionPoolConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.ImmutableMessageRouterSubscriberConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;
import org.onap.slice.analysis.ms.models.Configuration;

public class DcaeDmaapUtil {
    public static MessageRouterSubscriber buildSubscriber(){
        MessageRouterSubscriberConfig connectionPoolConfiguration = ImmutableMessageRouterSubscriberConfig.builder()
                .connectionPoolConfig(ImmutableDmaapConnectionPoolConfig.builder()
                        .connectionPool(16)
                        .maxIdleTime(10) //in seconds
                        .maxLifeTime(20) //in seconds
                        .build())
                .build();

        MessageRouterSubscriber cut = DmaapClientFactory.createMessageRouterSubscriber(connectionPoolConfiguration);
        return cut;
    }

    public static MessageRouterSubscribeRequest buildSubscriberRequest(String name, String topicUrl){
        MessageRouterSource sourceDefinition = ImmutableMessageRouterSource.builder()
                .name(name)
                .topicUrl(topicUrl)
                .build();
        MessageRouterSubscribeRequest request = ImmutableMessageRouterSubscribeRequest.builder()
                .consumerGroup("1")
                .consumerId("1")
                .sourceDefinition(sourceDefinition)
                .build();

        return request;
    }

    public static MessageRouterPublisher buildPublisher(){
        MessageRouterPublisher pub = DmaapClientFactory
                .createMessageRouterPublisher(MessageRouterPublisherConfig.createDefault());
        return pub;
    }

    public static MessageRouterPublishRequest buildPublisherRequest(String name, String topicUrl){
        MessageRouterSink sinkDefinition = ImmutableMessageRouterSink.builder()
                .name(name)
                .topicUrl(topicUrl)
                .build();
        MessageRouterPublishRequest request = ImmutableMessageRouterPublishRequest.builder()
                .sinkDefinition(sinkDefinition)
                .contentType(ContentType.TEXT_PLAIN)
                .build();
        return request;
    }


}
