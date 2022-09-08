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

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.config.DmaapProducerProperties;
import org.onap.bbs.event.processor.exceptions.DmaapException;
import org.onap.bbs.event.processor.model.ControlLoopPublisherDmaapModel;
import org.onap.bbs.event.processor.model.ImmutableControlLoopPublisherDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class DmaapPublisherTaskImplTest {

    private static final String DMAAP_PROTOCOL = "http";
    private static final String DMAAP_HOST = "message-router.onap.svc.cluster.local";
    private static final int DMAAP_PORT = 3904;
    private static final String DMAAP_TOPIC = "unauthenticated.DCAE_CL_OUTPUT";

    private static ControlLoopPublisherDmaapModel controlLoopPublisherDmaapModel;
    private static DmaapPublisherTaskImpl task;
    private static ApplicationConfiguration configuration;

    @BeforeAll
    static void setUp() {
        configuration = mock(ApplicationConfiguration.class);

        final var closedLoopEventClient = "DCAE.BBS_mSInstance";
        final var policyVersion = "1.0.0.5";
        final var policyName = "CPE_Authentication";
        final var policyScope =
                "service=HSIAService,type=SampleType,"
                        + "closedLoopControlName=CL-CPE_A-d925ed73-8231-4d02-9545-db4e101f88f8";
        final var targetType = "VM";
        final var closedLoopAlarmStart = 1484677482204798L;
        final var closedLoopEventStatus = "ONSET";
        final var closedLoopControlName = "ControlLoop-CPE_A-2179b738-fd36-4843-a71a-a8c24c70c88b";
        final var version = "1.0.2";
        final var target = "vserver.vserver-name";
        final var requestId = "97964e10-686e-4790-8c45-bdfa61df770f";
        final var from = "DCAE";

        final Map<String, String> aaiEnrichmentData = new LinkedHashMap<>();
        aaiEnrichmentData.put("service-information.service-instance-id", "service-instance-id-example");
        aaiEnrichmentData.put("cvlan-id", "example cvlan-id");
        aaiEnrichmentData.put("svlan-id", "example svlan-id");

        controlLoopPublisherDmaapModel = ImmutableControlLoopPublisherDmaapModel.builder()
                .closedLoopEventClient(closedLoopEventClient)
                .policyVersion(policyVersion)
                .policyName(policyName)
                .policyScope(policyScope)
                .targetType(targetType)
                .aaiEnrichmentData(aaiEnrichmentData)
                .closedLoopAlarmStart(closedLoopAlarmStart)
                .closedLoopEventStatus(closedLoopEventStatus)
                .closedLoopControlName(closedLoopControlName)
                .version(version)
                .target(target)
                .requestId(requestId)
                .originator(from)
                .build();
        var props = mock(DmaapProducerProperties.class);
        when(props.getDmaapProtocol()).thenReturn(DMAAP_PROTOCOL);
        when(props.getDmaapHostName()).thenReturn(DMAAP_HOST);
        when(props.getDmaapPortNumber()).thenReturn(DMAAP_PORT);
        when(props.getDmaapTopicName()).thenReturn(DMAAP_TOPIC);
        when(configuration.getDmaapProducerProperties()).thenReturn(props);

        var publisherConfig = mock(MessageRouterPublisherConfig.class);
        when(configuration.getDmaapPublisherConfiguration()).thenReturn(publisherConfig);


    }

    @Test
    void passingNullMessage_ExceptionIsRaised() {

        Executable executableFunction = () -> task.execute(null);

        Assertions.assertThrows(DmaapException.class, executableFunction, "Input message is invalid");
    }

    @Test
    void passingNormalMessage_ReactiveClientProcessesIt() throws DmaapException {
        var publisher = mock(MessageRouterPublisher.class);
        task = new DmaapPublisherTaskImpl(configuration, publisher);

        var response = mockResponse(true);
        when(publisher.put(any(),any())).thenReturn(Flux.just(response));

        StepVerifier.create(task.execute(controlLoopPublisherDmaapModel))
                .expectSubscription()
                .assertNext(r -> Assertions.assertTrue(r.successful()))
                .verifyComplete();

        verify(publisher, times(1)).put(any(),any());
        verifyNoMoreInteractions(publisher);
    }

    @Test
    void passingNormalMessage_IncorrectResponseIsHandled() throws DmaapException {
        var publisher = mock(MessageRouterPublisher.class);
        task = new DmaapPublisherTaskImpl(configuration, publisher);

        var response = mockResponse(false);
        when(publisher.put(any(),any())).thenReturn(Flux.just(response));

        StepVerifier.create(task.execute(controlLoopPublisherDmaapModel))
                .expectSubscription()
                .assertNext(r -> Assertions.assertFalse(r.successful()))
                .verifyComplete();

        verify(publisher, times(1)).put(any(),any());
        verifyNoMoreInteractions(publisher);
    }

    private MessageRouterPublishResponse mockResponse(boolean isSuccess) {
        var response = mock(MessageRouterPublishResponse.class);
        when(response.successful()).thenReturn(isSuccess);
        return response;
    }
}