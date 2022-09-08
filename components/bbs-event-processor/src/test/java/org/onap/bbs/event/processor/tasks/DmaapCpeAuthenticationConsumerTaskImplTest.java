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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.config.DmaapCpeAuthenticationConsumerProperties;
import org.onap.bbs.event.processor.exceptions.EmptyDmaapResponseException;
import org.onap.bbs.event.processor.model.CpeAuthenticationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.ImmutableCpeAuthenticationConsumerDmaapModel;
import org.onap.bbs.event.processor.utilities.CpeAuthenticationDmaapConsumerJsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class DmaapCpeAuthenticationConsumerTaskImplTest {

    private static final String DMAAP_PROTOCOL = "http";
    private static final String DMAAP_HOST = "message-router.onap.svc.cluster.local";
    private static final int DMAAP_PORT = 3904;
    private static final String DMAAP_TOPIC = "unauthenticated.CPE_AUTHENTICATION";
    private static final String SUBSCRIBER_ID = "subscriberID";
    private static final String SUBSCRIBER_GROUP = "subscriberGroup";

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
    private static String eventsArray;
    private static MessageRouterSubscriber subscriber;
    private static Gson gson = new Gson();
    private static ApplicationConfiguration configuration;

    @BeforeAll
    static void setUp() {

        // Mock Re-registration configuration
        configuration = mock(ApplicationConfiguration.class);
        var props = mock(DmaapCpeAuthenticationConsumerProperties.class);
        when(props.getDmaapProtocol()).thenReturn(DMAAP_PROTOCOL);
        when(props.getDmaapHostName()).thenReturn(DMAAP_HOST);
        when(props.getDmaapPortNumber()).thenReturn(DMAAP_PORT);
        when(props.getDmaapTopicName()).thenReturn(DMAAP_TOPIC);
        when(props.getConsumerId()).thenReturn(SUBSCRIBER_ID);
        when(props.getConsumerGroup()).thenReturn(SUBSCRIBER_GROUP);
        when(configuration.getDmaapCpeAuthenticationConsumerProperties()).thenReturn(props);

        var subscriberConfig = mock(MessageRouterSubscriberConfig.class);
        when(configuration.getDmaapCpeAuthenticationConsumerConfiguration()).thenReturn(subscriberConfig);

        var sourceName = "PNF-CorrelationId";
        var oldAuthenticationState = "outOfService";
        var newAuthenticationState = "inService";
        var stateInterface = "stateInterface";
        var rgwMacAddress = "00:0a:95:8d:78:16";
        var swVersion = "1.2";

        cpeAuthenticationConsumerDmaapModel = ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(sourceName)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rgwMacAddress(rgwMacAddress)
                .swVersion(swVersion)
                .build();

        var event = String.format(CPE_AUTHENTICATION_EVENT_TEMPLATE, sourceName, oldAuthenticationState,
                newAuthenticationState, stateInterface, rgwMacAddress, swVersion);

        eventsArray = "[" + event + "]";
    }

    @Test
    void passingEmptyMessage_NothingHappens() throws Exception {
        var empty = gson.toJsonTree("");
        subscriber = mock(MessageRouterSubscriber.class);
        when(subscriber.getElements(any())).thenReturn(Flux.just(empty));

        dmaapConsumerTask = new DmaapCpeAuthenticationConsumerTaskImpl(configuration, subscriber,
                new CpeAuthenticationDmaapConsumerJsonParser());

        StepVerifier.create(dmaapConsumerTask.execute("Sample input"))
                .expectSubscription()
                .expectError(EmptyDmaapResponseException.class);

        verify(subscriber, times(1)).getElements(any());
        verifyNoMoreInteractions(subscriber);
    }

    @Test
    void passingNormalMessage_ResponseSucceeds() throws Exception {
        var normalEventsArray = gson.toJsonTree(eventsArray);
        subscriber = mock(MessageRouterSubscriber.class);
        when(subscriber.getElements(any())).thenReturn(Flux.just(normalEventsArray));

        dmaapConsumerTask = new DmaapCpeAuthenticationConsumerTaskImpl(configuration, subscriber,
                new CpeAuthenticationDmaapConsumerJsonParser());

        StepVerifier.create(dmaapConsumerTask.execute("Sample input"))
                .expectSubscription()
                .consumeNextWith(e -> Assert.assertEquals(e, cpeAuthenticationConsumerDmaapModel));
        verify(subscriber, times(1)).getElements(any());
        verifyNoMoreInteractions(subscriber);
    }
}