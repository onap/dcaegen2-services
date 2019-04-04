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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLException;

import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.config.ConfigurationChangeObserver;
import org.onap.bbs.event.processor.exceptions.EmptyDmaapResponseException;
import org.onap.bbs.event.processor.model.ReRegistrationConsumerDmaapModel;
import org.onap.bbs.event.processor.utilities.ReRegistrationDmaapConsumerJsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer.ConsumerReactiveHttpClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer.DMaaPConsumerReactiveHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer.DMaaPReactiveWebClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DmaapReRegistrationConsumerTaskImpl implements DmaapReRegistrationConsumerTask,
        ConfigurationChangeObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapReRegistrationConsumerTaskImpl.class);
    private ApplicationConfiguration configuration;
    private final ReRegistrationDmaapConsumerJsonParser reRegistrationDmaapConsumerJsonParser;
    private final ConsumerReactiveHttpClientFactory httpClientFactory;

    private static final EmptyDmaapResponseException EMPTY_DMAAP_EXCEPTION =
            new EmptyDmaapResponseException("PNF Re-Registration: Got an empty response from DMaaP");

    private DMaaPConsumerReactiveHttpClient httpClient;

    @Autowired
    public DmaapReRegistrationConsumerTaskImpl(ApplicationConfiguration configuration) throws SSLException {
        this(configuration, new ReRegistrationDmaapConsumerJsonParser(),
                new ConsumerReactiveHttpClientFactory(new DMaaPReactiveWebClientFactory()));
    }

    DmaapReRegistrationConsumerTaskImpl(ApplicationConfiguration configuration,
                                                ReRegistrationDmaapConsumerJsonParser reRegDmaapConsumerJsonParser,
                                                ConsumerReactiveHttpClientFactory httpClientFactory)
            throws SSLException {
        this.configuration = configuration;
        this.reRegistrationDmaapConsumerJsonParser = reRegDmaapConsumerJsonParser;
        this.httpClientFactory = httpClientFactory;

        httpClient = httpClientFactory.create(this.configuration.getDmaapReRegistrationConsumerConfiguration());
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
        try {
            LOGGER.info("DMaaP PNF reregistration consumer update due to new application configuration");
            httpClient = httpClientFactory.create(this.configuration.getDmaapReRegistrationConsumerConfiguration());
        } catch (SSLException e) {
            LOGGER.error("Error while updating HTTP Client after a config update: SSL exception {}", e.getMessage());
        }
    }

    @Override
    public Flux<ReRegistrationConsumerDmaapModel> execute(String taskName) {
        LOGGER.debug("Executing task for Re-Registration with name \"{}\"", taskName);
        DMaaPConsumerReactiveHttpClient httpClient = getHttpClient();
        Mono<String> response = httpClient.getDMaaPConsumerResponse();
        return reRegistrationDmaapConsumerJsonParser.extractModelFromDmaap(response)
                .switchIfEmpty(Flux.error(EMPTY_DMAAP_EXCEPTION))
                .doOnError(e -> {
                    if (!(e instanceof EmptyDmaapResponseException)) {
                        LOGGER.error("DMaaP Consumption Exception: {}", e.getMessage());
                    }
                });
    }

    private synchronized DMaaPConsumerReactiveHttpClient getHttpClient() {
        return httpClient;
    }
}
