/*
* ============LICENSE_START=======================================================
* ONAP : DATALAKE
* ================================================================================
* Copyright 2019 China Mobile
* Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

package org.onap.datalake.feeder.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.collections.CollectionUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.EffectiveTopic;
import org.onap.datalake.feeder.domain.Kafka;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * This service will handle all the communication with Kafka
 *
 * @author Guobiao Mo
 *
 */
@Service
@Scope("prototype")
public class KafkaAdminService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationConfiguration config;

    @Autowired
    private TopicService topicService;

    private AdminClient adminClient;

    private Kafka kafka;

    public KafkaAdminService(Kafka kafka) {
        this.kafka = kafka;
    }

    @PreDestroy
    public void cleanUp() {
        config.getShutdownLock().readLock().lock();

        try {
            if (adminClient != null) {
                log.info("cleanUp() called, closing AdminClient.");
                adminClient.close();
            }
        } finally {
            config.getShutdownLock().readLock().unlock();
        }
    }

    @PostConstruct
    private void init() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaBootstrapServers());
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, config.getKafkaTimeout() * 1000);

        if (config.isKafkaSecure()) {
            String jaas = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                    + config.getKafkaLogin() + "\" password=\"" + config.getKafkaPass() + "\";";
            props.put("sasl.jaas.config", jaas);
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, config.getKafkaSecurityProtocol());
            props.put("sasl.mechanism", "PLAIN");
        }

        adminClient = AdminClient.create(props);
    }

    //get all topic names from Kafka using AdminClient
    //This method returns empty list if nothing found.
    public List<String> getTopics() {
        try {
            log.info("Listing topics from Kafka bootstrap servers: {}", config.getKafkaBootstrapServers());
            Set<String> topics = new HashSet<>(adminClient.listTopics().names().get());
            String excludedTopics = config.getKafkaExcludedTopics();
            if (excludedTopics != null && !excludedTopics.isEmpty()) {
                topics.removeAll(Arrays.asList(excludedTopics.split(",")));
            }
            log.info("list of topics: {}", topics);
            return new ArrayList<>(topics);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while listing topics from Kafka.", e);
            return Collections.emptyList();
        } catch (ExecutionException e) {
            log.error("Can not get topic list from Kafka, return empty list.", e);
            return Collections.emptyList();
        }
    }

    public Map<String, List<EffectiveTopic>> getActiveEffectiveTopic() throws IOException {
        log.debug("entering getActiveTopicConfigs()...");
        List<String> allTopics = getTopics();

        Map<String, List<EffectiveTopic>> ret = new HashMap<>();
        for (String topicStr : allTopics) {
            log.debug("get topic setting from DB: {}.", topicStr);

            List<EffectiveTopic> effectiveTopics= topicService.getEnabledEffectiveTopic(kafka, topicStr, true);
            if(CollectionUtils.isNotEmpty(effectiveTopics )) {
                log.debug("add effectiveTopics  {}:{}.", topicStr, effectiveTopics);
                ret.put(topicStr , effectiveTopics);
            }

        }
        return ret;
    }

}
