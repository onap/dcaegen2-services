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

package org.onap.bbs.event.processor.pipelines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.onap.bbs.event.processor.config.ApplicationConstants.CONSUME_REREGISTRATION_TASK_NAME;
import static org.onap.bbs.event.processor.config.ApplicationConstants.RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME;
import static org.onap.bbs.event.processor.config.ApplicationConstants.RETRIEVE_PNF_TASK_NAME;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import javax.net.ssl.SSLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.exceptions.AaiTaskException;
import org.onap.bbs.event.processor.exceptions.EmptyDmaapResponseException;
import org.onap.bbs.event.processor.model.ControlLoopPublisherDmaapModel;
import org.onap.bbs.event.processor.model.ImmutableMetadataEntryAaiObject;
import org.onap.bbs.event.processor.model.ImmutableMetadataListAaiObject;
import org.onap.bbs.event.processor.model.ImmutablePnfAaiObject;
import org.onap.bbs.event.processor.model.ImmutablePropertyAaiObject;
import org.onap.bbs.event.processor.model.ImmutableReRegistrationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.ImmutableRelationshipDataEntryAaiObject;
import org.onap.bbs.event.processor.model.ImmutableRelationshipEntryAaiObject;
import org.onap.bbs.event.processor.model.ImmutableRelationshipListAaiObject;
import org.onap.bbs.event.processor.model.ImmutableServiceInstanceAaiObject;
import org.onap.bbs.event.processor.model.MetadataListAaiObject;
import org.onap.bbs.event.processor.model.PnfAaiObject;
import org.onap.bbs.event.processor.model.ReRegistrationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.RelationshipListAaiObject;
import org.onap.bbs.event.processor.model.ServiceInstanceAaiObject;
import org.onap.bbs.event.processor.tasks.AaiClientTask;
import org.onap.bbs.event.processor.tasks.DmaapPublisherTask;
import org.onap.bbs.event.processor.tasks.DmaapReRegistrationConsumerTask;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

// We can safely suppress unchecked assignment warnings for the ResponseEntity mock
@SuppressWarnings("unchecked")
@DisplayName("PNF Re-registration Pipeline Unit-Tests")
class ReRegistrationPipelineTest {

    private ReRegistrationPipeline pipeline;
    private ApplicationConfiguration configuration;
    private DmaapReRegistrationConsumerTask consumerTask;
    private DmaapPublisherTask publisherTask;
    private AaiClientTask aaiClientTask;

    private ResponseEntity<String> responseEntity;

    @BeforeEach
    void setup() {

        responseEntity = Mockito.mock(ResponseEntity.class);

        configuration = Mockito.mock(ApplicationConfiguration.class);
        consumerTask = Mockito.mock(DmaapReRegistrationConsumerTask.class);
        publisherTask = Mockito.mock(DmaapPublisherTask.class);
        aaiClientTask = Mockito.mock(AaiClientTask.class);

        when(configuration.getReRegistrationCloseLoopControlName())
                .thenReturn("controlName");
        when(configuration.getReRegistrationCloseLoopPolicyScope())
                .thenReturn("policyScope");
        when(configuration.getPolicyVersion())
                .thenReturn("1.0.0");
        when(configuration.getCloseLoopTargetType())
                .thenReturn("VM");
        when(configuration.getCloseLoopEventStatus())
                .thenReturn("ONSET");
        when(configuration.getCloseLoopVersion())
                .thenReturn("1.0.2");
        when(configuration.getCloseLoopTarget())
                .thenReturn("CL-Target");
        when(configuration.getCloseLoopOriginator())
                .thenReturn("DCAE-BBS-ep");

        pipeline = new ReRegistrationPipeline(configuration, consumerTask,
                publisherTask, aaiClientTask, new HashMap<>());
    }

    @Test
    void handleEmptyResponseFromDmaap() throws SSLException {

        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(10);
        when(consumerTask.execute(anyString()))
                .thenReturn(Flux.error(new EmptyDmaapResponseException("Mock empty")));

        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .verifyComplete();

        verifyZeroInteractions(aaiClientTask);
        verifyZeroInteractions(publisherTask);
    }

    @Test
    void noResponseFromDmaap_PipelineTimesOut() throws SSLException {

        // Prepare mocks
        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(1);
        when(consumerTask.execute(CONSUME_REREGISTRATION_TASK_NAME))
                .thenReturn(Flux.never());

        // Execute pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .verifyComplete();

        verifyZeroInteractions(aaiClientTask);
        verifyZeroInteractions(publisherTask);
    }

    @Test
    void noResponseFromAai_PipelineTimesOut() throws SSLException {

        String pnfName = "olt1";
        String attachmentPoint = "olt2-2-2";
        String remoteId = "newRemoteId";
        String cvlan = "1005";
        String svlan = "100";

        // Prepare stubbed replies
        ReRegistrationConsumerDmaapModel event = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(pnfName)
                .attachmentPoint(attachmentPoint)
                .remoteId(remoteId)
                .cVlan(cvlan)
                .sVlan(svlan)
                .build();

        // Prepare mocks
        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(1);
        when(consumerTask.execute(CONSUME_REREGISTRATION_TASK_NAME)).thenReturn(Flux.just(event));
        when(aaiClientTask.executePnfRetrieval(anyString(), anyString())).thenReturn(Mono.never());

        // Execute pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .verifyComplete();

        verifyZeroInteractions(publisherTask);
    }

    @Test
    void noResponseWhilePublishing_PipelineTimesOut() throws SSLException {

        String pnfName = "olt1";
        String attachmentPoint = "olt2-2-2";
        String remoteId = "newRemoteId";
        String cvlan = "1005";
        String svlan = "100";
        String hsiCfsServiceInstanceId = UUID.randomUUID().toString();

        // Prepare stubbed replies
        ReRegistrationConsumerDmaapModel event = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(pnfName)
                .attachmentPoint(attachmentPoint)
                .remoteId(remoteId)
                .cVlan(cvlan)
                .sVlan(svlan)
                .build();

        PnfAaiObject pnfAaiObject = constructPnfObject(pnfName, "olt1-1-1", hsiCfsServiceInstanceId);
        ServiceInstanceAaiObject hsiCfsServiceInstance =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId, pnfName, cvlan);

        // Prepare Mocks
        String cfsUrl = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance.getServiceInstanceId());

        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(1);
        when(consumerTask.execute(CONSUME_REREGISTRATION_TASK_NAME)).thenReturn(Flux.just(event));

        when(aaiClientTask.executePnfRetrieval(anyString(), anyString()))
                .thenReturn(Mono.just(pnfAaiObject));

        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl))
                .thenReturn(Mono.just(hsiCfsServiceInstance));

        when(publisherTask.execute(any(ControlLoopPublisherDmaapModel.class))).thenReturn(Mono.never());

        // Execute the pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .verifyComplete();

        verify(publisherTask).execute(any(ControlLoopPublisherDmaapModel.class));
    }

    @Test
    void singleCorrectEvent_PnfHavingNoLogicalLink_handleGracefully() throws SSLException {

        String pnfName = "olt1";
        String attachmentPoint = "olt2-2-2";
        String remoteId = "newRemoteId";
        String cvlan = "1005";
        String svlan = "100";
        String hsiCfsServiceInstanceId = UUID.randomUUID().toString();

        // Prepare stubbed replies
        ReRegistrationConsumerDmaapModel event = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(pnfName)
                .attachmentPoint(attachmentPoint)
                .remoteId(remoteId)
                .cVlan(cvlan)
                .sVlan(svlan)
                .build();

        PnfAaiObject pnfAaiObject = constructPnfObjectWithoutLogicalLink(pnfName, hsiCfsServiceInstanceId);
        ServiceInstanceAaiObject hsiCfsServiceInstance =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId, pnfName, cvlan);

        // Prepare Mocks
        String cfsUrl = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance.getServiceInstanceId());

        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(10);
        when(consumerTask.execute(CONSUME_REREGISTRATION_TASK_NAME)).thenReturn(Flux.just(event));

        when(aaiClientTask.executePnfRetrieval(anyString(), anyString()))
                .thenReturn(Mono.just(pnfAaiObject));

        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl))
                .thenReturn(Mono.just(hsiCfsServiceInstance));

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.valueOf(HttpStatus.OK.value()));
        when(publisherTask.execute(any(ControlLoopPublisherDmaapModel.class))).thenReturn(Mono.just(responseEntity));

        // Execute the pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .verifyComplete();

        verify(aaiClientTask).executePnfRetrieval(anyString(), anyString());
        verifyNoMoreInteractions(aaiClientTask);
        verifyZeroInteractions(publisherTask);
    }

    @Test
    void singleCorrectEvent_handleSuccessfully() throws SSLException {

        String pnfName = "olt1";
        String attachmentPoint = "olt2-2-2";
        String remoteId = "newRemoteId";
        String cvlan = "1005";
        String svlan = "100";
        String hsiCfsServiceInstanceId = UUID.randomUUID().toString();

        // Prepare stubbed replies
        ReRegistrationConsumerDmaapModel event = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(pnfName)
                .attachmentPoint(attachmentPoint)
                .remoteId(remoteId)
                .cVlan(cvlan)
                .sVlan(svlan)
                .build();

        PnfAaiObject pnfAaiObject = constructPnfObject(pnfName, "old-attachment-point", hsiCfsServiceInstanceId);
        ServiceInstanceAaiObject hsiCfsServiceInstance =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId, pnfName, cvlan);

        // Prepare Mocks
        String cfsUrl = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance.getServiceInstanceId());

        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(10);
        when(consumerTask.execute(CONSUME_REREGISTRATION_TASK_NAME)).thenReturn(Flux.just(event));

        when(aaiClientTask.executePnfRetrieval(anyString(), anyString()))
                .thenReturn(Mono.just(pnfAaiObject));

        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl))
                .thenReturn(Mono.just(hsiCfsServiceInstance));

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.valueOf(HttpStatus.OK.value()));
        when(publisherTask.execute(any(ControlLoopPublisherDmaapModel.class))).thenReturn(Mono.just(responseEntity));

        // Execute the pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .assertNext(r -> assertEquals(HttpStatus.OK, r.getStatusCode()))
                .verifyComplete();

        verify(publisherTask).execute(any(ControlLoopPublisherDmaapModel.class));
    }

    @Test
    void twoCorrectEvents_handleSuccessfully() throws SSLException {

        String pnfName1 = "olt1";
        String pnfName2 = "olt2";
        String attachmentPoint1 = "olt1-1-1";
        String attachmentPoint2 = "olt2-2-2";
        String remoteId1 = "newRemoteId1";
        String remoteId2 = "newRemoteId2";
        String cvlan1 = "1005";
        String cvlan2 = "1006";
        String svlan = "100";
        String hsiCfsServiceInstanceId1 = UUID.randomUUID().toString();
        String hsiCfsServiceInstanceId2 = UUID.randomUUID().toString();

        // Prepare stubbed replies
        ReRegistrationConsumerDmaapModel firstEvent = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(pnfName1)
                .attachmentPoint(attachmentPoint1)
                .remoteId(remoteId1)
                .cVlan(cvlan1)
                .sVlan(svlan)
                .build();
        ReRegistrationConsumerDmaapModel secondEvent = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(pnfName2)
                .attachmentPoint(attachmentPoint2)
                .remoteId(remoteId2)
                .cVlan(cvlan2)
                .sVlan(svlan)
                .build();

        PnfAaiObject pnfAaiObject1 = constructPnfObject(pnfName1, "olt1-1-0", hsiCfsServiceInstanceId1);
        PnfAaiObject pnfAaiObject2 = constructPnfObject(pnfName2, "olt2-2-0", hsiCfsServiceInstanceId2);
        ServiceInstanceAaiObject hsiCfsServiceInstance1 =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId1, pnfName1, cvlan1);
        ServiceInstanceAaiObject hsiCfsServiceInstance2 =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId2, pnfName2, cvlan2);

        // Prepare Mocks
        String pnfUrl1 = String.format("/aai/v14/network/pnfs/pnf/%s?depth=all", pnfName1);
        String pnfUrl2 = String.format("/aai/v14/network/pnfs/pnf/%s?depth=all", pnfName2);
        String cfsUrl1 = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance1.getServiceInstanceId());
        String cfsUrl2 = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance2.getServiceInstanceId());

        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(10);
        when(consumerTask.execute(CONSUME_REREGISTRATION_TASK_NAME))
                .thenReturn(Flux.fromIterable(Arrays.asList(firstEvent, secondEvent)));

        when(aaiClientTask.executePnfRetrieval(RETRIEVE_PNF_TASK_NAME, pnfUrl1)).thenReturn(Mono.just(pnfAaiObject1));
        when(aaiClientTask.executePnfRetrieval(RETRIEVE_PNF_TASK_NAME, pnfUrl2)).thenReturn(Mono.just(pnfAaiObject2));

        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl1))
                .thenReturn(Mono.just(hsiCfsServiceInstance1));
        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl2))
                .thenReturn(Mono.just(hsiCfsServiceInstance2));

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.valueOf(HttpStatus.OK.value()));
        when(publisherTask.execute(any(ControlLoopPublisherDmaapModel.class))).thenReturn(Mono.just(responseEntity));

        // Execute the pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .assertNext(r -> assertEquals(HttpStatus.OK, r.getStatusCode()))
                .assertNext(r -> assertEquals(HttpStatus.OK, r.getStatusCode()))
                .verifyComplete();

        verify(publisherTask, times(2)).execute(any(ControlLoopPublisherDmaapModel.class));
    }

    @Test
    void singleEvent_withPnfErrorReply_handleGracefully() throws SSLException {

        String pnfName = "olt1";
        String attachmentPoint = "olt2-2-2";
        String remoteId = "newRemoteId";
        String cvlan = "1005";
        String svlan = "100";

        // Prepare stubbed replies
        ReRegistrationConsumerDmaapModel event = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(pnfName)
                .attachmentPoint(attachmentPoint)
                .remoteId(remoteId)
                .cVlan(cvlan)
                .sVlan(svlan)
                .build();

        // Prepare Mocks
        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(10);
        when(consumerTask.execute(CONSUME_REREGISTRATION_TASK_NAME)).thenReturn(Flux.just(event));
        when(aaiClientTask.executePnfRetrieval(anyString(), anyString()))
                .thenReturn(Mono.error(new AaiTaskException("Mock A&AI exception")));

        // Execute the pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .verifyComplete();

        verify(aaiClientTask).executePnfRetrieval(anyString(), anyString());
        verifyNoMoreInteractions(aaiClientTask);
        verifyZeroInteractions(publisherTask);
    }

    @Test
    void twoEvents_FirstOk_SecondNotRelocation_handleCorrectOnly() throws SSLException {

        String pnfName1 = "olt1";
        String pnfName2 = "olt2";
        String attachmentPoint1 = "olt1-1-1";
        String attachmentPoint2 = "olt2-2-2";
        String remoteId1 = "newRemoteId1";
        String remoteId2 = "newRemoteId2";
        String cvlan1 = "1005";
        String cvlan2 = "1006";
        String svlan = "100";
        String hsiCfsServiceInstanceId1 = UUID.randomUUID().toString();
        String hsiCfsServiceInstanceId2 = UUID.randomUUID().toString();

        // Prepare stubbed replies
        ReRegistrationConsumerDmaapModel firstEvent = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(pnfName1)
                .attachmentPoint(attachmentPoint1)
                .remoteId(remoteId1)
                .cVlan(cvlan1)
                .sVlan(svlan)
                .build();
        ReRegistrationConsumerDmaapModel secondEvent = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(pnfName2)
                .attachmentPoint(attachmentPoint2)
                .remoteId(remoteId2)
                .cVlan(cvlan2)
                .sVlan(svlan)
                .build();

        PnfAaiObject pnfAaiObject1 = constructPnfObject(pnfName1, "olt1-1-0", hsiCfsServiceInstanceId1);
        PnfAaiObject pnfAaiObject2 = constructPnfObject(pnfName2, attachmentPoint2, hsiCfsServiceInstanceId2);
        ServiceInstanceAaiObject hsiCfsServiceInstance1 =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId1, pnfName1, cvlan1);
        ServiceInstanceAaiObject hsiCfsServiceInstance2 =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId2, pnfName2, cvlan2);

        // Prepare Mocks
        String pnfUrl1 = String.format("/aai/v14/network/pnfs/pnf/%s?depth=all", pnfName1);
        String pnfUrl2 = String.format("/aai/v14/network/pnfs/pnf/%s?depth=all", pnfName2);
        String cfsUrl1 = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance1.getServiceInstanceId());
        String cfsUrl2 = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance2.getServiceInstanceId());

        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(10);
        when(consumerTask.execute(CONSUME_REREGISTRATION_TASK_NAME))
                .thenReturn(Flux.fromIterable(Arrays.asList(firstEvent, secondEvent)));

        when(aaiClientTask.executePnfRetrieval(RETRIEVE_PNF_TASK_NAME, pnfUrl1)).thenReturn(Mono.just(pnfAaiObject1));
        when(aaiClientTask.executePnfRetrieval(RETRIEVE_PNF_TASK_NAME, pnfUrl2)).thenReturn(Mono.just(pnfAaiObject2));

        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl1))
                .thenReturn(Mono.just(hsiCfsServiceInstance1));
        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl2))
                .thenReturn(Mono.just(hsiCfsServiceInstance2));

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.valueOf(HttpStatus.OK.value()));
        when(publisherTask.execute(any(ControlLoopPublisherDmaapModel.class))).thenReturn(Mono.just(responseEntity));

        // Execute the pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .assertNext(r -> assertEquals(HttpStatus.OK, r.getStatusCode()))
                .verifyComplete();

        verify(publisherTask).execute(any(ControlLoopPublisherDmaapModel.class));
    }

    @Test
    void twoEvents_firstOk_secondWithPnfErrorReply_handleCorrectOnly() throws SSLException {

        String pnfName1 = "olt1";
        String pnfName2 = "olt2";
        String attachmentPoint1 = "olt1-1-1";
        String attachmentPoint2 = "olt2-2-2";
        String remoteId1 = "newRemoteId1";
        String remoteId2 = "newRemoteId2";
        String cvlan1 = "1005";
        String cvlan2 = "1006";
        String svlan = "100";
        String hsiCfsServiceInstanceId = UUID.randomUUID().toString();

        // Prepare stubbed replies
        ReRegistrationConsumerDmaapModel firstEvent = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(pnfName1)
                .attachmentPoint(attachmentPoint1)
                .remoteId(remoteId1)
                .cVlan(cvlan1)
                .sVlan(svlan)
                .build();
        ReRegistrationConsumerDmaapModel secondEvent = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(pnfName2)
                .attachmentPoint(attachmentPoint2)
                .remoteId(remoteId2)
                .cVlan(cvlan2)
                .sVlan(svlan)
                .build();

        PnfAaiObject pnfAaiObject = constructPnfObject(pnfName1, "old-attachment-point", hsiCfsServiceInstanceId);
        ServiceInstanceAaiObject hsiCfsServiceInstance =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId, pnfName1, cvlan1);

        // Prepare Mocks
        String cfsUrl = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance.getServiceInstanceId());

        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(10);
        when(consumerTask.execute(CONSUME_REREGISTRATION_TASK_NAME))
                .thenReturn(Flux.fromIterable(Arrays.asList(firstEvent, secondEvent)));
        when(aaiClientTask.executePnfRetrieval(anyString(), anyString()))
                .thenReturn(Mono.just(pnfAaiObject))
                .thenReturn(Mono.error(new AaiTaskException("Mock A&AI exception")));
        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl))
                .thenReturn(Mono.just(hsiCfsServiceInstance));

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.valueOf(HttpStatus.OK.value()));
        when(publisherTask.execute(any(ControlLoopPublisherDmaapModel.class))).thenReturn(Mono.just(responseEntity));

        // Execute the pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .assertNext(r -> assertEquals(HttpStatus.OK, r.getStatusCode()))
                .verifyComplete();

        verify(aaiClientTask, times(2)).executePnfRetrieval(anyString(), anyString());
        verify(aaiClientTask).executeServiceInstanceRetrieval(anyString(), anyString());
        verify(publisherTask).execute(any(ControlLoopPublisherDmaapModel.class));
    }

    @Test
    void twoEvents_firstWithPnfErrorReply_secondOk_handleCorrectOnly() throws SSLException {

        String pnfName1 = "olt1";
        String pnfName2 = "olt2";
        String attachmentPoint1 = "olt1-1-1";
        String attachmentPoint2 = "olt2-2-2";
        String remoteId1 = "newRemoteId1";
        String remoteId2 = "newRemoteId2";
        String cvlan1 = "1005";
        String cvlan2 = "1006";
        String svlan = "100";
        String hsiCfsServiceInstanceId = UUID.randomUUID().toString();

        // Prepare stubbed replies
        ReRegistrationConsumerDmaapModel firstEvent = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(pnfName1)
                .attachmentPoint(attachmentPoint1)
                .remoteId(remoteId1)
                .cVlan(cvlan1)
                .sVlan(svlan)
                .build();
        ReRegistrationConsumerDmaapModel secondEvent = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(pnfName2)
                .attachmentPoint(attachmentPoint2)
                .remoteId(remoteId2)
                .cVlan(cvlan2)
                .sVlan(svlan)
                .build();

        PnfAaiObject pnfAaiObject = constructPnfObject(pnfName2, "old-attachment-point", hsiCfsServiceInstanceId);
        ServiceInstanceAaiObject hsiCfsServiceInstance =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId, pnfName2, cvlan2);

        // Prepare Mocks
        String cfsUrl = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance.getServiceInstanceId());

        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(10);
        when(consumerTask.execute(CONSUME_REREGISTRATION_TASK_NAME))
                .thenReturn(Flux.fromIterable(Arrays.asList(firstEvent, secondEvent)));
        when(aaiClientTask.executePnfRetrieval(anyString(), anyString()))
                .thenReturn(Mono.error(new AaiTaskException("Mock A&AI exception")))
                .thenReturn(Mono.just(pnfAaiObject));
        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl))
                .thenReturn(Mono.just(hsiCfsServiceInstance));

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.valueOf(HttpStatus.OK.value()));
        when(publisherTask.execute(any(ControlLoopPublisherDmaapModel.class))).thenReturn(Mono.just(responseEntity));

        // Execute the pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .assertNext(r -> assertEquals(HttpStatus.OK, r.getStatusCode()))
                .verifyComplete();

        verify(aaiClientTask, times(2))
                .executePnfRetrieval(anyString(), anyString());
        verify(aaiClientTask).executeServiceInstanceRetrieval(anyString(), anyString());
        verify(publisherTask).execute(any(ControlLoopPublisherDmaapModel.class));
    }

    private PnfAaiObject constructPnfObject(String pnfName, String attachmentPoint,
                                            String hsiCfsServiceInstanceId) {

        // Build Relationship Data
        RelationshipListAaiObject.RelationshipEntryAaiObject firstRelationshipEntry =
                ImmutableRelationshipEntryAaiObject.builder()
                        .relatedTo("service-instance")
                        .relatedLink("/aai/v14/business/customers/customer/Demonstration/service-subscriptions"
                                + "/service-subscription/BBS-CFS/service-instances"
                                + "/service-instance/" + hsiCfsServiceInstanceId)
                        .relationshipLabel("org.onap.relationships.inventory.ComposedOf")
                        .relationshipData(Arrays.asList(
                                ImmutableRelationshipDataEntryAaiObject.builder()
                                        .relationshipKey("customer.global-customer-id")
                                        .relationshipValue("Demonstration").build(),
                                ImmutableRelationshipDataEntryAaiObject.builder()
                                        .relationshipKey("service-subscription.service-type")
                                        .relationshipValue("BBS-CFS").build(),
                                ImmutableRelationshipDataEntryAaiObject.builder()
                                        .relationshipKey("service-instance.service-instance-id")
                                        .relationshipValue(hsiCfsServiceInstanceId).build())
                        )
                        .relatedToProperties(Collections.singletonList(
                                ImmutablePropertyAaiObject.builder()
                                        .propertyKey("service-instance.service-instance-name")
                                        .propertyValue("bbs-instance").build())
                        )
                        .build();

        RelationshipListAaiObject.RelationshipEntryAaiObject secondRelationshipEntry =
                ImmutableRelationshipEntryAaiObject.builder()
                        .relatedTo("logical-link")
                        .relatedLink("/network/logical-links/logical-link/" + attachmentPoint)
                        .relationshipData(Collections.singletonList(ImmutableRelationshipDataEntryAaiObject.builder()
                                .relationshipKey("logical-link.link-name")
                                .relationshipValue(attachmentPoint).build()))
                        .build();

        RelationshipListAaiObject relationshipListAaiObject = ImmutableRelationshipListAaiObject.builder()
                .relationshipEntries(Arrays.asList(firstRelationshipEntry, secondRelationshipEntry))
                .build();

        // Finally construct PNF object data
        return ImmutablePnfAaiObject.builder()
                .pnfName(pnfName)
                .isInMaintenance(true)
                .relationshipListAaiObject(relationshipListAaiObject)
                .build();
    }

    private PnfAaiObject constructPnfObjectWithoutLogicalLink(String pnfName, String hsiCfsServiceInstanceId) {

        // Build Relationship Data
        RelationshipListAaiObject.RelationshipEntryAaiObject relationshipEntry =
                ImmutableRelationshipEntryAaiObject.builder()
                        .relatedTo("service-instance")
                        .relatedLink("/aai/v14/business/customers/customer/Demonstration/service-subscriptions"
                                + "/service-subscription/BBS-CFS/service-instances"
                                + "/service-instance/" + hsiCfsServiceInstanceId)
                        .relationshipLabel("org.onap.relationships.inventory.ComposedOf")
                        .relationshipData(Arrays.asList(
                                ImmutableRelationshipDataEntryAaiObject.builder()
                                        .relationshipKey("customer.global-customer-id")
                                        .relationshipValue("Demonstration").build(),
                                ImmutableRelationshipDataEntryAaiObject.builder()
                                        .relationshipKey("service-subscription.service-type")
                                        .relationshipValue("BBS-CFS").build(),
                                ImmutableRelationshipDataEntryAaiObject.builder()
                                        .relationshipKey("service-instance.service-instance-id")
                                        .relationshipValue(hsiCfsServiceInstanceId).build())
                        )
                        .relatedToProperties(Collections.singletonList(
                                ImmutablePropertyAaiObject.builder()
                                        .propertyKey("service-instance.service-instance-name")
                                        .propertyValue("bbs-instance").build())
                        )
                        .build();

        RelationshipListAaiObject relationshipListAaiObject = ImmutableRelationshipListAaiObject.builder()
                .relationshipEntries(Collections.singletonList(relationshipEntry))
                .build();

        // Finally construct PNF object data
        return ImmutablePnfAaiObject.builder()
                .pnfName(pnfName)
                .isInMaintenance(true)
                .relationshipListAaiObject(relationshipListAaiObject)
                .build();
    }

    private ServiceInstanceAaiObject constructHsiCfsServiceInstanceObject(String hsiCfsServiceInstanceId,
                                                                             String pnfName,
                                                                             String cvlan) {
        String orchestrationStatus = "active";

        RelationshipListAaiObject.RelationshipEntryAaiObject relationshipEntry =
                ImmutableRelationshipEntryAaiObject.builder()
                        .relatedTo("pnf")
                        .relatedLink("/pnfs/pnf/" + pnfName)
                        .relationshipData(Collections.singletonList(ImmutableRelationshipDataEntryAaiObject.builder()
                                .relationshipKey("pnf.pnf-name")
                                .relationshipValue(pnfName).build()))
                        .build();

        RelationshipListAaiObject relationshipListAaiObject = ImmutableRelationshipListAaiObject.builder()
                .relationshipEntries(Collections.singletonList(relationshipEntry))
                .build();

        MetadataListAaiObject.MetadataEntryAaiObject metadataEntry =
                ImmutableMetadataEntryAaiObject.builder()
                        .metaname("cvlan")
                        .metavalue(cvlan)
                        .build();

        MetadataListAaiObject metadataListAaiObject = ImmutableMetadataListAaiObject.builder()
                .metadataEntries(Collections.singletonList(metadataEntry))
                .build();

        // Finally construct Service Instance object data
        return ImmutableServiceInstanceAaiObject.builder()
                .serviceInstanceId(hsiCfsServiceInstanceId)
                .orchestrationStatus(orchestrationStatus)
                .relationshipListAaiObject(relationshipListAaiObject)
                .metadataListAaiObject(metadataListAaiObject)
                .build();
    }
}