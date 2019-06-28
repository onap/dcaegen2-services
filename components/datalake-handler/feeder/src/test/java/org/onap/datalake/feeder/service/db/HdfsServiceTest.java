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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.dto.TopicConfig;
import org.onap.datalake.feeder.service.db.HdfsService;
import org.springframework.context.ApplicationContext;

/**
 * Test HdfsService
 * 
 * @author Guobiao Mo
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class HdfsServiceTest {

	@InjectMocks
	private HdfsService hdfsService;

	@Mock
	private ApplicationContext context;

	@Mock
	private ApplicationConfiguration config;

	@Mock
	private ExecutorService executorService;

	@Test
	public void saveMessages() {
		TopicConfig topicConfig = new TopicConfig();
		topicConfig.setName("test");

		List<Pair<Long, String>> messages = new ArrayList<>();
		messages.add(Pair.of(100L, "test message"));

		//when(config.getHdfsBufferSize()).thenReturn(1000);
		//hdfsService.saveMessages(topicConfig, messages);
	}

	@Test(expected = NullPointerException.class)
	public void cleanUp() {
		hdfsService.flush();
		hdfsService.flushStall();
		hdfsService.cleanUp();
	}
}