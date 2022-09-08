/*
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE
 * ================================================================================
 * Copyright 2019 China Mobile
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.util.TestUtil;

/**
 * Test TopicConfigPollingService
 * 
 * @author Guobiao Mo
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TopicConfigPollingServiceTest {
	@Mock
	private ApplicationConfiguration config;

	@Mock
	private DmaapService dmaapService;

	@InjectMocks
	private TopicConfigPollingService topicConfigPollingService = new TopicConfigPollingService();

	static String KAFKA_NAME = "kafka1";

	@Before
	public void init() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
		Method init = topicConfigPollingService.getClass().getDeclaredMethod("init");
		init.setAccessible(true);
		init.invoke(topicConfigPollingService);

		Set<String> activeTopics = new HashSet<>(Arrays.asList("test"));
		Map<Integer, Set<String>> activeTopicMap = new HashMap<>();
		activeTopicMap.put(1, activeTopics);

		Field activeTopicsField = TopicConfigPollingService.class.getDeclaredField("activeTopicMap");
		activeTopicsField.setAccessible(true);
		activeTopicsField.set(topicConfigPollingService, activeTopicMap);

		Method initMethod = TopicConfigPollingService.class.getDeclaredMethod("init");
		initMethod.setAccessible(true);
		initMethod.invoke(topicConfigPollingService);
	}

	@Test
	public void testRun() throws InterruptedException {

		when(config.getCheckTopicInterval()).thenReturn(1L);

		Thread thread = new Thread(topicConfigPollingService);
		thread.start();

		Thread.sleep(50);
		topicConfigPollingService.shutdown();
		thread.join();

		assertTrue(topicConfigPollingService.isActiveTopicsChanged(new Kafka()));
	}

	@Test
	public void testRunNoChange() throws InterruptedException {

		when(config.getCheckTopicInterval()).thenReturn(1L);

		Thread thread = new Thread(topicConfigPollingService);
		thread.start();

		Thread.sleep(50);
		topicConfigPollingService.shutdown();
		thread.join();

		assertTrue(topicConfigPollingService.isActiveTopicsChanged(new Kafka()));
	}

	@Test
	public void testGet() {
		Kafka kafka = TestUtil.newKafka(KAFKA_NAME);
		kafka.setId(1);
		//assertNull(topicConfigPollingService.getEffectiveTopic (kafka, "test"));
		assertNotNull(topicConfigPollingService.getActiveTopics(kafka));

	}

}