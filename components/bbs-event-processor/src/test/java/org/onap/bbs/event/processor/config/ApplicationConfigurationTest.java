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
        "configs.dmaap.consumer.re-registration.dmaapUserName=",
        "configs.dmaap.consumer.re-registration.dmaapUserPassword=",
        "configs.dmaap.consumer.re-registration.dmaapTopicName=/events/unauthenticated.PNF_REREGISTRATION",
        "configs.dmaap.consumer.re-registration.dmaapProtocol=http",
        "configs.dmaap.consumer.re-registration.dmaapContentType=application/json",
        "configs.dmaap.consumer.re-registration.consumerId=c12",
        "configs.dmaap.consumer.re-registration.consumerGroup=OpenDcae-c12",
        "configs.dmaap.consumer.re-registration.timeoutMs=-1",
        "configs.dmaap.consumer.re-registration.messageLimit=1",
        "configs.dmaap.consumer.cpe-authentication.dmaapHostName=test localhost",
        "configs.dmaap.consumer.cpe-authentication.dmaapPortNumber=1234",
        "configs.dmaap.consumer.cpe-authentication.dmaapUserName=",
        "configs.dmaap.consumer.cpe-authentication.dmaapUserPassword=",
        "configs.dmaap.consumer.cpe-authentication.dmaapTopicName=/events/unauthenticated.CPE_AUTHENTICATION",
        "configs.dmaap.consumer.cpe-authentication.dmaapProtocol=http",
        "configs.dmaap.consumer.cpe-authentication.dmaapContentType=application/json",
        "configs.dmaap.consumer.cpe-authentication.consumerId=c12",
        "configs.dmaap.consumer.cpe-authentication.consumerGroup=OpenDcae-c12",
        "configs.dmaap.consumer.cpe-authentication.timeoutMs=-1",
        "configs.dmaap.consumer.cpe-authentication.messageLimit=1",
        "configs.dmaap.producer.dmaapHostName=test localhost",
        "configs.dmaap.producer.dmaapPortNumber=1234",
        "configs.dmaap.producer.dmaapUserName=",
        "configs.dmaap.producer.dmaapUserPassword=",
        "configs.dmaap.producer.dmaapTopicName=/events/unauthenticated.DCAE_CL_OUTPUT",
        "configs.dmaap.producer.dmaapProtocol=http",
        "configs.dmaap.producer.dmaapContentType=application/json",
        "configs.security.trustStorePath=KeyStore.jks",
        "configs.security.trustStorePasswordPath=KeyStorePass.txt",
        "configs.security.keyStorePath=KeyStore.jks",
        "configs.security.keyStorePasswordPath=KeyStorePass.txt",
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

        var aaiClientConfiguration = configuration.getAaiClientConfiguration();
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

        assertAll("DMaaP Consumer Re-Registration Configuration Properties",
            () -> assertEquals("test localhost",
                    configuration.getDmaapReRegistrationConsumerProperties().getDmaapHostName()),
            () -> assertEquals(1234,
                    configuration.getDmaapReRegistrationConsumerProperties().getDmaapPortNumber()),
            () -> assertEquals("/events/unauthenticated.PNF_REREGISTRATION",
                    configuration.getDmaapReRegistrationConsumerProperties().getDmaapTopicName()),
            () -> assertEquals("http",
                    configuration.getDmaapReRegistrationConsumerProperties().getDmaapProtocol()),
            () -> assertEquals("",
                    configuration.getDmaapReRegistrationConsumerProperties().getDmaapUserName()),
            () -> assertEquals("",
                    configuration.getDmaapReRegistrationConsumerProperties().getDmaapUserPassword()),
            () -> assertEquals("application/json",
                    configuration.getDmaapReRegistrationConsumerProperties().getDmaapContentType()),
            () -> assertEquals("c12",
                    configuration.getDmaapReRegistrationConsumerProperties().getConsumerId()),
            () -> assertEquals("OpenDcae-c12",
                    configuration.getDmaapReRegistrationConsumerProperties().getConsumerGroup()),
            () -> assertEquals(-1, configuration.getDmaapReRegistrationConsumerProperties().getTimeoutMs()),
            () -> assertEquals(1, configuration.getDmaapReRegistrationConsumerProperties().getMessageLimit())
        );

        assertAll("DMaaP Consumer CPE Authentication Configuration Properties",
            () -> assertEquals("test localhost",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getDmaapHostName()),
            () -> assertEquals(1234,
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getDmaapPortNumber()),
            () -> assertEquals("/events/unauthenticated.CPE_AUTHENTICATION",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getDmaapTopicName()),
            () -> assertEquals("http",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getDmaapProtocol()),
            () -> assertEquals("",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getDmaapUserName()),
            () -> assertEquals("",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getDmaapUserPassword()),
            () -> assertEquals("application/json",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getDmaapContentType()),
            () -> assertEquals("c12",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getConsumerId()),
            () -> assertEquals("OpenDcae-c12",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getConsumerGroup()),
            () -> assertEquals(-1, configuration.getDmaapCpeAuthenticationConsumerProperties().getTimeoutMs()),
            () -> assertEquals(1,
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getMessageLimit())
        );

        assertAll("DMaaP Publisher Configuration Properties",
            () -> assertEquals("test localhost",
                    configuration.getDmaapProducerProperties().getDmaapHostName()),
            () -> assertEquals(1234, configuration.getDmaapProducerProperties().getDmaapPortNumber()),
            () -> assertEquals("/events/unauthenticated.DCAE_CL_OUTPUT",
                    configuration.getDmaapProducerProperties().getDmaapTopicName()),
            () -> assertEquals("http", configuration.getDmaapProducerProperties().getDmaapProtocol()),
            () -> assertEquals("", configuration.getDmaapProducerProperties().getDmaapUserName()),
            () -> assertEquals("", configuration.getDmaapProducerProperties().getDmaapUserPassword()),
            () -> assertEquals("application/json",
                    configuration.getDmaapProducerProperties().getDmaapContentType())
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
            () -> assertEquals("KeyStore.jks", aaiClientConfiguration.keyStorePath()),
            () -> assertEquals("KeyStorePass.txt",
                        aaiClientConfiguration.keyStorePasswordPath()),
            () -> assertEquals("KeyStore.jks", aaiClientConfiguration.trustStorePath()),
            () -> assertEquals("KeyStorePass.txt",
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
                .keyStorePath("KeyStore-update.jks")
                .keyStorePasswordPath("KeyStorePass-update.txt")
                .trustStorePath("KeyStore-update.jks")
                .trustStorePasswordPath("KeyStorePass-update.txt")
                .enableAaiCertAuth(true)
                .enableDmaapCertAuth(true)
                .streamSubscribesMap(subscribes)
                .streamPublishesMap(Collections.singletonMap("config_key_3", streamsObject3))
                .build();

        // Update the configuration
        configuration.updateCurrentConfiguration(updatedConfiguration);

        var aaiClientConfiguration = configuration.getAaiClientConfiguration();
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

        assertAll("DMaaP Consumer Re-Registration Configuration Properties",
            () -> assertEquals("we-are-message-router1.us",
                    configuration.getDmaapReRegistrationConsumerProperties().getDmaapHostName()),
            () -> assertEquals(3901,
                    configuration.getDmaapReRegistrationConsumerProperties().getDmaapPortNumber()),
            () -> assertEquals("unauthenticated.PNF_UPDATE",
                        configuration.getDmaapReRegistrationConsumerProperties().getDmaapTopicName()),
            () -> assertEquals("https",
                    configuration.getDmaapReRegistrationConsumerProperties().getDmaapProtocol()),
            () -> assertEquals("some-user",
                    configuration.getDmaapReRegistrationConsumerProperties().getDmaapUserName()),
            () -> assertEquals("some-password",
                    configuration.getDmaapReRegistrationConsumerProperties().getDmaapUserPassword()),
            () -> assertEquals("application/json",
                    configuration.getDmaapReRegistrationConsumerProperties().getDmaapContentType()),
            () -> assertEquals("c13",
                    configuration.getDmaapReRegistrationConsumerProperties().getConsumerId()),
            () -> assertEquals("OpenDcae-c13",
                    configuration.getDmaapReRegistrationConsumerProperties().getConsumerGroup()),
            () -> assertEquals(5, configuration.getDmaapReRegistrationConsumerProperties().getTimeoutMs()),
            () -> assertEquals(10, configuration.getDmaapReRegistrationConsumerProperties().getMessageLimit())
        );

        assertAll("DMaaP Consumer CPE Authentication Configuration Properties",
            () -> assertEquals("we-are-message-router2.us",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getDmaapHostName()),
            () -> assertEquals(3902,
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getDmaapPortNumber()),
            () -> assertEquals("unauthenticated.CPE_AUTHENTICATION",
                        configuration.getDmaapCpeAuthenticationConsumerProperties().getDmaapTopicName()),
            () -> assertEquals("https",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getDmaapProtocol()),
            () -> assertEquals("some-user",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getDmaapUserName()),
            () -> assertEquals("some-password",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getDmaapUserPassword()),
            () -> assertEquals("application/json",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getDmaapContentType()),
            () -> assertEquals("c13",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getConsumerId()),
            () -> assertEquals("OpenDcae-c13",
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getConsumerGroup()),
            () -> assertEquals(5, configuration.getDmaapCpeAuthenticationConsumerProperties().getTimeoutMs()),
            () -> assertEquals(10,
                    configuration.getDmaapCpeAuthenticationConsumerProperties().getMessageLimit())
        );

        assertAll("DMaaP Publisher Configuration Properties",
            () -> assertEquals("we-are-message-router3.us",
                    configuration.getDmaapProducerProperties().getDmaapHostName()),
            () -> assertEquals(3903, configuration.getDmaapProducerProperties().getDmaapPortNumber()),
            () -> assertEquals("unauthenticated.DCAE_CL_OUTPUT",
                        configuration.getDmaapProducerProperties().getDmaapTopicName()),
            () -> assertEquals("https", configuration.getDmaapProducerProperties().getDmaapProtocol()),
            () -> assertEquals("some-user", configuration.getDmaapProducerProperties().getDmaapUserName()),
            () -> assertEquals("some-password",
                    configuration.getDmaapProducerProperties().getDmaapUserPassword()),
            () -> assertEquals("application/json",
                    configuration.getDmaapProducerProperties().getDmaapContentType())
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
            () -> assertEquals("KeyStore-update.jks", aaiClientConfiguration.keyStorePath()),
            () -> assertEquals("KeyStorePass-update.txt",
                        aaiClientConfiguration.keyStorePasswordPath()),
            () -> assertEquals("KeyStore-update.jks", aaiClientConfiguration.trustStorePath()),
            () -> assertEquals("KeyStorePass-update.txt",
                        aaiClientConfiguration.trustStorePasswordPath())
        );
    }
}