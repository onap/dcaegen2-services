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
import static org.mockito.Mockito.doReturn;
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
import org.onap.bbs.event.processor.exceptions.DmaapException;
import org.onap.bbs.event.processor.model.ControlLoopPublisherDmaapModel;
import org.onap.bbs.event.processor.model.ImmutableControlLoopPublisherDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.ImmutableDmaapPublisherConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.DMaaPPublisherReactiveHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.PublisherReactiveHttpClientFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DmaapPublisherTaskImplTest {

    private static ControlLoopPublisherDmaapModel controlLoopPublisherDmaapModel;
    private static DmaapPublisherTaskImpl task;
    private static DMaaPPublisherReactiveHttpClient reactiveHttpClient;
    private static ApplicationConfiguration configuration;
    private static DmaapPublisherConfiguration dmaapPublisherConfiguration;

    @BeforeAll
    static void setUp() {
        dmaapPublisherConfiguration = testVersionOfDmaapPublisherConfiguration();
        configuration = mock(ApplicationConfiguration.class);

        final String closedLoopEventClient = "DCAE.BBS_mSInstance";
        final String policyVersion = "1.0.0.5";
        final String policyName = "CPE_Authentication";
        final String policyScope =
                "service=HSIAService,type=SampleType,"
                        + "closedLoopControlName=CL-CPE_A-d925ed73-8231-4d02-9545-db4e101f88f8";
        final String targetType = "VM";
        final long closedLoopAlarmStart = 1484677482204798L;
        final String closedLoopEventStatus = "ONSET";
        final String closedLoopControlName = "ControlLoop-CPE_A-2179b738-fd36-4843-a71a-a8c24c70c88b";
        final String version = "1.0.2";
        final String target = "vserver.vserver-name";
        final String requestId = "97964e10-686e-4790-8c45-bdfa61df770f";
        final String from = "DCAE";

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

        when(configuration.getDmaapPublisherConfiguration()).thenReturn(dmaapPublisherConfiguration);
    }

    @Test
    void passingNullMessage_ExceptionIsRaised() {

        task = new DmaapPublisherTaskImpl(configuration);

        Executable executableFunction = () -> task.execute(null);

        Assertions.assertThrows(DmaapException.class, executableFunction, "Input message is invalid");
    }

    @Test
    void passingNormalMessage_ReactiveClientProcessesIt() throws DmaapException {
        ResponseEntity<String> responseEntity = setupMocks(HttpStatus.OK.value());
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        StepVerifier.create(task.execute(controlLoopPublisherDmaapModel)).expectSubscription()
                .expectNext(responseEntity).verifyComplete();

        verify(reactiveHttpClient, times(1))
                .getDMaaPProducerResponse(controlLoopPublisherDmaapModel);
        verifyNoMoreInteractions(reactiveHttpClient);
    }

    @Test
    void passingNormalMessage_IncorrectResponseIsHandled() throws DmaapException {
        ResponseEntity<String> responseEntity = setupMocks(HttpStatus.UNAUTHORIZED.value());
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED);
        StepVerifier.create(task.execute(controlLoopPublisherDmaapModel)).expectSubscription()
                .expectNext(responseEntity).verifyComplete();

        verify(reactiveHttpClient, times(1))
                .getDMaaPProducerResponse(controlLoopPublisherDmaapModel);
        verifyNoMoreInteractions(reactiveHttpClient);
    }

    // We can safely suppress unchecked assignment warning here since it is a mock class
    @SuppressWarnings("unchecked")
    private ResponseEntity<String> setupMocks(Integer httpResponseCode) {

        ResponseEntity<String> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.valueOf(httpResponseCode));

        reactiveHttpClient = mock(DMaaPPublisherReactiveHttpClient.class);
        when(reactiveHttpClient.getDMaaPProducerResponse(any()))
                .thenReturn(Mono.just(responseEntity));

        PublisherReactiveHttpClientFactory httpClientFactory = mock(PublisherReactiveHttpClientFactory.class);
        doReturn(reactiveHttpClient).when(httpClientFactory).create(dmaapPublisherConfiguration);

        task = new DmaapPublisherTaskImpl(configuration, httpClientFactory);

        return responseEntity;
    }

    private static DmaapPublisherConfiguration testVersionOfDmaapPublisherConfiguration() {
        return new ImmutableDmaapPublisherConfiguration.Builder()
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
                .dmaapTopicName("/events/unauthenticated.DCAE_CL_OUTPUT")
                .build();
    }
}