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

import static org.onap.bbs.event.processor.config.ApplicationConstants.CONSUME_REREGISTRATION_TASK_NAME;
import static org.onap.bbs.event.processor.config.ApplicationConstants.DCAE_BBS_EVENT_PROCESSOR_MS_INSTANCE;
import static org.onap.bbs.event.processor.config.ApplicationConstants.RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME;
import static org.onap.bbs.event.processor.config.ApplicationConstants.RETRIEVE_PNF_TASK_NAME;
import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.INSTANCE_UUID;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLException;

import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.exceptions.EmptyDmaapResponseException;
import org.onap.bbs.event.processor.model.ControlLoopPublisherDmaapModel;
import org.onap.bbs.event.processor.model.ImmutableControlLoopPublisherDmaapModel;
import org.onap.bbs.event.processor.model.PnfAaiObject;
import org.onap.bbs.event.processor.model.ReRegistrationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.RelationshipListAaiObject;
import org.onap.bbs.event.processor.model.ServiceInstanceAaiObject;
import org.onap.bbs.event.processor.tasks.AaiClientTask;
import org.onap.bbs.event.processor.tasks.DmaapPublisherTask;
import org.onap.bbs.event.processor.tasks.DmaapReRegistrationConsumerTask;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReRegistrationPipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReRegistrationPipeline.class);

    private static final String POLICY_NAME = "Nomadic_ONT";

    private DmaapReRegistrationConsumerTask consumerTask;
    private DmaapPublisherTask publisherTask;
    private AaiClientTask aaiClientTask;

    private ApplicationConfiguration configuration;

    private Map<String, String> mdcContextMap;

    @Autowired
    ReRegistrationPipeline(ApplicationConfiguration configuration,
                           DmaapReRegistrationConsumerTask consumerTask,
                           DmaapPublisherTask publisherTask,
                           AaiClientTask aaiClientTask,
                           Map<String, String> mdcContextMap) {
        this.configuration = configuration;
        this.consumerTask = consumerTask;
        this.publisherTask = publisherTask;
        this.aaiClientTask = aaiClientTask;
        this.mdcContextMap = mdcContextMap;
    }

    /**
     * PNF re-registration processing pipeline for BBS uS.
     */
    public void processPnfReRegistrationEvents() {
        MDC.setContextMap(mdcContextMap);
        LOGGER.info("Process next Re-Registration events");
        executePipeline()
                .subscribe(this::onSuccess, this::onError, this::onComplete);
        LOGGER.trace("Reactive PNF Re-registration pipeline subscribed - Execution started");
    }

    Flux<MessageRouterPublishResponse> executePipeline() {
        return
            // Consume Re-Registration from DMaaP
            consumeReRegistrationsFromDmaap()
            // Fetch PNF from A&AI
            .flatMap(this::fetchPnfFromAai)
            // Fetch related HSI CFS instance from A&AI
            .flatMap(this::fetchHsiCfsServiceInstanceFromAai)
            // Trigger Policy for relocation
            .flatMap(this::triggerPolicy);
    }

    private void onSuccess(MessageRouterPublishResponse response) {
        if (response.successful()) {
            LOGGER.info("PNF Re-Registration event successfully handled. Published Policy event to DMaaP");
        } else {
            LOGGER.error("PNF Re-Registration event handling error [{}]", response.failReason());
        }
    }

    private void onError(Throwable throwable) {
        LOGGER.error("Aborted PNF Re-Registration events processing. Error: {}", throwable.getMessage());
    }

    private void onComplete() {
        LOGGER.info("PNF Re-Registration processing pipeline has been completed");
    }

    private Flux<PipelineState> consumeReRegistrationsFromDmaap() {
        return Flux.defer(() -> {
            MDC.put(INSTANCE_UUID, UUID.randomUUID().toString());
            try {
                return consumerTask.execute(CONSUME_REREGISTRATION_TASK_NAME)
                        .timeout(Duration.ofSeconds(configuration.getPipelinesTimeoutInSeconds()))
                        .doOnError(e -> {
                            if (e instanceof TimeoutException) {
                                LOGGER.warn("Timed out waiting for DMaaP response");
                            } else if (e instanceof EmptyDmaapResponseException) {
                                LOGGER.info("Nothing to consume from DMaaP");
                            } else {
                                LOGGER.error("DMaaP Consumer error: {}", e.getMessage());
                                LOGGER.debug("Error\n", e);
                            }
                        })
                        .onErrorResume(
                            e -> e instanceof Exception,
                            e -> Mono.empty())
                        .map(event -> {
                            // For each message, we have to keep separate state. This state will be enhanced
                            // in each step and handed off to the next processing step
                            var state = new PipelineState();
                            state.setReRegistrationEvent(event);
                            return state;
                        });
            } catch (SSLException e) {
                return Flux.error(e);
            }
        });
    }

    private Mono<PipelineState> fetchPnfFromAai(PipelineState state) {

        var vesEvent = state.getReRegistrationEvent();
        var pnfName = vesEvent.getCorrelationId();
        var url = String.format("/aai/v14/network/pnfs/pnf/%s?depth=all", pnfName);
        LOGGER.debug("Processing Step: Retrieve PNF. Url: ({})", url);

        return aaiClientTask.executePnfRetrieval(RETRIEVE_PNF_TASK_NAME, url)
                .timeout(Duration.ofSeconds(configuration.getPipelinesTimeoutInSeconds()))
                .doOnError(TimeoutException.class,
                        e -> LOGGER.warn("Timed out waiting for A&AI response")
                )
                .doOnError(e -> {
                            LOGGER.error("Error while retrieving PNF: {}", e.getMessage());
                            LOGGER.debug("Error\n", e);
                        }
                )
                .onErrorResume(
                    e -> e instanceof Exception,
                    e -> Mono.empty())
                .map(p -> {
                    state.setPnfAaiObject(p);
                    return state;
                });
    }

    private Mono<PipelineState> fetchHsiCfsServiceInstanceFromAai(PipelineState state) {

        if (state == null || state.getPnfAaiObject() == null) {
            return Mono.empty();
        }

        // At this point, we have both the VES-event of the re-registration and the PNF object retrieved from A&AI
        // We can check if this processing needs to continue in case of a true relocation
        if (isNotReallyAnOntRelocation(state)) {
            return Mono.empty();
        }

        var pnf = state.getPnfAaiObject();
        // Assuming that the PNF will only have a single service-instance relationship pointing
        // towards the HSI CFS service
        var serviceInstanceId = pnf.getRelationshipListAaiObject().getRelationshipEntries()
                .stream()
                .filter(e -> "service-instance".equals(e.getRelatedTo()))
                .flatMap(e -> e.getRelationshipData().stream())
                .filter(d -> "service-instance.service-instance-id".equals(d.getRelationshipKey()))
                .map(RelationshipListAaiObject.RelationshipDataEntryAaiObject::getRelationshipValue)
                .findFirst().orElse("");

        if (StringUtils.isEmpty(serviceInstanceId)) {
            LOGGER.error("Unable to retrieve HSI CFS service instance from PNF {}",
                    state.getPnfAaiObject().getPnfName());
            return Mono.empty();
        }

        var url = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                serviceInstanceId);
        LOGGER.debug("Processing Step: Retrieve HSI CFS Service. Url: ({})", url);
        return aaiClientTask.executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, url)
                .timeout(Duration.ofSeconds(configuration.getPipelinesTimeoutInSeconds()))
                .doOnError(TimeoutException.class,
                        e -> LOGGER.warn("Timed out waiting for A&AI response")
                )
                .doOnError(e -> {
                            LOGGER.error("Error while retrieving HSI CFS Service instance: {}", e.getMessage());
                            LOGGER.debug("Error\n", e);
                        }
                )
                .onErrorResume(
                    e -> e instanceof Exception,
                    e -> Mono.empty())
                .map(s -> {
                    state.setHsiCfsServiceInstance(s);
                    return state;
                });
    }

    private boolean isNotReallyAnOntRelocation(PipelineState state) {
        var relationshipEntries = state.getPnfAaiObject().getRelationshipListAaiObject().getRelationshipEntries();

        // If no logical-link, fail further processing
        if (relationshipEntries.stream().noneMatch(e -> "logical-link".equals(e.getRelatedTo()))) {
            LOGGER.warn("PNF {} does not have any logical-links bridged. Stop further processing",
                    state.getPnfAaiObject().getPnfName());
            return true;
        }

        // Assuming PNF will only have one logical-link per BBS use case design
        var isNotRelocation = relationshipEntries
                .stream()
                .filter(e -> "logical-link".equals(e.getRelatedTo()))
                .flatMap(e -> e.getRelationshipData().stream())
                .anyMatch(d -> d.getRelationshipValue()
                        .equals(state.getReRegistrationEvent().getAttachmentPoint()));


        if (isNotRelocation) {
            LOGGER.warn("Not a Relocation for PNF {} with attachment point {}",
                    state.getPnfAaiObject().getPnfName(),
                    state.getReRegistrationEvent().getAttachmentPoint());
        }
        return isNotRelocation;
    }

    private Flux<MessageRouterPublishResponse> triggerPolicy(PipelineState state) {

        if (state == null || state.getHsiCfsServiceInstance() == null) {
            return Flux.empty();
        }

        var event = buildTriggeringPolicyEvent(state);
        return publisherTask.execute(event)
                .timeout(Duration.ofSeconds(configuration.getPipelinesTimeoutInSeconds()))
                .doOnError(TimeoutException.class,
                        e -> LOGGER.warn("Timed out waiting for DMaaP confirmation")
                )
                .doOnError(e -> LOGGER.error("Error while triggering Policy: {}", e.getMessage()))
                .onErrorResume(
                    e -> e instanceof Exception,
                    e -> Mono.empty());
    }

    private ControlLoopPublisherDmaapModel buildTriggeringPolicyEvent(PipelineState state) {

        var cfsServiceInstanceId = state.getHsiCfsServiceInstance().getServiceInstanceId();

        var attachmentPoint = state.getReRegistrationEvent().getAttachmentPoint();
        var remoteId = state.getReRegistrationEvent().getRemoteId();
        var cvlan = state.getReRegistrationEvent().getCVlan();
        var svlan = state.getReRegistrationEvent().getSVlan();

        Map<String, String> enrichmentData = new HashMap<>();
        enrichmentData.put("service-information.hsia-cfs-service-instance-id", cfsServiceInstanceId);

        enrichmentData.put("attachmentPoint", attachmentPoint);
        enrichmentData.put("remoteId", remoteId);
        enrichmentData.put("cvlan", cvlan);
        enrichmentData.put("svlan", svlan);

        ControlLoopPublisherDmaapModel triggerEvent = ImmutableControlLoopPublisherDmaapModel.builder()
                .closedLoopEventClient(DCAE_BBS_EVENT_PROCESSOR_MS_INSTANCE)
                .policyVersion(configuration.getPolicyVersion())
                .policyName(POLICY_NAME)
                .policyScope(configuration.getReRegistrationCloseLoopPolicyScope())
                .targetType(configuration.getCloseLoopTargetType())
                .aaiEnrichmentData(enrichmentData)
                .closedLoopAlarmStart(Instant.now().getEpochSecond())
                .closedLoopEventStatus(configuration.getCloseLoopEventStatus())
                .closedLoopControlName(configuration.getReRegistrationCloseLoopControlName())
                .version(configuration.getCloseLoopVersion())
                .target(configuration.getCloseLoopTarget())
                .requestId(UUID.randomUUID().toString())
                .originator(configuration.getCloseLoopOriginator())
                .build();
        LOGGER.debug("Processing Step: Publish for Policy");
        LOGGER.trace("Trigger Policy event: ({})",triggerEvent);
        return triggerEvent;
    }

    private static class PipelineState {

        private ReRegistrationConsumerDmaapModel reRegistrationEvent;
        private PnfAaiObject pnfAaiObject;
        private ServiceInstanceAaiObject hsiCfsServiceInstance;

        ReRegistrationConsumerDmaapModel getReRegistrationEvent() {
            return reRegistrationEvent;
        }

        void setReRegistrationEvent(ReRegistrationConsumerDmaapModel reRegistrationEvent) {
            this.reRegistrationEvent = reRegistrationEvent;
        }

        PnfAaiObject getPnfAaiObject() {
            return pnfAaiObject;
        }

        void setPnfAaiObject(PnfAaiObject pnfAaiObject) {
            this.pnfAaiObject = pnfAaiObject;
        }

        ServiceInstanceAaiObject getHsiCfsServiceInstance() {
            return hsiCfsServiceInstance;
        }

        void setHsiCfsServiceInstance(ServiceInstanceAaiObject hsiCfsServiceInstance) {
            this.hsiCfsServiceInstance = hsiCfsServiceInstance;
        }
    }
}
