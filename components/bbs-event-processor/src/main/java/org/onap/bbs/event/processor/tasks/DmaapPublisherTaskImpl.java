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

package org.onap.bbs.event.processor.tasks;

import static org.onap.bbs.event.processor.config.ApplicationConstants.PUBLISH_URL_TEMPLATE;
import static org.onap.bbs.event.processor.utilities.GenericUtils.createPublishRequest;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.config.ConfigurationChangeObserver;
import org.onap.bbs.event.processor.exceptions.DmaapException;
import org.onap.bbs.event.processor.model.ControlLoopPublisherDmaapModel;
import org.onap.bbs.event.processor.utilities.ControlLoopJsonBodyBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DmaapClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class DmaapPublisherTaskImpl implements DmaapPublisherTask, ConfigurationChangeObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapPublisherTaskImpl.class);

    private ApplicationConfiguration configuration;
    private MessageRouterPublisher publisher;
    private String publishUrl;
    private MessageRouterPublishRequest publishRequest;

    @Autowired
    DmaapPublisherTaskImpl(ApplicationConfiguration configuration, MessageRouterPublisher publisher) {
        this.configuration = configuration;
        this.publisher = publisher;
        publishUrl = String.format(PUBLISH_URL_TEMPLATE,
                this.configuration.getDmaapProducerProperties().getDmaapProtocol(),
                this.configuration.getDmaapProducerProperties().getDmaapHostName(),
                this.configuration.getDmaapProducerProperties().getDmaapPortNumber(),
                this.configuration.getDmaapProducerProperties().getDmaapTopicName());
        publishRequest = createPublishRequest(publishUrl);
    }

    @PostConstruct
    void registerForConfigChanges() {
        configuration.register(this);
    }

    @PreDestroy
    void unRegisterForConfigChanges() {
        configuration.unRegister(this);
    }

    @Override
    public synchronized void updateConfiguration() {
        LOGGER.info("DMaaP Publisher update due to new application configuration");
        publisher =
                DmaapClientFactory.createMessageRouterPublisher(configuration.getDmaapPublisherConfiguration());
        publishUrl = String.format(PUBLISH_URL_TEMPLATE,
                configuration.getDmaapProducerProperties().getDmaapProtocol(),
                configuration.getDmaapProducerProperties().getDmaapHostName(),
                configuration.getDmaapProducerProperties().getDmaapPortNumber(),
                configuration.getDmaapProducerProperties().getDmaapTopicName());
        publishRequest = createPublishRequest(publishUrl);
    }

    @Override
    public Flux<MessageRouterPublishResponse> execute(ControlLoopPublisherDmaapModel event) {
        if (event == null) {
            throw new DmaapException("Cannot invoke a DMaaP Publish task with a null message");
        }
        LOGGER.info("Executing task for publishing control loop message");
        LOGGER.debug("CL message \n{}", event);
        return publisher.put(publishRequest, Flux.just(ControlLoopJsonBodyBuilder.createAsJsonElement(event)));
    }
}
