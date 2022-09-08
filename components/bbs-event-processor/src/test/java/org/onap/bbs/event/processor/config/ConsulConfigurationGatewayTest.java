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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.google.gson.JsonParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.bbs.event.processor.model.GeneratedAppConfigObject;
import org.onap.bbs.event.processor.model.ImmutableDmaapInfo;
import org.onap.bbs.event.processor.model.ImmutableGeneratedAppConfigObject;
import org.onap.bbs.event.processor.model.ImmutableStreamsObject;

class ConsulConfigurationGatewayTest {

    private ConsulConfigurationGateway configurationGateway;
    private static JsonParser jsonParser;

    @BeforeAll
    static void setup() {
        jsonParser = new JsonParser();
    }

    ConsulConfigurationGatewayTest() {
        var configuration = Mockito.mock(ApplicationConfiguration.class);
        this.configurationGateway = new ConsulConfigurationGateway(configuration);
    }

    @Test
    void passingValidJson_constructsGeneratedAppConfigObject() {
        final var validJson = "{"
                + "\"dmaap.protocol\": \"http\","
                + "\"dmaap.contentType\": \"application/json\","
                + "\"dmaap.consumer.consumerId\": \"c12\","
                + "\"dmaap.consumer.consumerGroup\": \"OpenDcae-c12\","
                + "\"dmaap.messageLimit\": 1,"
                + "\"dmaap.timeoutMs\": -1,"
                + "\"aai.host\": \"aai.onap.svc.cluster.local\","
                + "\"aai.port\": \"8443\","
                + "\"aai.protocol\": \"https\","
                + "\"aai.username\": \"AAI\","
                + "\"aai.password\": \"AAI\","
                + "\"aai.aaiIgnoreSslCertificateErrors\": true,"
                + "\"application.pipelinesPollingIntervalSec\": 30,"
                + "\"application.pipelinesTimeoutSec\": 15,"
                + "\"application.cbsPollingIntervalSec\": 180,"
                + "\"application.reregistration.policyScope\": \"policyScope\","
                + "\"application.reregistration.clControlName\": \"controlName\","
                + "\"application.cpe.authentication.policyScope\": \"policyScope\","
                + "\"application.cpe.authentication.clControlName\": \"controlName\","
                + "\"application.policyVersion\": \"1.0\","
                + "\"application.clTargetType\": \"VM\","
                + "\"application.clEventStatus\": \"ONSET\","
                + "\"application.clVersion\": \"1.0.2\","
                + "\"application.clTarget\": \"vserver.vserver-name\","
                + "\"application.clOriginator\": \"DCAE-bbs-event-processor\","
                + "\"application.reregistration.configKey\": \"config_key_2\","
                + "\"application.cpeAuth.configKey\": \"config_key_1\","
                + "\"application.closeLoop.configKey\": \"config_key_3\","
                + "\"application.loggingLevel\": \"TRACE\","
                + "\"application.ssl.keyStorePath\": \"/opt/app/bbs-event-processor/etc/cert/key.p12\","
                + "\"application.ssl.keyStorePasswordPath\": \"/opt/app/bbs-event-processor/etc/cert/key.pass\","
                + "\"application.ssl.trustStorePath\": \"/opt/app/bbs-event-processor/etc/cert/trust.jks\","
                + "\"application.ssl.trustStorePasswordPath\": \"/opt/app/bbs-event-processor/etc/cert/trust.pass\","
                + "\"application.ssl.enableAaiCertAuth\": true,"
                + "\"application.ssl.enableDmaapCertAuth\": true,"
                + "\"streams_subscribes\": {"
                + "\"config_key_1\": {"
                + "\"type\": \"message_router\","
                + "\"aaf_username\": \"some-user\","
                + "\"aaf_password\": \"some-password\","
                + "\"dmaap_info\": {"
                + "\"client_id\": \"1500462518108\","
                + "\"client_role\": \"com.dcae.member\","
                + "\"location\": \"mtc00\","
                + "\"topic_url\": \"https://we-are-message-router.us:3905/events/unauthenticated.CPE_AUTHENTICATION\""
                + "}"
                + "},"
                + "\"config_key_2\": {"
                + "\"type\": \"message_router\","
                + "\"aaf_username\": \"some-user\","
                + "\"aaf_password\": \"some-password\","
                + "\"dmaap_info\": {"
                + "\"client_id\": \"1500462518108\","
                + "\"client_role\": \"com.dcae.member\","
                + "\"location\": \"mtc00\","
                + "\"topic_url\": \"https://we-are-message-router.us:3905/events/unauthenticated.PNF_UPDATE\""
                + "}"
                + "}"
                + "},"
                + "\"streams_publishes\": {"
                + "\"config_key_3\": {"
                + "\"type\": \"message_router\","
                + "\"aaf_username\": \"some-user\","
                + "\"aaf_password\": \"some-password\","
                + "\"dmaap_info\": {"
                + "\"client_id\": \"1500462518108\","
                + "\"client_role\": \"com.dcae.member\","
                + "\"location\": \"mtc00\","
                + "\"topic_url\": \"https://we-are-message-router.us:3905/events/unauthenticated.DCAE_CL_OUTPUT\""
                + "}"
                + "}"
                + "},"
                + "\"services_calls\": {"
                + "\"aai-interaction\": []"
                + "}"
                + "}";

        // Create expected configuration
        // Create Subscribes Objects
        Map<String, GeneratedAppConfigObject.StreamsObject> subscribes = new HashMap<>();

        GeneratedAppConfigObject.DmaapInfo dmaapInfo1 = ImmutableDmaapInfo.builder()
                .clientId("1500462518108")
                .clientRole("com.dcae.member")
                .location("mtc00")
                .topicUrl("https://we-are-message-router.us:3905/events/unauthenticated.CPE_AUTHENTICATION")
                .build();
        GeneratedAppConfigObject.StreamsObject streamsObject1 = ImmutableStreamsObject.builder()
                .type("message_router")
                .aafUsername("some-user")
                .aafPassword("some-password")
                .dmaapInfo(dmaapInfo1)
                .build();
        GeneratedAppConfigObject.DmaapInfo dmaapInfo2 = ImmutableDmaapInfo.builder()
                .clientId("1500462518108")
                .clientRole("com.dcae.member")
                .location("mtc00")
                .topicUrl("https://we-are-message-router.us:3905/events/unauthenticated.PNF_UPDATE")
                .build();
        GeneratedAppConfigObject.StreamsObject streamsObject2 = ImmutableStreamsObject.builder()
                .type("message_router")
                .aafUsername("some-user")
                .aafPassword("some-password")
                .dmaapInfo(dmaapInfo2)
                .build();

        subscribes.put("config_key_1", streamsObject1);
        subscribes.put("config_key_2", streamsObject2);

        // Create Publishes Objects
        GeneratedAppConfigObject.DmaapInfo dmaapInfo3 = ImmutableDmaapInfo.builder()
                .clientId("1500462518108")
                .clientRole("com.dcae.member")
                .location("mtc00")
                .topicUrl("https://we-are-message-router.us:3905/events/unauthenticated.DCAE_CL_OUTPUT")
                .build();
        GeneratedAppConfigObject.StreamsObject streamsObject3 = ImmutableStreamsObject.builder()
                .type("message_router")
                .aafUsername("some-user")
                .aafPassword("some-password")
                .dmaapInfo(dmaapInfo3)
                .build();

        // Expected final config object
        GeneratedAppConfigObject expectedConfiguration = ImmutableGeneratedAppConfigObject.builder()
                .dmaapProtocol("http")
                .dmaapContentType("application/json")
                .dmaapConsumerConsumerId("c12")
                .dmaapConsumerConsumerGroup("OpenDcae-c12")
                .dmaapMessageLimit(1)
                .dmaapTimeoutMs(-1)
                .aaiHost("aai.onap.svc.cluster.local")
                .aaiPort(8443)
                .aaiProtocol("https")
                .aaiUsername("AAI")
                .aaiPassword("AAI")
                .aaiIgnoreSslCertificateErrors(true)
                .pipelinesPollingIntervalSec(30)
                .pipelinesTimeoutSec(15)
                .cbsPollingIntervalSec(180)
                .reRegistrationPolicyScope("policyScope")
                .reRegistrationClControlName("controlName")
                .cpeAuthPolicyScope("policyScope")
                .cpeAuthClControlName("controlName")
                .policyVersion("1.0")
                .closeLoopTargetType("VM")
                .closeLoopEventStatus("ONSET")
                .closeLoopVersion("1.0.2")
                .closeLoopTarget("vserver.vserver-name")
                .closeLoopOriginator("DCAE-bbs-event-processor")
                .reRegConfigKey("config_key_2")
                .cpeAuthConfigKey("config_key_1")
                .closeLoopConfigKey("config_key_3")
                .loggingLevel("TRACE")
                .keyStorePath("/opt/app/bbs-event-processor/etc/cert/key.p12")
                .keyStorePasswordPath("/opt/app/bbs-event-processor/etc/cert/key.pass")
                .trustStorePath("/opt/app/bbs-event-processor/etc/cert/trust.jks")
                .trustStorePasswordPath("/opt/app/bbs-event-processor/etc/cert/trust.pass")
                .enableAaiCertAuth(true)
                .enableDmaapCertAuth(true)
                .streamSubscribesMap(subscribes)
                .streamPublishesMap(Collections.singletonMap("config_key_3", streamsObject3))
                .build();

        var spiedGateway = Mockito.spy(configurationGateway);
        doReturn(false).when(spiedGateway).environmentNotReady();
        assertEquals(expectedConfiguration,
                spiedGateway.generateAppConfigObject(jsonParser.parse(validJson).getAsJsonObject()));
    }
}