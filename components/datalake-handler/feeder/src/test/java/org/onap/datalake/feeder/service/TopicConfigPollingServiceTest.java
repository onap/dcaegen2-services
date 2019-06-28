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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Kafka;

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

	@Test
	public void testRun() {
		
	}
	
	/*
	public void testInit() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
		Method init = topicConfigPollingService.getClass().getDeclaredMethod("init");
		init.setAccessible(true);
		init.invoke(topicConfigPollingService);

		List<String> activeTopics = Arrays.asList("test");
		Field activeTopicsField = topicConfigPollingService.getClass().getDeclaredField("activeTopics");
		activeTopicsField.setAccessible(true);
		activeTopicsField.set(topicConfigPollingService, activeTopics);
	}

	@Test
	public void testRun() throws InterruptedException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
		testInit();

		//when(config.getDmaapCheckNewTopicInterval()).thenReturn(1);

		Thread thread = new Thread(topicConfigPollingService);
		thread.start();

		Thread.sleep(50);
		topicConfigPollingService.shutdown();
		thread.join();

		assertTrue(topicConfigPollingService.isActiveTopicsChanged(new Kafka()));
	}

	@Test
	public void testRunNoChange() throws InterruptedException {
	
//		when(config.getDmaapCheckNewTopicInterval()).thenReturn(1);

		Thread thread = new Thread(topicConfigPollingService);
		thread.start();

		Thread.sleep(50);
		topicConfigPollingService.shutdown();
		thread.join();

		assertFalse(topicConfigPollingService.isActiveTopicsChanged(new Kafka()));
	}

	@Test
	public void testGet() {
		Kafka kafka=null;
		assertNull(topicConfigPollingService.getEffectiveTopic (new Kafka(), "test"));
		assertNull(topicConfigPollingService.getActiveTopics(kafka));

	}
	*/
}