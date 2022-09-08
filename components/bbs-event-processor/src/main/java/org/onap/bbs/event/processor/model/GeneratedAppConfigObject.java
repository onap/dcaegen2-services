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

package org.onap.bbs.event.processor.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters(fieldNamingStrategy = true, emptyAsNulls = true)
public interface GeneratedAppConfigObject {

    @SerializedName(value = "dmaap.protocol", alternate = "dmaap.protocol")
    String dmaapProtocol();

    @SerializedName(value = "dmaap.contentType", alternate = "dmaap.contentType")
    String dmaapContentType();

    @SerializedName(value = "dmaap.consumer.consumerId", alternate = "dmaap.consumer.consumerId")
    String dmaapConsumerConsumerId();

    @SerializedName(value = "dmaap.consumer.consumerGroup", alternate = "dmaap.consumer.consumerGroup")
    String dmaapConsumerConsumerGroup();

    @SerializedName(value = "dmaap.messageLimit", alternate = "dmaap.messageLimit")
    int dmaapMessageLimit();

    @SerializedName(value = "dmaap.timeoutMs", alternate = "dmaap.timeoutMs")
    int dmaapTimeoutMs();

    @SerializedName(value = "aai.host", alternate = "aai.host")
    String aaiHost();

    @SerializedName(value = "aai.port", alternate = "aai.port")
    int aaiPort();

    @SerializedName(value = "aai.protocol", alternate = "aai.protocol")
    String aaiProtocol();

    @SerializedName(value = "aai.username", alternate = "aai.username")
    String aaiUsername();

    @SerializedName(value = "aai.password", alternate = "aai.password")
    String aaiPassword();

    @SerializedName(value = "aai.aaiIgnoreSslCertificateErrors", alternate = "aai.aaiIgnoreSslCertificateErrors")
    boolean aaiIgnoreSslCertificateErrors();

    @SerializedName(value = "application.pipelinesPollingIntervalSec",
            alternate = "application.pipelinesPollingIntervalSec")
    int pipelinesPollingIntervalSec();

    @SerializedName(value = "application.pipelinesTimeoutSec", alternate = "application.pipelinesTimeoutSec")
    int pipelinesTimeoutSec();

    @SerializedName(value = "application.cbsPollingIntervalSec", alternate = "application.cbsPollingIntervalSec")
    int cbsPollingIntervalSec();

    @SerializedName(value = "application.reregistration.policyScope",
            alternate = "application.reregistration.policyScope")
    String reRegistrationPolicyScope();

    @SerializedName(value = "application.reregistration.clControlName",
            alternate = "application.reregistration.clControlName")
    String reRegistrationClControlName();

    @SerializedName(value = "application.cpe.authentication.policyScope",
            alternate = "application.reregistration.policyScope")
    String cpeAuthPolicyScope();

    @SerializedName(value = "application.cpe.authentication.clControlName",
            alternate = "application.reregistration.clControlName")
    String cpeAuthClControlName();

    @SerializedName(value = "application.policyVersion", alternate = "application.policyVersion")
    String policyVersion();

    @SerializedName(value = "application.clTargetType", alternate = "application.clTargetType")
    String closeLoopTargetType();

    @SerializedName(value = "application.clEventStatus", alternate = "application.clEventStatus")
    String closeLoopEventStatus();

    @SerializedName(value = "application.clVersion", alternate = "application.clVersion")
    String closeLoopVersion();

    @SerializedName(value = "application.clTarget", alternate = "application.clTarget")
    String closeLoopTarget();

    @SerializedName(value = "application.clOriginator", alternate = "application.clOriginator")
    String closeLoopOriginator();

    @SerializedName(value = "application.reregistration.configKey", alternate = "application.reregistration.configKey")
    String reRegConfigKey();

    @SerializedName(value = "application.cpeAuth.configKey", alternate = "application.cpeAuth.configKey")
    String cpeAuthConfigKey();

    @SerializedName(value = "application.closeLoop.configKey", alternate = "application.closeLoop.configKey")
    String closeLoopConfigKey();

    @SerializedName(value = "application.loggingLevel", alternate = "application.loggingLevel")
    String loggingLevel();

    @SerializedName(value = "application.ssl.trustStorePath", alternate = "application.ssl.trustStorePath")
    String trustStorePath();

    @SerializedName(value = "application.ssl.trustStorePasswordPath",
            alternate = "application.ssl.trustStorePasswordPath")
    String trustStorePasswordPath();

    @SerializedName(value = "application.ssl.keyStorePath", alternate = "application.ssl.keyStorePath")
    String keyStorePath();

    @SerializedName(value = "application.ssl.keyStorePasswordPath", alternate = "application.ssl.keyStorePasswordPath")
    String keyStorePasswordPath();

    @SerializedName(value = "application.ssl.enableAaiCertAuth", alternate = "application.ssl.enableAaiCertAuth")
    boolean enableAaiCertAuth();

    @SerializedName(value = "application.ssl.enableDmaapCertAuth", alternate = "application.ssl.enableDmaapCertAuth")
    boolean enableDmaapCertAuth();

    @SerializedName(value = "streams_subscribes", alternate = "streams_subscribes")
    Map<String, StreamsObject> streamSubscribesMap();

    @SerializedName(value = "streams_publishes", alternate = "streams_publishes")
    Map<String, StreamsObject> streamPublishesMap();

    @Value.Immutable
    @Gson.TypeAdapters(fieldNamingStrategy = true)
    interface StreamsObject {

        @SerializedName(value = "type", alternate = "type")
        String type();

        @SerializedName(value = "aaf_username", alternate = "aaf_username")
        String aafUsername();

        @SerializedName(value = "aaf_password", alternate = "aaf_password")
        String aafPassword();

        @SerializedName(value = "dmaap_info", alternate = "dmaap_info")
        DmaapInfo dmaapInfo();
    }

    @Value.Immutable
    @Gson.TypeAdapters(fieldNamingStrategy = true)
    interface DmaapInfo {

        @SerializedName(value = "client_id", alternate = "client_id")
        String clientId();

        @SerializedName(value = "client_role", alternate = "client_role")
        String clientRole();

        @SerializedName(value = "location", alternate = "location")
        String location();

        @SerializedName(value = "topic_url", alternate = "topic_url")
        String topicUrl();
    }
}
