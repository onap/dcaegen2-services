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
import org.onap.bbs.event.processor.model.ImmutableReRegistrationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.ReRegistrationConsumerDmaapModel;
import org.onap.bbs.event.processor.utilities.ReRegistrationDmaapConsumerJsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.ImmutableDmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer.ConsumerReactiveHttpClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer.DMaaPConsumerReactiveHttpClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DmaapReRegistrationConsumerTaskImplTest {

    private static final String RE_REGISTRATION_EVENT_TEMPLATE = "{\"event\": {"
            + "\"commonEventHeader\": { \"sourceName\":\"%s\"},"
            + "\"additionalFields\": {"
            + " \"attachment-point\": \"%s\","
            + " \"remote-id\": \"%s\","
            + " \"cvlan\": \"%s\","
            + " \"svlan\": \"%s\""
            + "}}}";

    private static DmaapReRegistrationConsumerTaskImpl dmaapConsumerTask;
    private static ReRegistrationConsumerDmaapModel reRegistrationConsumerDmaapModel;
    private static DMaaPConsumerReactiveHttpClient dMaaPConsumerReactiveHttpClient;
    private static String message;

    @BeforeAll
    static void setUp() throws SSLException {

        final String sourceName = "PNF-CorrelationId";
        final String attachmentPoint = "olt2/2/2";
        final String remoteId = "remoteId";
        final String cvlan = "1005";
        final String svlan = "100";

        // Mock Re-registration configuration
        DmaapConsumerConfiguration dmaapConsumerConfiguration = testVersionOfDmaapConsumerConfiguration();
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getDmaapReRegistrationConsumerConfiguration()).thenReturn(dmaapConsumerConfiguration);

        // Mock reactive DMaaP client
        ConsumerReactiveHttpClientFactory httpClientFactory = mock(ConsumerReactiveHttpClientFactory.class);
        dMaaPConsumerReactiveHttpClient = mock(DMaaPConsumerReactiveHttpClient.class);
        doReturn(dMaaPConsumerReactiveHttpClient).when(httpClientFactory).create(dmaapConsumerConfiguration);

        dmaapConsumerTask = new DmaapReRegistrationConsumerTaskImpl(configuration,
                new ReRegistrationDmaapConsumerJsonParser(), httpClientFactory);

        reRegistrationConsumerDmaapModel = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(sourceName)
                .attachmentPoint(attachmentPoint)
                .remoteId(remoteId)
                .cVlan(cvlan)
                .sVlan(svlan)
                .build();

        message = String.format("[" + RE_REGISTRATION_EVENT_TEMPLATE + "]",
                sourceName,
                attachmentPoint,
                remoteId,
                cvlan,
                svlan);
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
        when(dMaaPConsumerReactiveHttpClient.getDMaaPConsumerResponse()).thenReturn(Mono.just(message));

        StepVerifier.create(dmaapConsumerTask.execute("Sample input"))
                .expectSubscription()
                .consumeNextWith(e -> Assert.assertEquals(e, reRegistrationConsumerDmaapModel));
        verify(dMaaPConsumerReactiveHttpClient).getDMaaPConsumerResponse();
    }

    private static DmaapConsumerConfiguration testVersionOfDmaapConsumerConfiguration() {
        return new ImmutableDmaapConsumerConfiguration.Builder()
                .consumerGroup("OpenDCAE-c12")
                .consumerId("c12")
                .dmaapContentType("application/json")
                .dmaapHostName("message-router.onap.svc.cluster.local")
                .dmaapPortNumber(3904)
                .dmaapProtocol("http")
                .dmaapUserName("admin")
                .dmaapUserPassword("admin")
                .trustStorePath("/opt/app/bbs/local/org.onap.bbs.trust.jks")
                .trustStorePasswordPath("change_it")
                .keyStorePath("/opt/app/bbs/local/org.onap.bbs.p12")
                .keyStorePasswordPath("change_it")
                .enableDmaapCertAuth(false)
                .dmaapTopicName("/events/unauthenticated.PNF_REREGISTRATION")
                .timeoutMs(-1)
                .messageLimit(-1)
                .build();
    }
}