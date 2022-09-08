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

package org.onap.datalake.feeder.service.db;

import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.util.TestUtil;
import org.springframework.context.ApplicationContext;

/**
 * Test HdfsService
 * 
 * @author Guobiao Mo
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class HdfsServiceTest {
	private HdfsService hdfsService;

	@Mock
	private ApplicationContext context;

	@Mock
	private ApplicationConfiguration config;

	@Mock
	private ExecutorService executorService;

	@Before
	public void init() throws NoSuchFieldException, IllegalAccessException { 
		Db db = TestUtil.newDb("HDFS");
		db.setHost("host");
		db.setLogin("login");
		hdfsService = new HdfsService(db);

		Field configField = HdfsService.class.getDeclaredField("config");
		configField.setAccessible(true);
		configField.set(hdfsService, config);
		
		hdfsService.init();
	}
	
	@Test(expected = NullPointerException.class)
	public void saveJsons() { 
		when(config.getHdfsBufferSize()).thenReturn(1000);	

		when(config.isAsync()).thenReturn(true);
		TestUtil.testSaveJsons(config , hdfsService);

		when(config.isAsync()).thenReturn(false);	
		TestUtil.testSaveJsons(config , hdfsService);
	}

	@Test(expected = NullPointerException.class)
	public void cleanUp() {
		when(config.getShutdownLock()).thenReturn(new ReentrantReadWriteLock());
		hdfsService.flush();
		hdfsService.flushStall();
		hdfsService.cleanUp();
	}
}