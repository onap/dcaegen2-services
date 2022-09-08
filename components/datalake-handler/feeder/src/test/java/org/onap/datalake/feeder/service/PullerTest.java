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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.util.TestUtil;
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

	private Puller puller;

	@Mock
	private ApplicationContext context;

	@Mock
	private ApplicationConfiguration config;

	@Mock
	private StoreService storeService;

	@Mock
	private TopicConfigPollingService topicConfigPollingService;

	@Before
	public void init() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		Kafka kafka = TestUtil.newKafka("kafka");
		kafka.setBrokerList("brokerList:1,brokerList2:1");
		kafka.setGroup("group");
		kafka.setLogin("login");
		kafka.setSecure(true);
		kafka.setSecurityProtocol("securityProtocol");
		puller = new Puller(kafka);

		Field configField = Puller.class.getDeclaredField("config");
		configField.setAccessible(true);
		configField.set(puller, config);

		when(config.isAsync()).thenReturn(true);
		Method initMethod = Puller.class.getDeclaredMethod("init");
		initMethod.setAccessible(true);
		initMethod.invoke(puller);
	}

	@Test
	public void testRun() throws InterruptedException {
		Thread thread = new Thread(puller);
		thread.start();

		Thread.sleep(50);
		puller.shutdown();
		thread.join();
	}

}