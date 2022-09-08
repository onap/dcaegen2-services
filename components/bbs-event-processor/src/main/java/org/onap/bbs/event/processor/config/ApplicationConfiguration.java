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

import static org.onap.bbs.event.processor.config.ApplicationConstants.STREAMS_TYPE;
import static org.onap.bbs.event.processor.utilities.GenericUtils.keyStoreFromResource;
import static org.onap.dcaegen2.services.sdk.security.ssl.Passwords.fromPath;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.onap.bbs.event.processor.exceptions.ApplicationEnvironmentException;
import org.onap.bbs.event.processor.exceptions.ConfigurationParsingException;
import org.onap.bbs.event.processor.model.GeneratedAppConfigObject;
import org.onap.bbs.event.processor.utilities.LoggingUtil;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.ImmutableMessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.ImmutableMessageRouterSubscriberConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration implements ConfigurationChangeObservable {

    private final AaiClientProperties aaiClientProperties;
    private final DmaapReRegistrationConsumerProperties dmaapReRegistrationConsumerProperties;
    private final DmaapCpeAuthenticationConsumerProperties dmaapCpeAuthenticationConsumerProperties;
    private final DmaapProducerProperties dmaapProducerProperties;
    private final SecurityProperties securityProperties;
    private final GenericProperties genericProperties;

    private MessageRouterSubscriberConfig dmaapReRegistrationConsumerConfiguration;
    private MessageRouterSubscriberConfig dmaapCpeAuthenticationConsumerConfiguration;
    private MessageRouterPublisherConfig dmaapPublisherConfiguration;
    private AaiClientConfiguration aaiClientConfiguration;
    private Set<ConfigurationChangeObserver> observers;

    private int cbsPollingInterval;

    /**
     * Construct BBS event processor application configuration object.
     * @param aaiClientProperties Properties for AAI client setup
     * @param dmaapReRegistrationConsumerProperties Properties for DMaaP client setup (PNF re-registration)
     * @param dmaapCpeAuthenticationConsumerProperties Properties for DMaaP client setup (CPE authentication)
     * @param dmaapProducerProperties Properties for DMaaP client setup (Close Loop)
     * @param securityProperties General security properties
     * @param genericProperties General application properties
     */
    @Autowired
    public ApplicationConfiguration(AaiClientProperties aaiClientProperties,
                                    DmaapReRegistrationConsumerProperties dmaapReRegistrationConsumerProperties,
                                    DmaapCpeAuthenticationConsumerProperties dmaapCpeAuthenticationConsumerProperties,
                                    DmaapProducerProperties dmaapProducerProperties,
                                    SecurityProperties securityProperties,
                                    GenericProperties genericProperties) {
        this.aaiClientProperties = aaiClientProperties;
        this.dmaapReRegistrationConsumerProperties = dmaapReRegistrationConsumerProperties;
        this.dmaapCpeAuthenticationConsumerProperties = dmaapCpeAuthenticationConsumerProperties;
        this.dmaapProducerProperties = dmaapProducerProperties;
        this.securityProperties = securityProperties;
        this.genericProperties = genericProperties;
        observers = new HashSet<>();
        constructConfigurationObjects();
    }

    @Override
    public synchronized void register(ConfigurationChangeObserver observer) {
        observers.add(observer);
    }

    @Override
    public synchronized void unRegister(ConfigurationChangeObserver observer) {
        observers.remove(observer);
    }

    @Override
    public synchronized void notifyObservers() {
        observers.forEach(ConfigurationChangeObserver::updateConfiguration);
    }

    public synchronized MessageRouterSubscriberConfig getDmaapReRegistrationConsumerConfiguration() {
        return dmaapReRegistrationConsumerConfiguration;
    }

    public synchronized MessageRouterSubscriberConfig getDmaapCpeAuthenticationConsumerConfiguration() {
        return dmaapCpeAuthenticationConsumerConfiguration;
    }

    public synchronized MessageRouterPublisherConfig getDmaapPublisherConfiguration() {
        return dmaapPublisherConfiguration;
    }

    public synchronized AaiClientConfiguration getAaiClientConfiguration() {
        return aaiClientConfiguration;
    }

    public synchronized int getPipelinesPollingIntervalInSeconds() {
        return genericProperties.getPipelinesPollingIntervalSec();
    }

    public synchronized DmaapProducerProperties getDmaapProducerProperties() {
        return dmaapProducerProperties;
    }

    public synchronized DmaapReRegistrationConsumerProperties getDmaapReRegistrationConsumerProperties() {
        return dmaapReRegistrationConsumerProperties;
    }

    public synchronized DmaapCpeAuthenticationConsumerProperties getDmaapCpeAuthenticationConsumerProperties() {
        return dmaapCpeAuthenticationConsumerProperties;
    }

    public synchronized int getPipelinesTimeoutInSeconds() {
        return genericProperties.getPipelinesTimeoutSec();
    }

    public synchronized String getPolicyVersion() {
        return genericProperties.getPolicyVersion();
    }

    public synchronized String getCloseLoopTargetType() {
        return genericProperties.getClTargetType();
    }

    public synchronized String getCloseLoopEventStatus() {
        return genericProperties.getClEventStatus();
    }

    public synchronized String getCloseLoopVersion() {
        return genericProperties.getClVersion();
    }

    public synchronized String getCloseLoopTarget() {
        return genericProperties.getClTarget();
    }

    public String getCloseLoopOriginator() {
        return genericProperties.getClOriginator();
    }

    public synchronized int getCbsPollingInterval() {
        return cbsPollingInterval;
    }

    public synchronized String getReRegistrationCloseLoopPolicyScope() {
        return genericProperties.getReRegistration().getPolicyScope();
    }

    public synchronized String getReRegistrationCloseLoopControlName() {
        return genericProperties.getReRegistration().getClControlName();
    }

    public synchronized String getCpeAuthenticationCloseLoopPolicyScope() {
        return genericProperties.getCpeAuthentication().getPolicyScope();
    }

    public synchronized String getCpeAuthenticationCloseLoopControlName() {
        return genericProperties.getCpeAuthentication().getClControlName();
    }

    /**
     * Update current configuration based on the new configuration object fetched from Consul via CBS service of DCAE.
     * @param newConfiguration updated configuration object
     */
    public void updateCurrentConfiguration(GeneratedAppConfigObject newConfiguration) {

        synchronized (this) {
            cbsPollingInterval = newConfiguration.cbsPollingIntervalSec();

            securityProperties.setEnableAaiCertAuth(newConfiguration.enableAaiCertAuth());
            securityProperties.setEnableDmaapCertAuth(newConfiguration.enableDmaapCertAuth());
            securityProperties.setKeyStorePath(newConfiguration.keyStorePath());
            securityProperties.setKeyStorePasswordPath(newConfiguration.keyStorePasswordPath());
            securityProperties.setTrustStorePath(newConfiguration.trustStorePath());
            securityProperties.setTrustStorePasswordPath(newConfiguration.trustStorePasswordPath());
            final SecurityKeys securityKeys = ImmutableSecurityKeys.builder()
                    .keyStore(keyStoreFromResource(securityProperties.getKeyStorePath()))
                    .keyStorePassword(fromPath(Paths.get(securityProperties.getKeyStorePasswordPath())))
                    .trustStore(keyStoreFromResource(securityProperties.getTrustStorePath()))
                    .trustStorePassword(fromPath(Paths.get(securityProperties.getTrustStorePasswordPath())))
                    .build();

            var reRegObject =
                    getStreamsObject(newConfiguration.streamSubscribesMap(), newConfiguration.reRegConfigKey(),
                            "PNF Re-Registration");
            var topicUrlInfo = parseTopicUrl(reRegObject.dmaapInfo().topicUrl());
            dmaapReRegistrationConsumerProperties.setDmaapHostName(topicUrlInfo.getHost());
            dmaapReRegistrationConsumerProperties.setDmaapPortNumber(topicUrlInfo.getPort());
            dmaapReRegistrationConsumerProperties.setDmaapProtocol(newConfiguration.dmaapProtocol());
            dmaapReRegistrationConsumerProperties.setDmaapUserName(reRegObject.aafUsername());
            dmaapReRegistrationConsumerProperties.setDmaapUserPassword(reRegObject.aafPassword());
            dmaapReRegistrationConsumerProperties.setDmaapContentType(newConfiguration.dmaapContentType());
            dmaapReRegistrationConsumerProperties.setDmaapTopicName(topicUrlInfo.getTopicName());
            dmaapReRegistrationConsumerProperties.setConsumerId(newConfiguration.dmaapConsumerConsumerId());
            dmaapReRegistrationConsumerProperties.setConsumerGroup(newConfiguration.dmaapConsumerConsumerGroup());
            dmaapReRegistrationConsumerProperties.setMessageLimit(newConfiguration.dmaapMessageLimit());
            dmaapReRegistrationConsumerProperties.setTimeoutMs(newConfiguration.dmaapTimeoutMs());
            constructDmaapReRegistrationConfiguration(securityKeys);

            var cpeAuthObject =
                    getStreamsObject(newConfiguration.streamSubscribesMap(), newConfiguration.cpeAuthConfigKey(),
                            "CPE Authentication");
            topicUrlInfo = parseTopicUrl(cpeAuthObject.dmaapInfo().topicUrl());
            dmaapCpeAuthenticationConsumerProperties.setDmaapHostName(topicUrlInfo.getHost());
            dmaapCpeAuthenticationConsumerProperties.setDmaapPortNumber(topicUrlInfo.getPort());
            dmaapCpeAuthenticationConsumerProperties.setDmaapProtocol(newConfiguration.dmaapProtocol());
            dmaapCpeAuthenticationConsumerProperties.setDmaapUserName(cpeAuthObject.aafUsername());
            dmaapCpeAuthenticationConsumerProperties.setDmaapUserPassword(cpeAuthObject.aafPassword());
            dmaapCpeAuthenticationConsumerProperties.setDmaapContentType(newConfiguration.dmaapContentType());
            dmaapCpeAuthenticationConsumerProperties.setDmaapTopicName(topicUrlInfo.getTopicName());
            dmaapCpeAuthenticationConsumerProperties.setConsumerId(newConfiguration.dmaapConsumerConsumerId());
            dmaapCpeAuthenticationConsumerProperties.setConsumerGroup(newConfiguration.dmaapConsumerConsumerGroup());
            dmaapCpeAuthenticationConsumerProperties.setMessageLimit(newConfiguration.dmaapMessageLimit());
            dmaapCpeAuthenticationConsumerProperties.setTimeoutMs(newConfiguration.dmaapTimeoutMs());
            constructDmaapCpeAuthenticationConfiguration(securityKeys);

            var closeLoopObject =
                    getStreamsObject(newConfiguration.streamPublishesMap(), newConfiguration.closeLoopConfigKey(),
                            "Close Loop");
            topicUrlInfo = parseTopicUrl(closeLoopObject.dmaapInfo().topicUrl());
            dmaapProducerProperties.setDmaapHostName(topicUrlInfo.getHost());
            dmaapProducerProperties.setDmaapPortNumber(topicUrlInfo.getPort());
            dmaapProducerProperties.setDmaapProtocol(newConfiguration.dmaapProtocol());
            dmaapProducerProperties.setDmaapUserName(closeLoopObject.aafUsername());
            dmaapProducerProperties.setDmaapUserPassword(closeLoopObject.aafPassword());
            dmaapProducerProperties.setDmaapContentType(newConfiguration.dmaapContentType());
            dmaapProducerProperties.setDmaapTopicName(topicUrlInfo.getTopicName());
            constructDmaapProducerConfiguration(securityKeys);

            aaiClientProperties.setAaiHost(newConfiguration.aaiHost());
            aaiClientProperties.setAaiPort(newConfiguration.aaiPort());
            aaiClientProperties.setAaiProtocol(newConfiguration.aaiProtocol());
            aaiClientProperties.setAaiUserName(newConfiguration.aaiUsername());
            aaiClientProperties.setAaiUserPassword(newConfiguration.aaiPassword());
            aaiClientProperties.setAaiIgnoreSslCertificateErrors(newConfiguration.aaiIgnoreSslCertificateErrors());
            constructAaiConfiguration();

            genericProperties.setPipelinesPollingIntervalSec(newConfiguration.pipelinesPollingIntervalSec());
            genericProperties.setPipelinesTimeoutSec(newConfiguration.pipelinesTimeoutSec());
            genericProperties.setPolicyVersion(newConfiguration.policyVersion());
            genericProperties.setClTargetType(newConfiguration.closeLoopTargetType());
            genericProperties.setClEventStatus(newConfiguration.closeLoopEventStatus());
            genericProperties.setClVersion(newConfiguration.closeLoopVersion());
            genericProperties.setClTarget(newConfiguration.closeLoopTarget());
            genericProperties.setClOriginator(newConfiguration.closeLoopOriginator());
            genericProperties.getReRegistration().setPolicyScope(newConfiguration.reRegistrationPolicyScope());
            genericProperties.getReRegistration().setClControlName(newConfiguration.reRegistrationClControlName());
            genericProperties.getCpeAuthentication().setPolicyScope(newConfiguration.cpeAuthPolicyScope());
            genericProperties.getCpeAuthentication().setClControlName(newConfiguration.cpeAuthClControlName());

            LoggingUtil.changeLoggingLevel(newConfiguration.loggingLevel());
        }

        notifyObservers();
    }

    @NotNull
    private GeneratedAppConfigObject.StreamsObject getStreamsObject(
            Map<String, GeneratedAppConfigObject.StreamsObject> map, String configKey, String messageName) {
        var streamsObject = map.get(configKey);
        if (!STREAMS_TYPE.equals(streamsObject.type())) {
            throw new ApplicationEnvironmentException(String.format("%s requires information about"
                    + " message-router topic in ONAP", messageName));
        }
        return streamsObject;
    }

    private void constructConfigurationObjects() {
        final SecurityKeys securityKeysForReRegistration = ImmutableSecurityKeys.builder()
                .keyStore(keyStoreFromResource(securityProperties.getKeyStorePath()))
                .keyStorePassword(fromPath(Paths.get(securityProperties.getKeyStorePasswordPath())))
                .trustStore(keyStoreFromResource(securityProperties.getTrustStorePath()))
                .trustStorePassword(fromPath(Paths.get(securityProperties.getTrustStorePasswordPath())))
                .build();
        constructDmaapReRegistrationConfiguration(securityKeysForReRegistration);
        final SecurityKeys securityKeysForCpeAuthentication = ImmutableSecurityKeys.builder()
                .keyStore(keyStoreFromResource(securityProperties.getKeyStorePath()))
                .keyStorePassword(fromPath(Paths.get(securityProperties.getKeyStorePasswordPath())))
                .trustStore(keyStoreFromResource(securityProperties.getTrustStorePath()))
                .trustStorePassword(fromPath(Paths.get(securityProperties.getTrustStorePasswordPath())))
                .build();
        constructDmaapCpeAuthenticationConfiguration(securityKeysForCpeAuthentication);
        final SecurityKeys securityKeysForProducer = ImmutableSecurityKeys.builder()
                .keyStore(keyStoreFromResource(securityProperties.getKeyStorePath()))
                .keyStorePassword(fromPath(Paths.get(securityProperties.getKeyStorePasswordPath())))
                .trustStore(keyStoreFromResource(securityProperties.getTrustStorePath()))
                .trustStorePassword(fromPath(Paths.get(securityProperties.getTrustStorePasswordPath())))
                .build();
        constructDmaapProducerConfiguration(securityKeysForProducer);
        constructAaiConfiguration();
    }

    private void constructDmaapReRegistrationConfiguration(final SecurityKeys securityKeys) {
        dmaapReRegistrationConsumerConfiguration = ImmutableMessageRouterSubscriberConfig.builder()
                .securityKeys(securityKeys)
                .build();
    }

    private void constructDmaapCpeAuthenticationConfiguration(final SecurityKeys securityKeys) {
        dmaapCpeAuthenticationConsumerConfiguration = ImmutableMessageRouterSubscriberConfig.builder()
                .securityKeys(securityKeys)
                .build();
    }

    private void constructDmaapProducerConfiguration(final SecurityKeys securityKeys) {
        dmaapPublisherConfiguration = ImmutableMessageRouterPublisherConfig.builder()
                .securityKeys(securityKeys)
                .build();
    }

    private void constructAaiConfiguration() {
        aaiClientConfiguration = new ImmutableAaiClientConfiguration.Builder()
                .aaiHost(aaiClientProperties.getAaiHost())
                .aaiPort(aaiClientProperties.getAaiPort())
                .aaiProtocol(aaiClientProperties.getAaiProtocol())
                .aaiUserName(aaiClientProperties.getAaiUserName())
                .aaiUserPassword(aaiClientProperties.getAaiUserPassword())
                .aaiHeaders(aaiClientProperties.getAaiHeaders())
                .aaiIgnoreSslCertificateErrors(aaiClientProperties.isAaiIgnoreSslCertificateErrors())
                .enableAaiCertAuth(securityProperties.isEnableAaiCertAuth())
                .keyStorePath(securityProperties.getKeyStorePath())
                .keyStorePasswordPath(securityProperties.getKeyStorePasswordPath())
                .trustStorePath(securityProperties.getTrustStorePath())
                .trustStorePasswordPath(securityProperties.getTrustStorePasswordPath())
                .build();
    }

    private TopicUrlInfo parseTopicUrl(String topicUrl) {
        var urlTokens = topicUrl.split(":");
        if (urlTokens.length != 3) {
            throw new ConfigurationParsingException("Wrong topic URL format");
        }
        var topicUrlInfo = new TopicUrlInfo();
        topicUrlInfo.setHost(urlTokens[1].replace("/", ""));

        var tokensAfterHost = urlTokens[2].split("/events/");
        if (tokensAfterHost.length != 2) {
            throw new ConfigurationParsingException("Wrong topic name structure");
        }
        topicUrlInfo.setPort(Integer.valueOf(tokensAfterHost[0]));
        topicUrlInfo.setTopicName(tokensAfterHost[1]);

        return topicUrlInfo;
    }

    private static class TopicUrlInfo {
        private String host;
        private int port;
        private String topicName;

        String getHost() {
            return host;
        }

        void setHost(String host) {
            this.host = host;
        }

        int getPort() {
            return port;
        }

        void setPort(int port) {
            this.port = port;
        }

        String getTopicName() {
            return topicName;
        }

        void setTopicName(String topicName) {
            this.topicName = topicName;
        }
    }
}
