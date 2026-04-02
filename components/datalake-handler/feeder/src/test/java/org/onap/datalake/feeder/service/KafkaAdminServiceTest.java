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

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.KafkaFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.util.TestUtil;

@RunWith(MockitoJUnitRunner.class)
public class KafkaAdminServiceTest {

    private KafkaAdminService kafkaAdminService;

    @Mock
    private ApplicationConfiguration config;
    @Mock
    private TopicService topicService;
    @Mock
    private AdminClient adminClient;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Kafka kafka = TestUtil.newKafka("kafka");
        kafkaAdminService = new KafkaAdminService(kafka);

        Field configField = KafkaAdminService.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(kafkaAdminService, config);

        Field adminClientField = KafkaAdminService.class.getDeclaredField("adminClient");
        adminClientField.setAccessible(true);
        adminClientField.set(kafkaAdminService, adminClient);
    }

    @Test
    public void testGetTopics() throws Exception {
        when(config.getKafkaBootstrapServers()).thenReturn("localhost:9092");
        when(config.getKafkaExcludedTopics()).thenReturn("__consumer_offsets,__transaction_state");

        Set<String> topicSet = new HashSet<>();
        topicSet.add("unauthenticated.DCAE_CL_OUTPUT");
        topicSet.add("__consumer_offsets");
        topicSet.add("unauthenticated.SEC_FAULT_OUTPUT");

        ListTopicsResult listTopicsResult = mock(ListTopicsResult.class);
        when(listTopicsResult.names()).thenReturn(KafkaFuture.completedFuture(topicSet));
        when(adminClient.listTopics()).thenReturn(listTopicsResult);

        List<String> result = kafkaAdminService.getTopics();
        assertNotNull(result);
        assertTrue(result.contains("unauthenticated.DCAE_CL_OUTPUT"));
        assertTrue(result.contains("unauthenticated.SEC_FAULT_OUTPUT"));
        assertTrue(!result.contains("__consumer_offsets"));

        when(config.getShutdownLock()).thenReturn(new ReentrantReadWriteLock());
        kafkaAdminService.cleanUp();
    }

    @Test
    public void testGetActiveTopicConfigs() throws IOException {
        when(config.getKafkaBootstrapServers()).thenReturn("localhost:9092");
        when(config.getKafkaExcludedTopics()).thenReturn("__consumer_offsets");

        ListTopicsResult listTopicsResult = mock(ListTopicsResult.class);
        when(listTopicsResult.names()).thenReturn(KafkaFuture.completedFuture(Collections.emptySet()));
        when(adminClient.listTopics()).thenReturn(listTopicsResult);

        try {
            assertNotNull(kafkaAdminService.getActiveEffectiveTopic());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
