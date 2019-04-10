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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.onap.bbs.event.processor.model.GeneratedAppConfigObject;
import org.onap.bbs.event.processor.model.ImmutableDmaapInfo;
import org.onap.bbs.event.processor.model.ImmutableGeneratedAppConfigObject;
import org.onap.bbs.event.processor.model.ImmutableStreamsObject;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {
        AaiClientProperties.class,
        DmaapReRegistrationConsumerProperties.class,
        DmaapCpeAuthenticationConsumerProperties.class,
        DmaapProducerProperties.class,
        SecurityProperties.class,
        GenericProperties.class})
@EnableConfigurationProperties
@TestPropertySource(properties = {
        "configs.aai.client.aaiHost=test localhost",
        "configs.aai.client.aaiPort=1234",
        "configs.aai.client.aaiProtocol=https",
        "configs.aai.client.aaiUserName=AAI",
        "configs.aai.client.aaiUserPassword=AAI",
        "configs.aai.client.aaiIgnoreSslCertificateErrors=true",
        "configs.aai.client.aaiHeaders.X-FromAppId=bbs",
        "configs.aai.client.aaiHeaders.X-TransactionId=9999",
        "configs.aai.client.aaiHeaders.Accept=application/json",
        "configs.aai.client.aaiHeaders.Real-Time=true",
        "configs.aai.client.aaiHeaders.Content-Type=application/merge-patch+json",
        "configs.dmaap.consumer.re-registration.dmaapHostName=test localhost",
        "configs.dmaap.consumer.re-registration.dmaapPortNumber=1234",
        "configs.dmaap.consumer.re-registration.dmaapTopicName=/events/unauthenticated.PNF_REREGISTRATION",
        "configs.dmaap.consumer.re-registration.dmaapProtocol=http",
        "configs.dmaap.consumer.re-registration.dmaapContentType=application/json",
        "configs.dmaap.consumer.re-registration.consumerId=c12",
        "configs.dmaap.consumer.re-registration.consumerGroup=OpenDcae-c12",
        "configs.dmaap.consumer.re-registration.timeoutMs=-1",
        "configs.dmaap.consumer.re-registration.messageLimit=1",
        "configs.dmaap.consumer.cpe-authentication.dmaapHostName=test localhost",
        "configs.dmaap.consumer.cpe-authentication.dmaapPortNumber=1234",
        "configs.dmaap.consumer.cpe-authentication.dmaapTopicName=/events/unauthenticated.CPE_AUTHENTICATION",
        "configs.dmaap.consumer.cpe-authentication.dmaapProtocol=http",
        "configs.dmaap.consumer.cpe-authentication.dmaapContentType=application/json",
        "configs.dmaap.consumer.cpe-authentication.consumerId=c12",
        "configs.dmaap.consumer.cpe-authentication.consumerGroup=OpenDcae-c12",
        "configs.dmaap.consumer.cpe-authentication.timeoutMs=-1",
        "configs.dmaap.consumer.cpe-authentication.messageLimit=1",
        "configs.dmaap.producer.dmaapHostName=test localhost",
        "configs.dmaap.producer.dmaapPortNumber=1234",
        "configs.dmaap.producer.dmaapTopicName=/events/unauthenticated.DCAE_CL_OUTPUT",
        "configs.dmaap.producer.dmaapProtocol=http",
        "configs.dmaap.producer.dmaapContentType=application/json",
        "configs.security.trustStorePath=test trust store path",
        "configs.security.trustStorePasswordPath=test trust store password path",
        "configs.security.keyStorePath=test key store path",
        "configs.security.keyStorePasswordPath=test key store password path",
        "configs.security.enableDmaapCertAuth=false",
        "configs.security.enableAaiCertAuth=false",
        "configs.application.pipelinesPollingIntervalSec=30",
        "configs.application.pipelinesTimeoutSec=15",
        "configs.application.policyVersion=1.0.0",
        "configs.application.clTargetType=VM",
        "configs.application.clEventStatus=ONSET",
        "configs.application.clVersion=1.0.2",
        "configs.application.clTarget=vserver.vserver-name",
        "configs.application.clOriginator=DCAE-bbs-event-processor",
        "configs.application.re-registration.policyScope=reRegPolicyScope",
        "configs.application.re-registration.clControlName=reRegControlName",
        "configs.application.cpe-authentication.policyScope=cpeAuthPolicyScope",
        "configs.application.cpe-authentication.clControlName=cpeAuthControlName"})
@DisplayName("Application Configuration Unit-Tests")
// Ordering tests because we need a first configuration population from @TestPropertySource
// and then update of the config parameters based on a new Consul-retrieved Configuration object
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ApplicationConfigurationTest {

    private ApplicationConfiguration configuration;

    @Autowired
    public ApplicationConfigurationTest(AaiClientProperties aaiClientProperties,
                                        DmaapReRegistrationConsumerProperties dmaapReRegistrationConsumerProperties,
                                        DmaapCpeAuthenticationConsumerProperties
                                                    dmaapCpeAuthenticationConsumerProperties,
                                        DmaapProducerProperties dmaapProducerProperties,
                                        SecurityProperties securityProperties,
                                        GenericProperties genericProperties) {
        this.configuration = new ApplicationConfiguration(aaiClientProperties,
                dmaapReRegistrationConsumerProperties,
                dmaapCpeAuthenticationConsumerProperties,
                dmaapProducerProperties,
                securityProperties,
                genericProperties);
    }

    @Test
    void testA_configurationObjectSuccessfullyPopulated() {

        AaiClientConfiguration aaiClientConfiguration = configuration.getAaiClientConfiguration();
        assertAll("AAI Client Configuration Properties",
            () -> assertEquals("test localhost", aaiClientConfiguration.aaiHost()),
            () -> assertEquals(Integer.valueOf(1234), aaiClientConfiguration.aaiPort()),
            () -> assertEquals("https", aaiClientConfiguration.aaiProtocol()),
            () -> assertEquals("AAI", aaiClientConfiguration.aaiUserName()),
            () -> assertEquals("AAI", aaiClientConfiguration.aaiUserPassword()),
            () -> assertTrue(aaiClientConfiguration.aaiIgnoreSslCertificateErrors()),
            () -> assertEquals("bbs", aaiClientConfiguration.aaiHeaders().get("X-FromAppId")),
            () -> assertEquals("9999", aaiClientConfiguration.aaiHeaders().get("X-TransactionId")),
            () -> assertEquals("application/json", aaiClientConfiguration.aaiHeaders().get("Accept")),
            () -> assertEquals("true", aaiClientConfiguration.aaiHeaders().get("Real-Time")),
            () -> assertEquals("application/merge-patch+json",
                        aaiClientConfiguration.aaiHeaders().get("Content-Type"))
        );

        DmaapConsumerConfiguration dmaapConsumerReRegistrationConfig =
                configuration.getDmaapReRegistrationConsumerConfiguration();
        assertAll("DMaaP Consumer Re-Registration Configuration Properties",
            () -> assertEquals("test localhost", dmaapConsumerReRegistrationConfig.dmaapHostName()),
            () -> assertEquals(Integer.valueOf(1234), dmaapConsumerReRegistrationConfig.dmaapPortNumber()),
            () -> assertEquals("/events/unauthenticated.PNF_REREGISTRATION",
                    dmaapConsumerReRegistrationConfig.dmaapTopicName()),
            () -> assertEquals("http", dmaapConsumerReRegistrationConfig.dmaapProtocol()),
            () -> assertEquals("", dmaapConsumerReRegistrationConfig.dmaapUserName()),
            () -> assertEquals("", dmaapConsumerReRegistrationConfig.dmaapUserPassword()),
            () -> assertEquals("application/json", dmaapConsumerReRegistrationConfig.dmaapContentType()),
            () -> assertEquals("c12", dmaapConsumerReRegistrationConfig.consumerId()),
            () -> assertEquals("OpenDcae-c12", dmaapConsumerReRegistrationConfig.consumerGroup()),
            () -> assertEquals(Integer.valueOf(-1), dmaapConsumerReRegistrationConfig.timeoutMs()),
            () -> assertEquals(Integer.valueOf(1), dmaapConsumerReRegistrationConfig.messageLimit())
        );

        DmaapConsumerConfiguration dmaapConsumerCpeAuthenticationConfig =
                configuration.getDmaapCpeAuthenticationConsumerConfiguration();
        assertAll("DMaaP Consumer CPE Authentication Configuration Properties",
            () -> assertEquals("test localhost", dmaapConsumerCpeAuthenticationConfig.dmaapHostName()),
            () -> assertEquals(Integer.valueOf(1234), dmaapConsumerCpeAuthenticationConfig.dmaapPortNumber()),
            () -> assertEquals("/events/unauthenticated.CPE_AUTHENTICATION",
                    dmaapConsumerCpeAuthenticationConfig.dmaapTopicName()),
            () -> assertEquals("http", dmaapConsumerCpeAuthenticationConfig.dmaapProtocol()),
            () -> assertEquals("", dmaapConsumerCpeAuthenticationConfig.dmaapUserName()),
            () -> assertEquals("", dmaapConsumerCpeAuthenticationConfig.dmaapUserPassword()),
            () -> assertEquals("application/json", dmaapConsumerCpeAuthenticationConfig.dmaapContentType()),
            () -> assertEquals("c12", dmaapConsumerCpeAuthenticationConfig.consumerId()),
            () -> assertEquals("OpenDcae-c12", dmaapConsumerCpeAuthenticationConfig.consumerGroup()),
            () -> assertEquals(Integer.valueOf(-1), dmaapConsumerCpeAuthenticationConfig.timeoutMs()),
            () -> assertEquals(Integer.valueOf(1), dmaapConsumerCpeAuthenticationConfig.messageLimit())
        );

        DmaapPublisherConfiguration dmaapPublisherConfiguration = configuration.getDmaapPublisherConfiguration();
        assertAll("DMaaP Publisher Configuration Properties",
            () -> assertEquals("test localhost", dmaapPublisherConfiguration.dmaapHostName()),
            () -> assertEquals(Integer.valueOf(1234), dmaapPublisherConfiguration.dmaapPortNumber()),
            () -> assertEquals("/events/unauthenticated.DCAE_CL_OUTPUT",
                    dmaapPublisherConfiguration.dmaapTopicName()),
            () -> assertEquals("http", dmaapPublisherConfiguration.dmaapProtocol()),
            () -> assertEquals("", dmaapPublisherConfiguration.dmaapUserName()),
            () -> assertEquals("", dmaapPublisherConfiguration.dmaapUserPassword()),
            () -> assertEquals("application/json", dmaapPublisherConfiguration.dmaapContentType())
        );

        assertAll("Generic Application Properties",
            () -> assertEquals(30, configuration.getPipelinesPollingIntervalInSeconds()),
            () -> assertEquals(15, configuration.getPipelinesTimeoutInSeconds()),
            () -> assertEquals("1.0.0", configuration.getPolicyVersion()),
            () -> assertEquals("VM", configuration.getCloseLoopTargetType()),
            () -> assertEquals("ONSET", configuration.getCloseLoopEventStatus()),
            () -> assertEquals("1.0.2", configuration.getCloseLoopVersion()),
            () -> assertEquals("vserver.vserver-name", configuration.getCloseLoopTarget()),
            () -> assertEquals("DCAE-bbs-event-processor", configuration.getCloseLoopOriginator()),
            () -> assertEquals("reRegPolicyScope", configuration.getReRegistrationCloseLoopPolicyScope()),
            () -> assertEquals("cpeAuthPolicyScope", configuration.getCpeAuthenticationCloseLoopPolicyScope()),
            () -> assertEquals("reRegControlName", configuration.getReRegistrationCloseLoopControlName()),
            () -> assertEquals("cpeAuthControlName", configuration.getCpeAuthenticationCloseLoopControlName())
        );

        assertAll("Security Application Properties",
            () -> assertFalse(aaiClientConfiguration.enableAaiCertAuth()),
            () -> assertFalse(dmaapConsumerReRegistrationConfig.enableDmaapCertAuth()),
            () -> assertEquals("test key store path", aaiClientConfiguration.keyStorePath()),
            () -> assertEquals("test key store password path",
                        aaiClientConfiguration.keyStorePasswordPath()),
            () -> assertEquals("test trust store path", aaiClientConfiguration.trustStorePath()),
            () -> assertEquals("test trust store password path",
                        aaiClientConfiguration.trustStorePasswordPath())
        );
    }

    @Test
    void testB_passingNewConfiguration_UpdateSucceeds() {
        Map<String, GeneratedAppConfigObject.StreamsObject> subscribes = new HashMap<>();

        GeneratedAppConfigObject.DmaapInfo dmaapInfo1 = ImmutableDmaapInfo.builder()
                .clientId("1500462518108")
                .clientRole("com.dcae.member")
                .location("mtc00")
                .topicUrl("https://we-are-message-router1.us:3901/events/unauthenticated.PNF_UPDATE")
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
                .topicUrl("https://we-are-message-router2.us:3902/events/unauthenticated.CPE_AUTHENTICATION")
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
                .topicUrl("https://we-are-message-router3.us:3903/events/unauthenticated.DCAE_CL_OUTPUT")
                .build();
        GeneratedAppConfigObject.StreamsObject streamsObject3 = ImmutableStreamsObject.builder()
                .type("message_router")
                .aafUsername("some-user")
                .aafPassword("some-password")
                .dmaapInfo(dmaapInfo3)
                .build();

        // Final config object
        GeneratedAppConfigObject updatedConfiguration = ImmutableGeneratedAppConfigObject.builder()
                .dmaapProtocol("https")
                .dmaapContentType("application/json")
                .dmaapConsumerConsumerId("c13")
                .dmaapConsumerConsumerGroup("OpenDcae-c13")
                .dmaapMessageLimit(10)
                .dmaapTimeoutMs(5)
                .aaiHost("aai.onap.svc.cluster.local")
                .aaiPort(8443)
                .aaiProtocol("http")
                .aaiUsername("AAI-update")
                .aaiPassword("AAI-update")
                .aaiIgnoreSslCertificateErrors(false)
                .pipelinesPollingIntervalSec(20)
                .pipelinesTimeoutSec(20)
                .cbsPollingIntervalSec(180)
                .policyVersion("2.0.0")
                .closeLoopTargetType("VM2")
                .closeLoopEventStatus("ONSET-update")
                .closeLoopVersion("2.0.2")
                .closeLoopTarget("Target-update")
                .closeLoopOriginator("Originator-update")
                .reRegistrationPolicyScope("policyScope-update")
                .reRegistrationClControlName("controlName-update")
                .cpeAuthPolicyScope("policyScope-update")
                .cpeAuthClControlName("controlName-update")
                .reRegConfigKey("config_key_1")
                .cpeAuthConfigKey("config_key_2")
                .closeLoopConfigKey("config_key_3")
                .loggingLevel("TRACE")
                .keyStorePath("test key store path - update")
                .keyStorePasswordPath("test key store password path - update")
                .trustStorePath("test trust store path - update")
                .trustStorePasswordPath("test trust store password path - update")
                .enableAaiCertAuth(true)
                .enableDmaapCertAuth(true)
                .streamSubscribesMap(subscribes)
                .streamPublishesMap(Collections.singletonMap("config_key_3", streamsObject3))
                .build();

        // Update the configuration
        configuration.updateCurrentConfiguration(updatedConfiguration);

        AaiClientConfiguration aaiClientConfiguration = configuration.getAaiClientConfiguration();
        assertAll("AAI Client Configuration Properties",
            () -> assertEquals("aai.onap.svc.cluster.local", aaiClientConfiguration.aaiHost()),
            () -> assertEquals(Integer.valueOf(8443), aaiClientConfiguration.aaiPort()),
            () -> assertEquals("http", aaiClientConfiguration.aaiProtocol()),
            () -> assertEquals("AAI-update", aaiClientConfiguration.aaiUserName()),
            () -> assertEquals("AAI-update", aaiClientConfiguration.aaiUserPassword()),
            () -> assertFalse(aaiClientConfiguration.aaiIgnoreSslCertificateErrors()),
            () -> assertEquals("bbs", aaiClientConfiguration.aaiHeaders().get("X-FromAppId")),
            () -> assertEquals("9999", aaiClientConfiguration.aaiHeaders().get("X-TransactionId")),
            () -> assertEquals("application/json", aaiClientConfiguration.aaiHeaders().get("Accept")),
            () -> assertEquals("true", aaiClientConfiguration.aaiHeaders().get("Real-Time")),
            () -> assertEquals("application/merge-patch+json",
                        aaiClientConfiguration.aaiHeaders().get("Content-Type"))
        );

        DmaapConsumerConfiguration dmaapConsumerReRegistrationConfig =
                configuration.getDmaapReRegistrationConsumerConfiguration();
        assertAll("DMaaP Consumer Re-Registration Configuration Properties",
            () -> assertEquals("we-are-message-router1.us", dmaapConsumerReRegistrationConfig.dmaapHostName()),
            () -> assertEquals(Integer.valueOf(3901), dmaapConsumerReRegistrationConfig.dmaapPortNumber()),
            () -> assertEquals("events/unauthenticated.PNF_UPDATE",
                    dmaapConsumerReRegistrationConfig.dmaapTopicName()),
            () -> assertEquals("https", dmaapConsumerReRegistrationConfig.dmaapProtocol()),
            () -> assertEquals("some-user", dmaapConsumerReRegistrationConfig.dmaapUserName()),
            () -> assertEquals("some-password", dmaapConsumerReRegistrationConfig.dmaapUserPassword()),
            () -> assertEquals("application/json", dmaapConsumerReRegistrationConfig.dmaapContentType()),
            () -> assertEquals("c13", dmaapConsumerReRegistrationConfig.consumerId()),
            () -> assertEquals("OpenDcae-c13", dmaapConsumerReRegistrationConfig.consumerGroup()),
            () -> assertEquals(Integer.valueOf(5), dmaapConsumerReRegistrationConfig.timeoutMs()),
            () -> assertEquals(Integer.valueOf(10), dmaapConsumerReRegistrationConfig.messageLimit())
        );

        DmaapConsumerConfiguration dmaapConsumerCpeAuthenticationConfig =
                configuration.getDmaapCpeAuthenticationConsumerConfiguration();
        assertAll("DMaaP Consumer CPE Authentication Configuration Properties",
            () -> assertEquals("we-are-message-router2.us", dmaapConsumerCpeAuthenticationConfig.dmaapHostName()),
            () -> assertEquals(Integer.valueOf(3902), dmaapConsumerCpeAuthenticationConfig.dmaapPortNumber()),
            () -> assertEquals("events/unauthenticated.CPE_AUTHENTICATION",
                    dmaapConsumerCpeAuthenticationConfig.dmaapTopicName()),
            () -> assertEquals("https", dmaapConsumerCpeAuthenticationConfig.dmaapProtocol()),
            () -> assertEquals("some-user", dmaapConsumerCpeAuthenticationConfig.dmaapUserName()),
            () -> assertEquals("some-password", dmaapConsumerCpeAuthenticationConfig.dmaapUserPassword()),
            () -> assertEquals("application/json", dmaapConsumerCpeAuthenticationConfig.dmaapContentType()),
            () -> assertEquals("c13", dmaapConsumerCpeAuthenticationConfig.consumerId()),
            () -> assertEquals("OpenDcae-c13", dmaapConsumerCpeAuthenticationConfig.consumerGroup()),
            () -> assertEquals(Integer.valueOf(5), dmaapConsumerCpeAuthenticationConfig.timeoutMs()),
            () -> assertEquals(Integer.valueOf(10), dmaapConsumerCpeAuthenticationConfig.messageLimit())
        );

        DmaapPublisherConfiguration dmaapPublisherConfiguration = configuration.getDmaapPublisherConfiguration();
        assertAll("DMaaP Publisher Configuration Properties",
            () -> assertEquals("we-are-message-router3.us", dmaapPublisherConfiguration.dmaapHostName()),
            () -> assertEquals(Integer.valueOf(3903), dmaapPublisherConfiguration.dmaapPortNumber()),
            () -> assertEquals("events/unauthenticated.DCAE_CL_OUTPUT",
                    dmaapPublisherConfiguration.dmaapTopicName()),
            () -> assertEquals("https", dmaapPublisherConfiguration.dmaapProtocol()),
            () -> assertEquals("some-user", dmaapPublisherConfiguration.dmaapUserName()),
            () -> assertEquals("some-password", dmaapPublisherConfiguration.dmaapUserPassword()),
            () -> assertEquals("application/json", dmaapPublisherConfiguration.dmaapContentType())
        );

        assertAll("Generic Application Properties",
            () -> assertEquals(20, configuration.getPipelinesPollingIntervalInSeconds()),
            () -> assertEquals(20, configuration.getPipelinesTimeoutInSeconds()),
            () -> assertEquals(180, configuration.getCbsPollingInterval()),
            () -> assertEquals("2.0.0", configuration.getPolicyVersion()),
            () -> assertEquals("VM2", configuration.getCloseLoopTargetType()),
            () -> assertEquals("ONSET-update", configuration.getCloseLoopEventStatus()),
            () -> assertEquals("2.0.2", configuration.getCloseLoopVersion()),
            () -> assertEquals("Target-update", configuration.getCloseLoopTarget()),
            () -> assertEquals("Originator-update", configuration.getCloseLoopOriginator()),
            () -> assertEquals("policyScope-update", configuration.getReRegistrationCloseLoopPolicyScope()),
            () -> assertEquals("policyScope-update", configuration.getCpeAuthenticationCloseLoopPolicyScope()),
            () -> assertEquals("controlName-update", configuration.getReRegistrationCloseLoopControlName()),
            () -> assertEquals("controlName-update", configuration.getCpeAuthenticationCloseLoopControlName())
        );

        assertAll("Security Application Properties",
            () -> assertTrue(aaiClientConfiguration.enableAaiCertAuth()),
            () -> assertTrue(dmaapConsumerReRegistrationConfig.enableDmaapCertAuth()),
            () -> assertEquals("test key store path - update", aaiClientConfiguration.keyStorePath()),
            () -> assertEquals("test key store password path - update",
                        aaiClientConfiguration.keyStorePasswordPath()),
            () -> assertEquals("test trust store path - update", aaiClientConfiguration.trustStorePath()),
            () -> assertEquals("test trust store password path - update",
                        aaiClientConfiguration.trustStorePasswordPath())
        );
    }
}