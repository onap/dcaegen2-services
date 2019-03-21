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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.net.ssl.SSLException;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.exceptions.EmptyDmaapResponseException;
import org.onap.bbs.event.processor.model.CpeAuthenticationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.ImmutableCpeAuthenticationConsumerDmaapModel;
import org.onap.bbs.event.processor.utilities.CpeAuthenticationDmaapConsumerJsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.ImmutableDmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer.ConsumerReactiveHttpClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer.DMaaPConsumerReactiveHttpClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DmaapCpeAuthenticationConsumerTaskImplTest {

    private static final String CPE_AUTHENTICATION_EVENT_TEMPLATE = "{\"event\": {"
            + "\"commonEventHeader\": { \"sourceName\":\"%s\"},"
            + "\"stateChangeFields\": {"
            + " \"oldState\": \"%s\","
            + " \"newState\": \"%s\","
            + " \"stateInterface\": \"%s\","
            + " \"additionalFields\": {"
            + "   \"macAddress\": \"%s\","
            + "   \"swVersion\": \"%s\""
            + "}}}}";

    private static DmaapCpeAuthenticationConsumerTask dmaapConsumerTask;
    private static CpeAuthenticationConsumerDmaapModel cpeAuthenticationConsumerDmaapModel;
    private static DMaaPConsumerReactiveHttpClient dMaaPConsumerReactiveHttpClient;
    private static String eventsArray;

    @BeforeAll
    static void setUp() throws SSLException {

        final String sourceName = "PNF-CorrelationId";
        final String oldAuthenticationState = "outOfService";
        final String newAuthenticationState = "inService";
        final String stateInterface = "stateInterface";
        final String rgwMacAddress = "00:0a:95:8d:78:16";
        final String swVersion = "1.2";

        // Mock Re-registration configuration
        DmaapConsumerConfiguration dmaapConsumerConfiguration = testVersionOfDmaapConsumerConfiguration();
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getDmaapCpeAuthenticationConsumerConfiguration()).thenReturn(dmaapConsumerConfiguration);

        // Mock reactive DMaaP client
        ConsumerReactiveHttpClientFactory httpClientFactory = mock(ConsumerReactiveHttpClientFactory.class);
        dMaaPConsumerReactiveHttpClient = mock(DMaaPConsumerReactiveHttpClient.class);
        doReturn(dMaaPConsumerReactiveHttpClient).when(httpClientFactory).create(dmaapConsumerConfiguration);

        dmaapConsumerTask = new DmaapCpeAuthenticationConsumerTaskImpl(configuration,
                new CpeAuthenticationDmaapConsumerJsonParser(), httpClientFactory);

        cpeAuthenticationConsumerDmaapModel = ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(sourceName)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rgwMacAddress(rgwMacAddress)
                .swVersion(swVersion)
                .build();

        String event = String.format(CPE_AUTHENTICATION_EVENT_TEMPLATE, sourceName, oldAuthenticationState,
                newAuthenticationState, stateInterface, rgwMacAddress, swVersion);

        eventsArray = "[" + event + "]";
    }

    @AfterEach
    void resetMock() {
        reset(dMaaPConsumerReactiveHttpClient);
    }

    @Test
    void passingEmptyMessage_NothingHappens() throws Exception {
        when(dMaaPConsumerReactiveHttpClient.getDMaaPConsumerResponse()).thenReturn(Mono.just(""));

        StepVerifier.create(dmaapConsumerTask.execute("Sample input"))
                .expectSubscription()
                .expectError(EmptyDmaapResponseException.class);
        verify(dMaaPConsumerReactiveHttpClient).getDMaaPConsumerResponse();
    }

    @Test
    void passingNormalMessage_ResponseSucceeds() throws Exception {
        when(dMaaPConsumerReactiveHttpClient.getDMaaPConsumerResponse()).thenReturn(Mono.just(eventsArray));

        StepVerifier.create(dmaapConsumerTask.execute("Sample input"))
                .expectSubscription()
                .consumeNextWith(e -> Assert.assertEquals(e, cpeAuthenticationConsumerDmaapModel));
        verify(dMaaPConsumerReactiveHttpClient).getDMaaPConsumerResponse();
    }

    private static DmaapConsumerConfiguration testVersionOfDmaapConsumerConfiguration() {
        return new ImmutableDmaapConsumerConfiguration.Builder()
                .consumerGroup("consumer-group")
                .consumerId("consumer-id")
                .dmaapContentType("application/json")
                .dmaapHostName("message-router.onap.svc.cluster.local")
                .dmaapPortNumber(3904)
                .dmaapProtocol("http")
                .dmaapUserName("admin")
                .dmaapUserPassword("admin")
                .trustStorePath("change it")
                .trustStorePasswordPath("change_it")
                .keyStorePath("change it")
                .keyStorePasswordPath("change_it")
                .enableDmaapCertAuth(false)
                .dmaapTopicName("/events/unauthenticated.CPE_AUTHENTICATION")
                .timeoutMs(-1)
                .messageLimit(-1)
                .build();
    }
}