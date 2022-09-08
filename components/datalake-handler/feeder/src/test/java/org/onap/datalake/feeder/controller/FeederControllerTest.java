/*-
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE
 * ================================================================================
 * Copyright (C) 2018-2019 Huawei. All rights reserved.
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
package org.onap.datalake.feeder.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.service.PullService;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeederControllerTest {
	@Mock
	private PullService pullService1;

	@Mock
	private ApplicationConfiguration config;

	@InjectMocks
	private FeederController feederController;

	@Test
	public void testStart() throws IOException {
		when(pullService1.isRunning()).thenReturn(true);
		String start = feederController.start();
		assertEquals("{\"running\": true}", start);

		when(pullService1.isRunning()).thenReturn(false);
		start = feederController.start();
		assertEquals("{\"running\": true}", start);
	}

	@Test
	public void testStop() {
		when(pullService1.isRunning()).thenReturn(true);
		String stop = feederController.stop();
		assertEquals("{\"running\": false}", stop);

		when(pullService1.isRunning()).thenReturn(false);
		stop = feederController.stop();
		assertEquals("{\"running\": false}", stop);
	}

	@Test
	public void testStatus() {
		when(pullService1.isRunning()).thenReturn(false);
		when(config.getDatalakeVersion()).thenReturn("0.0.1");

		String status = feederController.status();
		assertEquals("{\"version\": \"0.0.1\", \"running\": false}", status);
	}
}
