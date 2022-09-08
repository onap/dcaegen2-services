/*
 * ============LICENSE_START=======================================================
 * BBS-RELOCATION-CPE-AUTHENTICATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.bbs.event.processor.utilities;

import java.nio.file.Paths;

import org.jetbrains.annotations.NotNull;
import org.onap.bbs.event.processor.exceptions.ApplicationEnvironmentException;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.ContentType;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeysStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class GenericUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericUtils.class);

    private GenericUtils() {}

    /**
     * Creates Message Router subscription request.
     * @param topicUrl URL of topic to use
     * @param consumerGroup Consumer Group for subscription
     * @param consumerId Consumer ID for subscription
     * @return request based on provided input
     */
    public static MessageRouterSubscribeRequest createSubscribeRequest(String topicUrl,
                                                                       String consumerGroup, String consumerId) {
        var sourceDefinition = ImmutableMessageRouterSource.builder()
                .name("Subscriber source")
                .topicUrl(topicUrl)
                .build();

        return ImmutableMessageRouterSubscribeRequest
                .builder()
                .sourceDefinition(sourceDefinition)
                .consumerGroup(consumerGroup)
                .consumerId(consumerId)
                .build();
    }

    /**
     * Creates Message Router publish request.
     * @param topicUrl URL of topic to use
     * @return request based on provided input
     */
    public static MessageRouterPublishRequest createPublishRequest(String topicUrl) {
        MessageRouterSink sinkDefinition = ImmutableMessageRouterSink.builder()
                .topicUrl(topicUrl)
                .name("Producer sink")
                .build();

        return ImmutableMessageRouterPublishRequest.builder()
                .sinkDefinition(sinkDefinition)
                .contentType(ContentType.APPLICATION_JSON)
                .build();
    }

    /**
     * Creates a security key store for HTTPS.
     * @param resource identifying the resource from which we will read security information
     * @return store that will be used for HTTPS
     */
    @NotNull public static SecurityKeysStore keyStoreFromResource(String resource) {
        if (StringUtils.isEmpty(resource)) {
            throw new ApplicationEnvironmentException("Resource for security key store is empty");
        }
        var path = Paths.get(resource);
        LOGGER.info("Reading keys from {}", path.toString());
        return SecurityKeysStore.fromPath(path);
    }
}
