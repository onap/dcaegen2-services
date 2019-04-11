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

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLException;

import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.config.ConfigurationChangeObserver;
import org.onap.bbs.event.processor.exceptions.DmaapException;
import org.onap.bbs.event.processor.model.ControlLoopPublisherDmaapModel;
import org.onap.bbs.event.processor.utilities.ControlLoopJsonBodyBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.DMaaPPublisherReactiveHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.DmaaPRestTemplateFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.PublisherReactiveHttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class DmaapPublisherTaskImpl implements DmaapPublisherTask, ConfigurationChangeObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapPublisherTaskImpl.class);
    private ApplicationConfiguration configuration;
    private final PublisherReactiveHttpClientFactory httpClientFactory;

    private DMaaPPublisherReactiveHttpClient httpClient;

    @Autowired
    DmaapPublisherTaskImpl(ApplicationConfiguration configuration) {
        this(configuration, new PublisherReactiveHttpClientFactory(new DmaaPRestTemplateFactory(),
                new ControlLoopJsonBodyBuilder()));
    }

    DmaapPublisherTaskImpl(ApplicationConfiguration configuration,
                           PublisherReactiveHttpClientFactory httpClientFactory) {
        this.configuration = configuration;
        this.httpClientFactory = httpClientFactory;

        try {
            httpClient = httpClientFactory.create(this.configuration.getDmaapPublisherConfiguration());
        } catch (SSLException e) {
            LOGGER.error("SSL error while creating HTTP Client: {}", e.getMessage());
            LOGGER.debug("SSL exception\n", e);
        }
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
        try {
            LOGGER.info("Creating secure context with:\n {}", this.configuration.getDmaapPublisherConfiguration());
            httpClient = httpClientFactory.create(this.configuration.getDmaapPublisherConfiguration());
        } catch (SSLException e) {
            LOGGER.error("SSL error while updating HTTP Client after a config update: {}", e.getMessage());
            LOGGER.debug("SSL exception\n", e);
        }
    }

    @Override
    public Mono<HttpResponse> execute(ControlLoopPublisherDmaapModel controlLoopPublisherDmaapModel) {
        if (controlLoopPublisherDmaapModel == null) {
            throw new DmaapException("Cannot invoke a DMaaP Publish task with a null message");
        }
        LOGGER.info("Executing task for publishing control loop message");
        LOGGER.debug("CL message \n{}", controlLoopPublisherDmaapModel);
        DMaaPPublisherReactiveHttpClient httpClient = getHttpClient();
        return httpClient.getDMaaPProducerResponse(controlLoopPublisherDmaapModel, Optional.empty());
    }

    private synchronized DMaaPPublisherReactiveHttpClient getHttpClient() {
        return httpClient;
    }
}
