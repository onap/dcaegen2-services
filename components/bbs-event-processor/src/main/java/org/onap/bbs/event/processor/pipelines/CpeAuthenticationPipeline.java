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

import static org.onap.bbs.event.processor.config.ApplicationConstants.CLOSED_LOOP_EVENT_STATUS;
import static org.onap.bbs.event.processor.config.ApplicationConstants.CLOSE_LOOP_TARGET;
import static org.onap.bbs.event.processor.config.ApplicationConstants.CLOSE_LOOP_TARGET_TYPE;
import static org.onap.bbs.event.processor.config.ApplicationConstants.CLOSE_LOOP_VERSION;
import static org.onap.bbs.event.processor.config.ApplicationConstants.CONSUME_CPE_AUTHENTICATION_TASK_NAME;
import static org.onap.bbs.event.processor.config.ApplicationConstants.DCAE_BBS_EVENT_PROCESSOR_MS_INSTANCE;
import static org.onap.bbs.event.processor.config.ApplicationConstants.FROM;
import static org.onap.bbs.event.processor.config.ApplicationConstants.POLICY_VERSION;
import static org.onap.bbs.event.processor.config.ApplicationConstants.RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME;
import static org.onap.bbs.event.processor.config.ApplicationConstants.RETRIEVE_PNF_TASK_NAME;
import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.INSTANCE_UUID;
import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.RESPONSE_CODE;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLException;

import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.exceptions.EmptyDmaapResponseException;
import org.onap.bbs.event.processor.model.ControlLoopPublisherDmaapModel;
import org.onap.bbs.event.processor.model.CpeAuthenticationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.ImmutableControlLoopPublisherDmaapModel;
import org.onap.bbs.event.processor.model.MetadataListAaiObject;
import org.onap.bbs.event.processor.model.PnfAaiObject;
import org.onap.bbs.event.processor.model.RelationshipListAaiObject;
import org.onap.bbs.event.processor.model.ServiceInstanceAaiObject;
import org.onap.bbs.event.processor.tasks.AaiClientTask;
import org.onap.bbs.event.processor.tasks.DmaapCpeAuthenticationConsumerTask;
import org.onap.bbs.event.processor.tasks.DmaapPublisherTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CpeAuthenticationPipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(CpeAuthenticationPipeline.class);

    private static final String POLICY_NAME = "CPE_Authentication";

    private DmaapCpeAuthenticationConsumerTask consumerTask;
    private DmaapPublisherTask publisherTask;
    private AaiClientTask aaiClientTask;

    private ApplicationConfiguration configuration;

    private Map<String, String> mdcContextMap;

    @Autowired
    CpeAuthenticationPipeline(ApplicationConfiguration configuration,
                              DmaapCpeAuthenticationConsumerTask consumerTask,
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
     * PNF CPE Authentication processing pipeline for BBS uS.
     */
    public void processPnfCpeAuthenticationEvents() {
        MDC.setContextMap(mdcContextMap);
        LOGGER.info("Process next CPE Authentication events");
        executePipeline()
                .subscribe(this::onSuccess, this::onError, this::onComplete);
        LOGGER.trace("Reactive CPE Authentication pipeline subscribed - Execution started");
    }

    Flux<ResponseEntity<String>> executePipeline() {
        return
            // Consume CPE Authentication from DMaaP
            consumeCpeAuthenticationFromDmaap()
            // Fetch PNF from A&AI
            .flatMap(this::fetchPnfFromAai)
            // Fetch related HSI CFS instance from A&AI
            .flatMap(this::fetchHsiCfsServiceInstanceFromAai)
            // Trigger Policy for relocation
            .flatMap(this::triggerPolicy);
    }

    private void onSuccess(ResponseEntity<String> responseCode) {
        MDC.put(RESPONSE_CODE, responseCode.getStatusCode().toString());
        LOGGER.info("CPE Authentication event successfully handled. "
                        + "Publishing to DMaaP for Policy returned a status code of ({} {})",
                responseCode.getStatusCode().value(), responseCode.getStatusCode().getReasonPhrase());
        MDC.remove(RESPONSE_CODE);
    }

    private void onError(Throwable throwable) {
        LOGGER.error("Aborted CPE Authentication events processing. Error: {}", throwable.getMessage());
    }

    private void onComplete() {
        LOGGER.info("CPE Authentication processing pipeline has been completed");
    }

    private Flux<PipelineState> consumeCpeAuthenticationFromDmaap() {
        return Flux.defer(() -> {
            MDC.put(INSTANCE_UUID, UUID.randomUUID().toString());
            try {
                return consumerTask.execute(CONSUME_CPE_AUTHENTICATION_TASK_NAME)
                        .timeout(Duration.ofSeconds(configuration.getPipelinesTimeoutInSeconds()))
                        .doOnError(e -> {
                            if (e instanceof TimeoutException) {
                                LOGGER.warn("Timed out waiting for DMaaP response");
                            } else if (e instanceof EmptyDmaapResponseException) {
                                LOGGER.info("Nothing to consume from DMaaP");
                            } else {
                                LOGGER.error("DMaaP Consumer error: {}", e.getMessage());
                            }
                        })
                        .onErrorResume(
                            e -> e instanceof Exception,
                            e -> Mono.empty())
                        .map(event -> {
                            // For each message, we have to keep separate state. This state will be enhanced
                            // in each step and handed off to the next processing step
                            PipelineState state = new PipelineState();
                            state.setCpeAuthenticationEvent(event);
                            return state;
                        });
            } catch (SSLException e) {
                return Flux.error(e);
            }
        });
    }

    private Mono<PipelineState> fetchPnfFromAai(PipelineState state) {

        CpeAuthenticationConsumerDmaapModel vesEvent = state.getCpeAuthenticationEvent();
        String pnfName = vesEvent.getCorrelationId();
        String url = String.format("/aai/v14/network/pnfs/pnf/%s?depth=all", pnfName);
        LOGGER.debug("Processing Step: Retrieve PNF. Url: ({})", url);

        return aaiClientTask.executePnfRetrieval(RETRIEVE_PNF_TASK_NAME, url)
                .timeout(Duration.ofSeconds(configuration.getPipelinesTimeoutInSeconds()))
                .doOnError(TimeoutException.class,
                        e -> LOGGER.warn("Timed out waiting for A&AI response")
                )
                .doOnError(e -> LOGGER.error("Error while retrieving PNF: {}",
                        e.getMessage())
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

        PnfAaiObject pnf = state.getPnfAaiObject();
        // Assuming that the PNF will only have a single service-instance relationship pointing
        // towards the HSI CFS service
        String serviceInstanceId = pnf.getRelationshipListAaiObject().getRelationshipEntries()
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

        String url = String.format("/aai/v14/nodes/service-instances/service-instance/%s?depth=all",
                serviceInstanceId);
        LOGGER.debug("Processing Step: Retrieve HSI CFS Service. Url: ({})", url);
        return aaiClientTask.executeServiceInstanceRetrieval(RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME, url)
                .timeout(Duration.ofSeconds(configuration.getPipelinesTimeoutInSeconds()))
                .doOnError(TimeoutException.class,
                        e -> LOGGER.warn("Timed out waiting for A&AI response")
                )
                .doOnError(e -> LOGGER.error("Error while retrieving HSI CFS Service instance: {}",
                        e.getMessage())
                )
                .onErrorResume(
                    e -> e instanceof Exception,
                    e -> Mono.empty())
                .map(s -> {
                    state.setHsiCfsServiceInstance(s);
                    return state;
                });
    }

    private Mono<ResponseEntity<String>> triggerPolicy(PipelineState state) {

        if (state == null || state.getHsiCfsServiceInstance() == null) {
            return Mono.empty();
        }

        // At this point, we must check if the PNF RGW MAC address matches the value extracted from VES event
        if (!isCorrectMacAddress(state)) {
            LOGGER.warn("Processing stopped. RGW MAC address taken from event ({}) "
                            + "does not match with A&AI metadata corresponding value",
                    state.getCpeAuthenticationEvent().getRgwMacAddress());
            return Mono.empty();
        }

        ControlLoopPublisherDmaapModel event = buildTriggeringPolicyEvent(state);
        return publisherTask.execute(event)
                .timeout(Duration.ofSeconds(configuration.getPipelinesTimeoutInSeconds()))
                .doOnError(TimeoutException.class,
                        e -> LOGGER.warn("Timed out waiting for DMaaP publish confirmation")
                )
                .doOnError(e -> LOGGER.error("Error while triggering Policy: {}", e.getMessage()))
                .onErrorResume(
                    e -> e instanceof Exception,
                    e -> Mono.empty());
    }


    private boolean isCorrectMacAddress(PipelineState state) {
        // We need to check if the RGW MAC address received in VES event matches the one found in
        // HSIA CFS service (in its metadata section)
        Optional<MetadataListAaiObject> optionalMetadata = state.getHsiCfsServiceInstance()
                .getMetadataListAaiObject();
        String eventRgwMacAddress = state.getCpeAuthenticationEvent().getRgwMacAddress().orElse("");
        return optionalMetadata
                .map(list -> list.getMetadataEntries()
                .stream()
                .anyMatch(m -> "rgw-mac-address".equals(m.getMetaname())
                        && m.getMetavalue().equals(eventRgwMacAddress)))
                .orElse(false);
    }

    private ControlLoopPublisherDmaapModel buildTriggeringPolicyEvent(PipelineState state) {

        String cfsServiceInstanceId = state.getHsiCfsServiceInstance().getServiceInstanceId();

        Map<String, String> enrichmentData = new HashMap<>();
        enrichmentData.put("service-information.hsia-cfs-service-instance-id", cfsServiceInstanceId);
        enrichmentData.put("cpe.old-authentication-state", state.cpeAuthenticationEvent.getOldAuthenticationState());
        enrichmentData.put("cpe.new-authentication-state", state.cpeAuthenticationEvent.getNewAuthenticationState());
        String swVersion = state.getCpeAuthenticationEvent().getSwVersion().orElse("");
        if (!StringUtils.isEmpty(swVersion)) {
            enrichmentData.put("cpe.swVersion", swVersion);
        }

        ControlLoopPublisherDmaapModel triggerEvent = ImmutableControlLoopPublisherDmaapModel.builder()
                .closedLoopEventClient(DCAE_BBS_EVENT_PROCESSOR_MS_INSTANCE)
                .policyVersion(POLICY_VERSION)
                .policyName(POLICY_NAME)
                .policyScope(configuration.getCpeAuthenticationCloseLoopPolicyScope())
                .targetType(CLOSE_LOOP_TARGET_TYPE)
                .aaiEnrichmentData(enrichmentData)
                .closedLoopAlarmStart(Instant.now().getEpochSecond())
                .closedLoopEventStatus(CLOSED_LOOP_EVENT_STATUS)
                .closedLoopControlName(configuration.getCpeAuthenticationCloseLoopControlName())
                .version(CLOSE_LOOP_VERSION)
                .target(CLOSE_LOOP_TARGET)
                .requestId(UUID.randomUUID().toString())
                .originator(FROM)
                .build();
        LOGGER.debug("Processing Step: Publish for Policy");
        LOGGER.trace("Trigger Policy event: ({})",triggerEvent);
        return triggerEvent;
    }

    private static class PipelineState {

        private CpeAuthenticationConsumerDmaapModel cpeAuthenticationEvent;
        private PnfAaiObject pnfAaiObject;
        private ServiceInstanceAaiObject hsiCfsServiceInstance;

        CpeAuthenticationConsumerDmaapModel getCpeAuthenticationEvent() {
            return cpeAuthenticationEvent;
        }

        void setCpeAuthenticationEvent(CpeAuthenticationConsumerDmaapModel cpeAuthenticationEvent) {
            this.cpeAuthenticationEvent = cpeAuthenticationEvent;
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
