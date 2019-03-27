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

package org.onap.bbs.event.processor.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.onap.bbs.event.processor.exceptions.ApplicationEnvironmentException;
import org.onap.bbs.event.processor.model.GeneratedAppConfigObject;
import org.onap.bbs.event.processor.model.ImmutableDmaapInfo;
import org.onap.bbs.event.processor.model.ImmutableGeneratedAppConfigObject;
import org.onap.bbs.event.processor.model.ImmutableStreamsObject;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.EnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.Disposable;

@Component
public class ConsulConfigurationGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulConfigurationGateway.class);

    private static final String CONSUL_HOST = "CONSUL_HOST";
    private static final String CONFIG_BINDING_SERVICE = "CONFIG_BINDING_SERVICE";
    private static final String HOSTNAME = "HOSTNAME";

    private final ApplicationConfiguration configuration;
    private Gson gson;
    private Disposable cbsFetchPipeline;

    @Autowired
    ConsulConfigurationGateway(ApplicationConfiguration configuration) {
        this.configuration = configuration;
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Periodically fetch application configuration via CBS service of DCAE.
     * @param initialDelay initial delay before initiation of polling
     * @param period polling interval
     */
    public void periodicallyFetchConfigFromCbs(Duration initialDelay, Duration period) {
        if (environmentNotReady()) {
            throw new ApplicationEnvironmentException(
                    String.format("Application Environment missing critical parameters: %s",
                            getMissingEnvironmentVariables()));
        }

        fetchConfig(initialDelay, period);
    }

    boolean environmentNotReady() {
        String consulHost = System.getenv().get(CONSUL_HOST);
        String cbs = System.getenv().get(CONFIG_BINDING_SERVICE);
        String hostname = System.getenv().get(HOSTNAME);
        return consulHost == null || cbs == null || hostname == null;
    }

    /**
     * Reschedule application configuration periodic retrieval via CBS service of DCAE.
     * @param initialDelay initial delay before rescheduling
     * @param period new polling interval
     */
    public void rescheduleCbsConfigurationRetrieval(Duration initialDelay, Duration period) {
        if (cbsFetchPipeline != null && !cbsFetchPipeline.isDisposed()) {
            LOGGER.info("Disposing old CBS Config fetch job");
            cbsFetchPipeline.dispose();
        }
        periodicallyFetchConfigFromCbs(initialDelay, period);
    }

    private void parseConsulRetrievedConfiguration(JsonObject jsonObject) {

        GeneratedAppConfigObject generatedAppConfigObject = generateAppConfigObject(jsonObject);
        LOGGER.trace("Consul-Retrieved Application Generated Object:\n{}", generatedAppConfigObject);
        configuration.updateCurrentConfiguration(generatedAppConfigObject);
    }

    private void handleErrors(Throwable throwable) {
        LOGGER.error("Periodic CBS configuration polling was terminated with error: {}", throwable);
        LOGGER.info("Will restart CBS configuration fetching job due to abnormal termination."
                + " Will start fetching after 60 seconds (please correct configuration in the meantime)"
                + " and it will then poll every {} seconds (reverting to default)",
                configuration.getCbsPollingInterval());
        fetchConfig(Duration.ofSeconds(60), Duration.ofSeconds(configuration.getCbsPollingInterval()));
    }

    private void fetchConfig(Duration initialDelay, Duration period) {
        RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();

        // Necessary properties from the environment (Consul host:port, service-name (hostname), CBS name)
        EnvProperties env = EnvProperties.fromEnvironment();

        // Create the client and use it to get the configuration
        cbsFetchPipeline = CbsClientFactory.createCbsClient(env)
                .doOnError(e -> LOGGER.warn("CBS Configuration fetch failed with error: {}", e))
                .retry(e -> true)
                .flatMapMany(cbsClient -> cbsClient.updates(diagnosticContext, initialDelay, period))
                .subscribe(this::parseConsulRetrievedConfiguration, this::handleErrors);
    }

    @NotNull
    GeneratedAppConfigObject generateAppConfigObject(JsonObject configObject) {

        if (LOGGER.isInfoEnabled()) {
            String configAsString = gson.toJson(configObject);
            LOGGER.info("Received App Config object\n{}", configAsString);
        }

        final String dmaapProtocol = configObject.get("dmaap.protocol").getAsString();
        final String dmaapContentType  = configObject.get("dmaap.contentType").getAsString();
        final String dmaapConsumerId  = configObject.get("dmaap.consumer.consumerId").getAsString();
        final String dmaapConsumerGroup = configObject.get("dmaap.consumer.consumerGroup").getAsString();
        final int dmaapMessageLimit  = configObject.get("dmaap.messageLimit").getAsInt();
        final int dmaapTimeoutMs  = configObject.get("dmaap.timeoutMs").getAsInt();

        final String aaiHost = configObject.get("aai.host").getAsString();
        final int aaiPort = configObject.get("aai.port").getAsInt();
        final String aaiProtocol = configObject.get("aai.protocol").getAsString();
        final String aaiUsername = configObject.get("aai.username").getAsString();
        final String aaiPassword = configObject.get("aai.password").getAsString();
        final boolean aaiIgnoreSslCertificateErrors =
                configObject.get("aai.aaiIgnoreSslCertificateErrors").getAsBoolean();

        final int pipelinesPollingIntervalSec = configObject.get("application.pipelinesPollingIntervalSec").getAsInt();
        final int pipelinesTimeoutSec = configObject.get("application.pipelinesTimeoutSec").getAsInt();
        final int cbsPollingIntervalSec = configObject.get("application.cbsPollingIntervalSec").getAsInt();

        final String reRegPolicyScope = configObject.get("application.reregistration.policyScope").getAsString();
        final String reRegClControlName = configObject.get("application.reregistration.clControlName").getAsString();
        final String cpeAuthPolicyScope = configObject.get("application.cpe.authentication.policyScope").getAsString();
        final String cpeAuthClControlName =
                configObject.get("application.cpe.authentication.clControlName").getAsString();

        final String policyVersion = configObject.get("application.policyVersion").getAsString();
        final String closeLoopTargetType = configObject.get("application.clTargetType").getAsString();
        final String closeLoopEventStatus = configObject.get("application.clEventStatus").getAsString();
        final String closeLoopVersion = configObject.get("application.clVersion").getAsString();
        final String closeLoopTarget = configObject.get("application.clTarget").getAsString();
        final String closeLoopOriginator = configObject.get("application.clOriginator").getAsString();

        final String reRegConfigKey = configObject.get("application.reregistration.configKey").getAsString();
        final String cpeAuthConfigKey = configObject.get("application.cpeAuth.configKey").getAsString();
        final String closeLoopConfigKey = configObject.get("application.closeLoop.configKey").getAsString();

        final JsonObject streamsPublishes = configObject.getAsJsonObject("streams_publishes");
        final JsonObject streamsSubscribes = configObject.getAsJsonObject("streams_subscribes");

        return ImmutableGeneratedAppConfigObject.builder()
                .dmaapProtocol(dmaapProtocol)
                .dmaapContentType(dmaapContentType)
                .dmaapConsumerConsumerId(dmaapConsumerId)
                .dmaapConsumerConsumerGroup(dmaapConsumerGroup)
                .dmaapMessageLimit(dmaapMessageLimit)
                .dmaapTimeoutMs(dmaapTimeoutMs)
                .aaiHost(aaiHost)
                .aaiPort(aaiPort)
                .aaiProtocol(aaiProtocol)
                .aaiUsername(aaiUsername)
                .aaiPassword(aaiPassword)
                .aaiIgnoreSslCertificateErrors(aaiIgnoreSslCertificateErrors)
                .pipelinesPollingIntervalSec(pipelinesPollingIntervalSec)
                .pipelinesTimeoutSec(pipelinesTimeoutSec)
                .cbsPollingIntervalSec(cbsPollingIntervalSec)
                .reRegistrationPolicyScope(reRegPolicyScope)
                .reRegistrationClControlName(reRegClControlName)
                .policyVersion(policyVersion)
                .closeLoopTargetType(closeLoopTargetType)
                .closeLoopEventStatus(closeLoopEventStatus)
                .closeLoopVersion(closeLoopVersion)
                .closeLoopTarget(closeLoopTarget)
                .closeLoopOriginator(closeLoopOriginator)
                .cpeAuthPolicyScope(cpeAuthPolicyScope)
                .cpeAuthClControlName(cpeAuthClControlName)
                .reRegConfigKey(reRegConfigKey)
                .cpeAuthConfigKey(cpeAuthConfigKey)
                .closeLoopConfigKey(closeLoopConfigKey)
                .streamSubscribesMap(parseStreamsObjects(streamsSubscribes))
                .streamPublishesMap(parseStreamsObjects(streamsPublishes))
                .build();
    }

    private Set<String> getMissingEnvironmentVariables() {
        Set<String> missingVars = new HashSet<>();
        if (System.getenv().get(CONSUL_HOST) == null) {
            missingVars.add(CONSUL_HOST);
        }
        if (System.getenv().get(CONFIG_BINDING_SERVICE) == null) {
            missingVars.add(CONFIG_BINDING_SERVICE);
        }
        if (System.getenv().get(HOSTNAME) == null) {
            missingVars.add(HOSTNAME);
        }
        return missingVars;
    }

    private Map<String, GeneratedAppConfigObject.StreamsObject> parseStreamsObjects(
            JsonObject jsonObject) {
        Map<String, GeneratedAppConfigObject.StreamsObject> streams = new HashMap<>();

        jsonObject.entrySet().stream()
                .map(this::parseStreamsSingleObject)
                .forEach(e -> streams.put(e.getKey(), e.getValue()));

        return streams;
    }

    private Map.Entry<String, GeneratedAppConfigObject.StreamsObject> parseStreamsSingleObject(
            Map.Entry<String, JsonElement> jsonEntry) {

        JsonObject closeLoopOutput = (JsonObject) jsonEntry.getValue();

        String type = closeLoopOutput.get("type").getAsString();
        String aafUsername = closeLoopOutput.get("aaf_username") != null
                ? closeLoopOutput.get("aaf_username").getAsString() : "";
        String aafPassword = closeLoopOutput.get("aaf_password") != null
                ? closeLoopOutput.get("aaf_password").getAsString() : "";

        JsonObject dmaapInfo = closeLoopOutput.getAsJsonObject("dmaap_info");
        String clientId = dmaapInfo.get("client_id") != null
                ? dmaapInfo.get("client_id").getAsString() : "";
        String clientRole = dmaapInfo.get("client_role") != null
                ? dmaapInfo.get("client_role").getAsString() : "";
        String location = dmaapInfo.get("location") != null
                ? dmaapInfo.get("location").getAsString() : "";
        String topicUrl = dmaapInfo.get("topic_url").getAsString();

        GeneratedAppConfigObject.DmaapInfo dmaapInfoObject = ImmutableDmaapInfo.builder()
                .clientId(clientId)
                .clientRole(clientRole)
                .location(location)
                .topicUrl(topicUrl)
                .build();
        GeneratedAppConfigObject.StreamsObject streamsObject = ImmutableStreamsObject.builder()
                .type(type)
                .aafUsername(aafUsername)
                .aafPassword(aafPassword)
                .dmaapInfo(dmaapInfoObject)
                .build();

        return new AbstractMap.SimpleEntry<>(jsonEntry.getKey(), streamsObject);
    }
}
