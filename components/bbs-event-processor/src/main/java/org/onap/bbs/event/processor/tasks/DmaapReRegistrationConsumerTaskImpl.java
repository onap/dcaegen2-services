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

import static org.onap.bbs.event.processor.config.ApplicationConstants.SUBSCRIBE_URL_TEMPLATE;
import static org.onap.bbs.event.processor.utilities.GenericUtils.createSubscribeRequest;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.config.ConfigurationChangeObserver;
import org.onap.bbs.event.processor.exceptions.EmptyDmaapResponseException;
import org.onap.bbs.event.processor.model.ReRegistrationConsumerDmaapModel;
import org.onap.bbs.event.processor.utilities.ReRegistrationDmaapConsumerJsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DmaapReRegistrationConsumerTaskImpl implements DmaapReRegistrationConsumerTask,
        ConfigurationChangeObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapReRegistrationConsumerTaskImpl.class);

    private final ReRegistrationDmaapConsumerJsonParser reRegistrationDmaapConsumerJsonParser;
    private ApplicationConfiguration configuration;
    private MessageRouterSubscriber subscriber;
    private String subscribeUrl;
    private MessageRouterSubscribeRequest subscribeRequest;

    private static final EmptyDmaapResponseException EMPTY_DMAAP_EXCEPTION =
            new EmptyDmaapResponseException("PNF Re-Registration: Got an empty response from DMaaP");

    @Autowired
    DmaapReRegistrationConsumerTaskImpl(ApplicationConfiguration configuration,
                                        @Qualifier("ReRegMessageRouterSubscriber") MessageRouterSubscriber subscriber,
                                                ReRegistrationDmaapConsumerJsonParser parser) {
        this.reRegistrationDmaapConsumerJsonParser = parser;
        this.configuration = configuration;
        this.subscriber = subscriber;
        subscribeUrl = String.format(SUBSCRIBE_URL_TEMPLATE,
                this.configuration.getDmaapReRegistrationConsumerProperties().getDmaapProtocol(),
                this.configuration.getDmaapReRegistrationConsumerProperties().getDmaapHostName(),
                this.configuration.getDmaapReRegistrationConsumerProperties().getDmaapPortNumber(),
                this.configuration.getDmaapReRegistrationConsumerProperties().getDmaapTopicName());

        subscribeRequest = createSubscribeRequest(
                subscribeUrl,
                this.configuration.getDmaapReRegistrationConsumerProperties().getConsumerGroup(),
                this.configuration.getDmaapReRegistrationConsumerProperties().getConsumerId());
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
        LOGGER.info("DMaaP PNF reregistration consumer update due to new application configuration");
        subscribeUrl = String.format(SUBSCRIBE_URL_TEMPLATE,
                configuration.getDmaapReRegistrationConsumerProperties().getDmaapProtocol(),
                configuration.getDmaapReRegistrationConsumerProperties().getDmaapHostName(),
                configuration.getDmaapReRegistrationConsumerProperties().getDmaapPortNumber(),
                configuration.getDmaapReRegistrationConsumerProperties().getDmaapTopicName());
        subscribeRequest = createSubscribeRequest(
                subscribeUrl,
                this.configuration.getDmaapReRegistrationConsumerProperties().getConsumerGroup(),
                this.configuration.getDmaapReRegistrationConsumerProperties().getConsumerId());
    }

    @Override
    public Flux<ReRegistrationConsumerDmaapModel> execute(String taskName) {
        LOGGER.debug("Executing task for Re-Registration with name \"{}\"", taskName);
        return subscriber.getElements(subscribeRequest)
                .flatMap(jsonElement ->
                        reRegistrationDmaapConsumerJsonParser.extractModelFromDmaap(Mono.just(jsonElement)))
                .switchIfEmpty(Mono.error(EMPTY_DMAAP_EXCEPTION))
                .doOnError(e -> {
                    if (!(e instanceof EmptyDmaapResponseException)) {
                        LOGGER.error("DMaaP Consumption Exception: {}", e.getMessage());
                        LOGGER.debug("Exception\n", e);
                    }
                });
    }
}
