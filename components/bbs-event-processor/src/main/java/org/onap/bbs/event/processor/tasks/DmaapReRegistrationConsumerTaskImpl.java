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

import javax.net.ssl.SSLException;

import org.onap.bbs.event.processor.config.ApplicationConfiguration;
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
public class DmaapReRegistrationConsumerTaskImpl implements DmaapReRegistrationConsumerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapReRegistrationConsumerTaskImpl.class);
    private final ApplicationConfiguration configuration;
    private final ReRegistrationDmaapConsumerJsonParser reRegistrationDmaapConsumerJsonParser;
    private final ConsumerReactiveHttpClientFactory httpClientFactory;

    @Autowired
    public DmaapReRegistrationConsumerTaskImpl(ApplicationConfiguration configuration) {
        this(configuration, new ReRegistrationDmaapConsumerJsonParser(),
                new ConsumerReactiveHttpClientFactory(new DMaaPReactiveWebClientFactory()));
    }

    DmaapReRegistrationConsumerTaskImpl(ApplicationConfiguration configuration,
                                                ReRegistrationDmaapConsumerJsonParser reRegDmaapConsumerJsonParser,
                                                ConsumerReactiveHttpClientFactory httpClientFactory) {
        this.configuration = configuration;
        this.reRegistrationDmaapConsumerJsonParser = reRegDmaapConsumerJsonParser;
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public Flux<ReRegistrationConsumerDmaapModel> execute(String taskName) throws SSLException {
        LOGGER.debug("Executing task for Re-Registration with name \"{}\"", taskName);
        DMaaPConsumerReactiveHttpClient dmaaPConsumerReactiveHttpClient = resolveClient();
        Mono<String> response = dmaaPConsumerReactiveHttpClient.getDMaaPConsumerResponse();
        return reRegistrationDmaapConsumerJsonParser.extractModelFromDmaap(response)
                .switchIfEmpty(Flux.error(
                        new EmptyDmaapResponseException("Re-Registration: Got an empty response from DMaaP")))
                .doOnError(e -> {
                    if (!(e instanceof EmptyDmaapResponseException)) {
                        LOGGER.error("DMaaP Consumption Exception: {}", e.getMessage());
                    }
                });
    }

    @Override
    public DMaaPConsumerReactiveHttpClient resolveClient() throws SSLException {
        return httpClientFactory.create(configuration.getDmaapReRegistrationConsumerConfiguration());
    }
}
