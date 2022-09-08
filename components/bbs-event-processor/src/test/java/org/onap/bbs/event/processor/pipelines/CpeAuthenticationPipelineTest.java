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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.onap.bbs.event.processor.config.ApplicationConstants.CONSUME_CPE_AUTHENTICATION_TASK_NAME;
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
import org.onap.bbs.event.processor.model.CpeAuthenticationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.ImmutableCpeAuthenticationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.ImmutableMetadataEntryAaiObject;
import org.onap.bbs.event.processor.model.ImmutableMetadataListAaiObject;
import org.onap.bbs.event.processor.model.ImmutablePnfAaiObject;
import org.onap.bbs.event.processor.model.ImmutablePropertyAaiObject;
import org.onap.bbs.event.processor.model.ImmutableRelationshipDataEntryAaiObject;
import org.onap.bbs.event.processor.model.ImmutableRelationshipEntryAaiObject;
import org.onap.bbs.event.processor.model.ImmutableRelationshipListAaiObject;
import org.onap.bbs.event.processor.model.ImmutableServiceInstanceAaiObject;
import org.onap.bbs.event.processor.model.MetadataListAaiObject;
import org.onap.bbs.event.processor.model.PnfAaiObject;
import org.onap.bbs.event.processor.model.RelationshipListAaiObject;
import org.onap.bbs.event.processor.model.ServiceInstanceAaiObject;
import org.onap.bbs.event.processor.tasks.AaiClientTask;
import org.onap.bbs.event.processor.tasks.DmaapCpeAuthenticationConsumerTask;
import org.onap.bbs.event.processor.tasks.DmaapPublisherTask;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("CPE Authentication Pipeline Unit-Tests")
class CpeAuthenticationPipelineTest {

    private CpeAuthenticationPipeline pipeline;
    private ApplicationConfiguration configuration;
    private DmaapCpeAuthenticationConsumerTask consumerTask;
    private DmaapPublisherTask publisherTask;
    private AaiClientTask aaiClientTask;

    private MessageRouterPublishResponse publishResponse;

    @BeforeEach
    void setup() {

        publishResponse = Mockito.mock(MessageRouterPublishResponse.class);

        configuration = Mockito.mock(ApplicationConfiguration.class);
        consumerTask = Mockito.mock(DmaapCpeAuthenticationConsumerTask.class);
        publisherTask = Mockito.mock(DmaapPublisherTask.class);
        aaiClientTask = Mockito.mock(AaiClientTask.class);

        when(configuration.getCpeAuthenticationCloseLoopControlName())
                .thenReturn("controlName");
        when(configuration.getCpeAuthenticationCloseLoopPolicyScope())
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

        pipeline = new CpeAuthenticationPipeline(configuration, consumerTask,
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
        when(consumerTask.execute(CONSUME_CPE_AUTHENTICATION_TASK_NAME))
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

        var pnfName = "olt1";
        var oldAuthenticationState = "outOfService";
        var newAuthenticationState = "inService";
        var stateInterface = "stateInterface";
        var rgwMacAddress = "00:0a:95:8d:78:16";
        var swVersion = "1.2";

        // Prepare stubbed replies
        CpeAuthenticationConsumerDmaapModel event = ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(pnfName)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rgwMacAddress(rgwMacAddress)
                .swVersion(swVersion)
                .build();

        // Prepare mocks
        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(1);
        when(consumerTask.execute(CONSUME_CPE_AUTHENTICATION_TASK_NAME)).thenReturn(Flux.just(event));
        when(aaiClientTask.executePnfRetrieval(anyString(), anyString())).thenReturn(Mono.never());

        // Execute pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .verifyComplete();

        verifyZeroInteractions(publisherTask);
    }

    @Test
    void noResponseWhilePublishing_PipelineTimesOut() throws SSLException {

        var pnfName = "olt1";
        var oldAuthenticationState = "outOfService";
        var newAuthenticationState = "inService";
        var stateInterface = "stateInterface";
        var rgwMacAddress = "00:0a:95:8d:78:16";
        var swVersion = "1.2";
        var hsiCfsServiceInstanceId = UUID.randomUUID().toString();

        // Prepare stubbed replies
        CpeAuthenticationConsumerDmaapModel event = ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(pnfName)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rgwMacAddress(rgwMacAddress)
                .swVersion(swVersion)
                .build();

        var pnfAaiObject = constructPnfObject(pnfName, hsiCfsServiceInstanceId);
        var hsiCfsServiceInstance =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId, pnfName, rgwMacAddress);

        // Prepare Mocks
        var cfsUrl = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance.getServiceInstanceId());

        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(1);
        when(consumerTask.execute(CONSUME_CPE_AUTHENTICATION_TASK_NAME)).thenReturn(Flux.just(event));

        when(aaiClientTask.executePnfRetrieval(anyString(), anyString()))
                .thenReturn(Mono.just(pnfAaiObject));

        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl))
                .thenReturn(Mono.just(hsiCfsServiceInstance));

        when(publisherTask.execute(any(ControlLoopPublisherDmaapModel.class))).thenReturn(Flux.never());

        // Execute the pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .verifyComplete();

        verify(publisherTask).execute(any(ControlLoopPublisherDmaapModel.class));
    }

    @Test
    void singleCorrectEvent_handleSuccessfully() throws SSLException {

        var pnfName = "olt1";
        var oldAuthenticationState = "outOfService";
        var newAuthenticationState = "inService";
        var stateInterface = "stateInterface";
        var rgwMacAddress = "00:0a:95:8d:78:16";
        var swVersion = "1.2";
        var hsiCfsServiceInstanceId = UUID.randomUUID().toString();

        // Prepare stubbed replies
        CpeAuthenticationConsumerDmaapModel event = ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(pnfName)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rgwMacAddress(rgwMacAddress)
                .swVersion(swVersion)
                .build();

        var pnfAaiObject = constructPnfObject(pnfName, hsiCfsServiceInstanceId);
        var hsiCfsServiceInstance =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId, pnfName, rgwMacAddress);

        // Prepare Mocks
        var cfsUrl = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance.getServiceInstanceId());

        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(10);
        when(consumerTask.execute(CONSUME_CPE_AUTHENTICATION_TASK_NAME)).thenReturn(Flux.just(event));

        when(aaiClientTask.executePnfRetrieval(anyString(), anyString()))
                .thenReturn(Mono.just(pnfAaiObject));

        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl))
                .thenReturn(Mono.just(hsiCfsServiceInstance));

        when(publishResponse.successful()).thenReturn(true);
        when(publisherTask.execute(any(ControlLoopPublisherDmaapModel.class))).thenReturn(Flux.just(publishResponse));

        // Execute the pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .assertNext(r -> assertTrue(r.successful()))
                .verifyComplete();

        verify(publisherTask).execute(any(ControlLoopPublisherDmaapModel.class));
    }

    @Test
    void twoCorrectEvents_handleSuccessfully() throws SSLException {

        var pnfName1 = "olt1";
        var pnfName2 = "olt2";
        var oldAuthenticationState = "outOfService";
        var newAuthenticationState = "inService";
        var stateInterface = "stateInterface";
        var rgwMacAddress1 = "00:0a:95:8d:78:16";
        var rgwMacAddress2 = "00:0a:95:8d:78:17";
        var swVersion = "1.2";
        var hsiCfsServiceInstanceId1 = UUID.randomUUID().toString();
        var hsiCfsServiceInstanceId2 = UUID.randomUUID().toString();

        // Prepare stubbed replies
        CpeAuthenticationConsumerDmaapModel firstEvent = ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(pnfName1)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rgwMacAddress(rgwMacAddress1)
                .swVersion(swVersion)
                .build();
        CpeAuthenticationConsumerDmaapModel secondEvent = ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(pnfName2)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rgwMacAddress(rgwMacAddress2)
                .swVersion(swVersion)
                .build();

        var pnfAaiObject1 = constructPnfObject(pnfName1, hsiCfsServiceInstanceId1);
        var pnfAaiObject2 = constructPnfObject(pnfName2, hsiCfsServiceInstanceId2);
        var hsiCfsServiceInstance1 =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId1, pnfName1, rgwMacAddress1);
        var hsiCfsServiceInstance2 =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId2, pnfName2, rgwMacAddress2);

        // Prepare Mocks
        var pnfUrl1 = String.format("/aai/v14/network/pnfs/pnf/%s?depth=all", pnfName1);
        var pnfUrl2 = String.format("/aai/v14/network/pnfs/pnf/%s?depth=all", pnfName2);
        var cfsUrl1 = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance1.getServiceInstanceId());
        var cfsUrl2 = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance2.getServiceInstanceId());

        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(10);
        when(consumerTask.execute(CONSUME_CPE_AUTHENTICATION_TASK_NAME))
                .thenReturn(Flux.fromIterable(Arrays.asList(firstEvent, secondEvent)));

        when(aaiClientTask.executePnfRetrieval(RETRIEVE_PNF_TASK_NAME, pnfUrl1)).thenReturn(Mono.just(pnfAaiObject1));
        when(aaiClientTask.executePnfRetrieval(RETRIEVE_PNF_TASK_NAME, pnfUrl2)).thenReturn(Mono.just(pnfAaiObject2));

        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl1))
                .thenReturn(Mono.just(hsiCfsServiceInstance1));
        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl2))
                .thenReturn(Mono.just(hsiCfsServiceInstance2));

        when(publishResponse.successful()).thenReturn(true);
        when(publisherTask.execute(any(ControlLoopPublisherDmaapModel.class))).thenReturn(Flux.just(publishResponse));

        // Execute the pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .assertNext(r -> assertTrue(r.successful()))
                .assertNext(r -> assertTrue(r.successful()))
                .verifyComplete();

        verify(publisherTask, times(2)).execute(any(ControlLoopPublisherDmaapModel.class));
    }

    @Test
    void singleEvent_withPnfErrorReply_handleGracefully() throws SSLException {

        var pnfName = "olt1";
        var oldAuthenticationState = "outOfService";
        var newAuthenticationState = "inService";
        var stateInterface = "stateInterface";
        var rgwMacAddress = "00:0a:95:8d:78:16";
        var swVersion = "1.2";

        // Prepare stubbed replies
        CpeAuthenticationConsumerDmaapModel event = ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(pnfName)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rgwMacAddress(rgwMacAddress)
                .swVersion(swVersion)
                .build();

        // Prepare Mocks
        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(10);
        when(consumerTask.execute(CONSUME_CPE_AUTHENTICATION_TASK_NAME)).thenReturn(Flux.just(event));
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
    void twoEvents_firstOk_secondWithPnfErrorReply_handleCorrectOnly() throws SSLException {

        var pnfName1 = "olt1";
        var pnfName2 = "olt2";
        var oldAuthenticationState = "outOfService";
        var newAuthenticationState = "inService";
        var stateInterface = "stateInterface";
        var rgwMacAddress = "00:0a:95:8d:78:16";
        var swVersion = "1.2";
        var hsiCfsServiceInstanceId = UUID.randomUUID().toString();

        // Prepare stubbed replies
        CpeAuthenticationConsumerDmaapModel firstEvent = ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(pnfName1)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rgwMacAddress(rgwMacAddress)
                .swVersion(swVersion)
                .build();
        CpeAuthenticationConsumerDmaapModel secondEvent = ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(pnfName2)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rgwMacAddress(rgwMacAddress)
                .swVersion(swVersion)
                .build();

        var pnfAaiObject = constructPnfObject(pnfName1, hsiCfsServiceInstanceId);
        var hsiCfsServiceInstance =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId, pnfName1, rgwMacAddress);

        // Prepare Mocks
        var cfsUrl = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance.getServiceInstanceId());

        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(10);
        when(consumerTask.execute(CONSUME_CPE_AUTHENTICATION_TASK_NAME))
                .thenReturn(Flux.fromIterable(Arrays.asList(firstEvent, secondEvent)));
        when(aaiClientTask.executePnfRetrieval(anyString(), anyString()))
                .thenReturn(Mono.just(pnfAaiObject))
                .thenReturn(Mono.error(new AaiTaskException("Mock A&AI exception")));
        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl))
                .thenReturn(Mono.just(hsiCfsServiceInstance));

        when(publishResponse.successful()).thenReturn(true);
        when(publisherTask.execute(any(ControlLoopPublisherDmaapModel.class))).thenReturn(Flux.just(publishResponse));

        // Execute the pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .assertNext(r -> assertTrue(r.successful()))
                .verifyComplete();

        verify(aaiClientTask, times(2)).executePnfRetrieval(anyString(), anyString());
        verify(aaiClientTask).executeServiceInstanceRetrieval(anyString(), anyString());
        verify(publisherTask).execute(any(ControlLoopPublisherDmaapModel.class));
    }

    @Test
    void twoEvents_firstWithPnfErrorReply_secondOk_handleCorrectOnly() throws SSLException {

        var pnfName1 = "olt1";
        var pnfName2 = "olt2";
        var oldAuthenticationState = "outOfService";
        var newAuthenticationState = "inService";
        var stateInterface = "stateInterface";
        var rgwMacAddress = "00:0a:95:8d:78:16";
        var swVersion = "1.2";
        var hsiCfsServiceInstanceId = UUID.randomUUID().toString();

        // Prepare stubbed replies
        CpeAuthenticationConsumerDmaapModel firstEvent = ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(pnfName1)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rgwMacAddress(rgwMacAddress)
                .swVersion(swVersion)
                .build();
        CpeAuthenticationConsumerDmaapModel secondEvent = ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(pnfName2)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rgwMacAddress(rgwMacAddress)
                .swVersion(swVersion)
                .build();

        var pnfAaiObject = constructPnfObject(pnfName2, hsiCfsServiceInstanceId);
        var hsiCfsServiceInstance =
                constructHsiCfsServiceInstanceObject(hsiCfsServiceInstanceId, pnfName2, rgwMacAddress);

        // Prepare Mocks
        var cfsUrl = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                hsiCfsServiceInstance.getServiceInstanceId());

        when(configuration.getPipelinesTimeoutInSeconds()).thenReturn(10);
        when(consumerTask.execute(CONSUME_CPE_AUTHENTICATION_TASK_NAME))
                .thenReturn(Flux.fromIterable(Arrays.asList(firstEvent, secondEvent)));
        when(aaiClientTask.executePnfRetrieval(anyString(), anyString()))
                .thenReturn(Mono.error(new AaiTaskException("Mock A&AI exception")))
                .thenReturn(Mono.just(pnfAaiObject));
        when(aaiClientTask
                .executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, cfsUrl))
                .thenReturn(Mono.just(hsiCfsServiceInstance));

        when(publishResponse.successful()).thenReturn(true);
        when(publisherTask.execute(any(ControlLoopPublisherDmaapModel.class))).thenReturn(Flux.just(publishResponse));

        // Execute the pipeline
        StepVerifier.create(pipeline.executePipeline())
                .expectSubscription()
                .assertNext(r -> assertTrue(r.successful()))
                .verifyComplete();

        verify(aaiClientTask, times(2))
                .executePnfRetrieval(anyString(), anyString());
        verify(aaiClientTask).executeServiceInstanceRetrieval(anyString(), anyString());
        verify(publisherTask).execute(any(ControlLoopPublisherDmaapModel.class));
    }

    private PnfAaiObject constructPnfObject(String pnfName, String hsiCfsServiceInstanceId) {

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
                                                                          String rgwMacAddress) {
        var orchestrationStatus = "active";

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
                        .metaname("rgw-mac-address")
                        .metavalue(rgwMacAddress)
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