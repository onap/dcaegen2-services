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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PullServiceTest {

	@InjectMocks
	private PullService pullService;

	@Mock
	private ApplicationContext context;

	@Mock
	private ApplicationConfiguration config;

	@Mock
	private ExecutorService executorService;

	@Mock
	private List<Puller> consumers;

	@Test
	public void isRunning() {
		assertFalse(pullService.isRunning());
	}

	@Test(expected = NullPointerException.class)
	public void start() {
		setRunning(false);
		pullService.start();
		setRunning(true);
		pullService.start();
	}

	@Test
	public void shutdown() {
		when(config.getShutdownLock()).thenReturn(new ReentrantReadWriteLock());
		setRunning(false);
		pullService.shutdown();
		setRunning(true);
		pullService.shutdown();
	}

	private void setRunning(boolean running) {
		Field configField;
		try {
			configField = PullService.class.getDeclaredField("isRunning");
			configField.setAccessible(true);
			configField.set(pullService, running);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}