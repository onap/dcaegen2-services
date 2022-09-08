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
import org.onap.bbs.event.processor.config.DmaapReRegistrationConsumerProperties;
import org.onap.bbs.event.processor.exceptions.EmptyDmaapResponseException;
import org.onap.bbs.event.processor.model.ImmutableReRegistrationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.ReRegistrationConsumerDmaapModel;
import org.onap.bbs.event.processor.utilities.ReRegistrationDmaapConsumerJsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class DmaapReRegistrationConsumerTaskImplTest {

    private static final String DMAAP_PROTOCOL = "http";
    private static final String DMAAP_HOST = "message-router.onap.svc.cluster.local";
    private static final int DMAAP_PORT = 3904;
    private static final String DMAAP_TOPIC = "unauthenticated.PNF_REREGISTRATION";
    private static final String SUBSCRIBER_ID = "subscriberID";
    private static final String SUBSCRIBER_GROUP = "subscriberGroup";

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
    private static String eventsArray;
    private static MessageRouterSubscriber subscriber;
    private static Gson gson = new Gson();
    private static ApplicationConfiguration configuration;

    @BeforeAll
    static void setUp() {

        // Mock Re-registration configuration
        configuration = mock(ApplicationConfiguration.class);
        var props = mock(DmaapReRegistrationConsumerProperties.class);
        when(props.getDmaapProtocol()).thenReturn(DMAAP_PROTOCOL);
        when(props.getDmaapHostName()).thenReturn(DMAAP_HOST);
        when(props.getDmaapPortNumber()).thenReturn(DMAAP_PORT);
        when(props.getDmaapTopicName()).thenReturn(DMAAP_TOPIC);
        when(props.getConsumerId()).thenReturn(SUBSCRIBER_ID);
        when(props.getConsumerGroup()).thenReturn(SUBSCRIBER_GROUP);
        when(configuration.getDmaapReRegistrationConsumerProperties()).thenReturn(props);

        var subscriberConfig = mock(MessageRouterSubscriberConfig.class);
        when(configuration.getDmaapReRegistrationConsumerConfiguration()).thenReturn(subscriberConfig);

        var sourceName = "PNF-CorrelationId";
        var attachmentPoint = "olt2/2/2";
        var remoteId = "remoteId";
        var cvlan = "1005";
        var svlan = "100";

        reRegistrationConsumerDmaapModel = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(sourceName)
                .attachmentPoint(attachmentPoint)
                .remoteId(remoteId)
                .cVlan(cvlan)
                .sVlan(svlan)
                .build();

        var event = String.format(RE_REGISTRATION_EVENT_TEMPLATE, sourceName, attachmentPoint, remoteId,
                cvlan, svlan);

        eventsArray = "[" + event + "]";
    }

    @Test
    void passingEmptyMessage_NothingHappens() {
        var empty = gson.toJsonTree("");
        subscriber = mock(MessageRouterSubscriber.class);
        when(subscriber.getElements(any())).thenReturn(Flux.just(empty));

        dmaapConsumerTask = new DmaapReRegistrationConsumerTaskImpl(configuration, subscriber,
                new ReRegistrationDmaapConsumerJsonParser());

        StepVerifier.create(dmaapConsumerTask.execute("Sample input"))
                .expectSubscription()
                .expectError(EmptyDmaapResponseException.class);

        verify(subscriber, times(1)).getElements(any());
        verifyNoMoreInteractions(subscriber);
    }

    @Test
    void passingNormalMessage_ResponseSucceeds() {
        System.out.println("Events sent : " + eventsArray);
        var normalEventsArray = gson.toJsonTree(eventsArray);
        subscriber = mock(MessageRouterSubscriber.class);
        when(subscriber.getElements(any())).thenReturn(Flux.just(normalEventsArray));

        dmaapConsumerTask = new DmaapReRegistrationConsumerTaskImpl(configuration, subscriber,
                new ReRegistrationDmaapConsumerJsonParser());

        StepVerifier.create(dmaapConsumerTask.execute("Sample input"))
                .expectSubscription()
                .consumeNextWith(e -> Assert.assertEquals(e, reRegistrationConsumerDmaapModel));
        verify(subscriber, times(1)).getElements(any());
        verifyNoMoreInteractions(subscriber);
    }
}