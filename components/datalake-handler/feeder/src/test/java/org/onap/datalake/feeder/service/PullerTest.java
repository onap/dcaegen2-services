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

import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.springframework.context.ApplicationContext;

/**
 * Test Puller
 * 
 * Without a Kafka server, the coverage is low.
 * 
 * @author Guobiao Mo
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PullerTest {

	@InjectMocks
	private Puller puller = new Puller();

	@Mock
	private ApplicationContext context;

	@Mock
	private ApplicationConfiguration config;

	@Mock
	private StoreService storeService;

	@Mock
	private TopicConfigPollingService topicConfigPollingService;

	public void testInit() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		when(config.isAsync()).thenReturn(true);

		Method init = puller.getClass().getDeclaredMethod("init");
		init.setAccessible(true);
		init.invoke(puller);
	}

	@Test
	public void testRun() throws InterruptedException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
		testInit();

		when(config.getDmaapKafkaHostPort()).thenReturn("test:1000");
		when(config.getDmaapKafkaGroup()).thenReturn("test");
		when(config.getDmaapKafkaLogin()).thenReturn("login");
		when(config.getDmaapKafkaPass()).thenReturn("pass");
		when(config.getDmaapKafkaSecurityProtocol()).thenReturn("TEXT");

		Thread thread = new Thread(puller);
		thread.start();

		Thread.sleep(50);
		puller.shutdown();
		thread.join();

	}

}