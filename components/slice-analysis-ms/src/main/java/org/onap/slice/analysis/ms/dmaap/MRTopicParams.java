/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 * Copyright (C) 2018-2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2019 Nordix Foundation.
 * Copyright (C) 2022 Huawei Canada Limited.
 * Copyright (C) 2022 CTC, Inc.
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

package org.onap.slice.analysis.ms.dmaap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Partially copied from Onap Policy
 * policy/common/policy-endpoints/src/main/java/org/onap/policy/common/endpoints/event/comm/bus/internal/BusTopicParams.java
 * Modified to fit this project.
 * Member variables of this Params class are as follows.
 *
 * <p>servers DMaaP servers
 * topic DMaaP Topic to be monitored
 * apiKey DMaaP API Key (optional)
 * apiSecret DMaaP API Secret (optional)
 * consumerGroup DMaaP Reader Consumer Group
 * consumerInstance DMaaP Reader Instance
 * fetchTimeout DMaaP fetch timeout
 * fetchLimit DMaaP fetch limit
 * environment DME2 Environment
 * aftEnvironment DME2 AFT Environment
 * partner DME2 Partner
 * latitude DME2 Latitude
 * longitude DME2 Longitude
 * additionalProps Additional properties to pass to DME2
 * useHttps does connection use HTTPS?
 * allowSelfSignedCerts are self-signed certificates allow
 */
@Getter
@Setter
public class MRTopicParams {

    private int port;
    private List<String> servers;
    private Map<String, String> additionalProps;
    private String topic;
    private String effectiveTopic;
    private String apiKey;
    private String apiSecret;
    private String consumerGroup;
    private String consumerInstance;
    private int fetchTimeout;
    private int fetchLimit;
    private boolean useHttps;
    private boolean allowSelfSignedCerts;
    private boolean managed;

    private String userName;
    private String password;
    private String environment;
    private String aftEnvironment;
    private String partner;
    private String latitude;
    private String longitude;
    private String partitionId;
    private String clientName;
    private String hostname;
    private String basePath;
    @Getter
    private String serializationProvider;

    public static TopicParamsBuilder builder() {
        return new TopicParamsBuilder();
    }

    /**
     * Methods to Check if the property is INVALID.
     */

    public boolean isEnvironmentInvalid() {
        return StringUtils.isBlank(environment);
    }

    public boolean isAftEnvironmentInvalid() {
        return StringUtils.isBlank(aftEnvironment);
    }

    public boolean isLatitudeInvalid() {
        return StringUtils.isBlank(latitude);
    }

    public boolean isLongitudeInvalid() {
        return StringUtils.isBlank(longitude);
    }

    public boolean isConsumerInstanceInvalid() {
        return StringUtils.isBlank(consumerInstance);
    }

    public boolean isConsumerGroupInvalid() {
        return StringUtils.isBlank(consumerGroup);
    }

    public boolean isClientNameInvalid() {
        return StringUtils.isBlank(clientName);
    }

    public boolean isPartnerInvalid() {
        return StringUtils.isBlank(partner);
    }

    public boolean isServersInvalid() {
        return (servers == null || servers.isEmpty()
                || (servers.size() == 1 && ("".equals(servers.get(0)))));
    }

    public boolean isTopicInvalid() {
        return StringUtils.isBlank(topic);
    }

    public boolean isPartitionIdInvalid() {
        return StringUtils.isBlank(partitionId);
    }

    public boolean isHostnameInvalid() {
        return StringUtils.isBlank(hostname);
    }

    public boolean isPortInvalid() {
        return  (port <= 0 || port >= 65535);
    }

    /**
     * Methods to Check if the property is Valid.
     */

    public boolean isApiKeyValid() {
        return StringUtils.isNotBlank(apiKey);
    }

    public boolean isApiSecretValid() {
        return StringUtils.isNotBlank(apiSecret);
    }

    public boolean isUserNameValid() {
        return StringUtils.isNotBlank(userName);
    }

    public boolean isPasswordValid() {
        return StringUtils.isNotBlank(password);
    }

    public boolean isAdditionalPropsValid() {
        return additionalProps != null;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TopicParamsBuilder {

        final MRTopicParams params = new MRTopicParams();

        public TopicParamsBuilder servers(List<String> servers) {
            this.params.servers = servers;
            return this;
        }

        public TopicParamsBuilder topic(String topic) {
            this.params.topic = topic;
            return this;
        }

        public TopicParamsBuilder effectiveTopic(String effectiveTopic) {
            this.params.effectiveTopic = effectiveTopic;
            return this;
        }

        public TopicParamsBuilder apiKey(String apiKey) {
            this.params.apiKey = apiKey;
            return this;
        }

        public TopicParamsBuilder apiSecret(String apiSecret) {
            this.params.apiSecret = apiSecret;
            return this;
        }

        public TopicParamsBuilder consumerGroup(String consumerGroup) {
            this.params.consumerGroup = consumerGroup;
            return this;
        }

        public TopicParamsBuilder consumerInstance(String consumerInstance) {
            this.params.consumerInstance = consumerInstance;
            return this;
        }

        public TopicParamsBuilder fetchTimeout(int fetchTimeout) {
            this.params.fetchTimeout = fetchTimeout;
            return this;
        }

        public TopicParamsBuilder fetchLimit(int fetchLimit) {
            this.params.fetchLimit = fetchLimit;
            return this;
        }

        public TopicParamsBuilder useHttps(boolean useHttps) {
            this.params.useHttps = useHttps;
            return this;
        }

        public TopicParamsBuilder allowSelfSignedCerts(boolean allowSelfSignedCerts) {
            this.params.allowSelfSignedCerts = allowSelfSignedCerts;
            return this;
        }

        public TopicParamsBuilder userName(String userName) {
            this.params.userName = userName;
            return this;
        }

        public TopicParamsBuilder password(String password) {
            this.params.password = password;
            return this;
        }

        public TopicParamsBuilder environment(String environment) {
            this.params.environment = environment;
            return this;
        }

        public TopicParamsBuilder aftEnvironment(String aftEnvironment) {
            this.params.aftEnvironment = aftEnvironment;
            return this;
        }

        public TopicParamsBuilder partner(String partner) {
            this.params.partner = partner;
            return this;
        }

        public TopicParamsBuilder latitude(String latitude) {
            this.params.latitude = latitude;
            return this;
        }

        public TopicParamsBuilder longitude(String longitude) {
            this.params.longitude = longitude;
            return this;
        }

        public TopicParamsBuilder additionalProps(Map<String, String> additionalProps) {
            this.params.additionalProps = additionalProps;
            return this;
        }

        public TopicParamsBuilder partitionId(String partitionId) {
            this.params.partitionId = partitionId;
            return this;
        }

        public MRTopicParams build() {
            return params;
        }

        public TopicParamsBuilder buildFromConfigJson(JsonObject jsonObject) {
            String consumerGroup = null;
            String consumerInstance = null;
            String aafUsername = null;
            String aafPassword = null;
            List<String> servers = new ArrayList<>();
            String topic = null;
            boolean useHttps = false;
            int fetchTimeout = -1;
            int fetchLimit = -1;

            if (jsonObject.has("consumer_group") && !jsonObject.get("consumer_group").isJsonNull()) {
                consumerGroup = jsonObject.get("consumer_group").getAsString();
            }
            if (jsonObject.has("consumer_instance") && !jsonObject.get("consumer_instance").isJsonNull()) {
                consumerInstance = jsonObject.get("consumer_instance").getAsString();
            }
            if (jsonObject.has("aaf_username") && !jsonObject.get("aaf_username").isJsonNull()) {
                aafUsername = jsonObject.get("aaf_username").getAsString();
            }
            if (jsonObject.has("aaf_password") && !jsonObject.get("aaf_password").isJsonNull()) {
                aafPassword = jsonObject.get("aaf_password").getAsString();
            }
            if (jsonObject.has("fetch_timeout") && !jsonObject.get("fetch_timeout").isJsonNull()) {
                fetchTimeout = jsonObject.get("fetch_timeout").getAsInt();
            }
            if (jsonObject.has("fetch_limit") && !jsonObject.get("fetch_limit").isJsonNull()) {
                fetchLimit = jsonObject.get("fetch_limit").getAsInt();
            }
            if (jsonObject.has("servers") && !jsonObject.get("servers").isJsonNull()) {
                JsonArray jsonArray = jsonObject.get("servers").getAsJsonArray();
                servers = new ArrayList<>();
                for (int i=0, e=jsonArray.size(); i<e; i++){
                    servers.add(jsonArray.get(i).getAsString());
                }
            }

            String topicUrl = jsonObject.get("dmaap_info").getAsJsonObject().get("topic_url").getAsString();
            if (topicUrl.startsWith("https")){
                useHttps = true;
            }
            String[] pmTopicSplit = topicUrl.split("\\/");
            topic = pmTopicSplit[pmTopicSplit.length - 1];

            this.params.topic = topicUrl;
            this.params.servers = servers;
            this.params.consumerGroup = consumerGroup;
            this.params.consumerInstance = consumerInstance;
            this.params.password = aafPassword;
            this.params.userName = aafUsername;
            this.params.fetchTimeout = fetchTimeout;
            this.params.fetchLimit = fetchLimit;
            this.params.useHttps = useHttps;
            return this;
        }

        public TopicParamsBuilder managed(boolean managed) {
            this.params.managed = managed;
            return this;
        }

        public TopicParamsBuilder hostname(String hostname) {
            this.params.hostname = hostname;
            return this;
        }

        public TopicParamsBuilder clientName(String clientName) {
            this.params.clientName = clientName;
            return this;
        }

        public TopicParamsBuilder port(int port) {
            this.params.port = port;
            return this;
        }

        public TopicParamsBuilder basePath(String basePath) {
            this.params.basePath = basePath;
            return this;
        }

        public TopicParamsBuilder serializationProvider(String serializationProvider) {
            this.params.serializationProvider = serializationProvider;
            return this;
        }

    }
}
