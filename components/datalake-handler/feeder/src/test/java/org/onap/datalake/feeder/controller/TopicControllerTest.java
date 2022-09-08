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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.controller.domain.PostReturnBody;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.dto.TopicConfig;
import org.onap.datalake.feeder.repository.TopicNameRepository;
import org.onap.datalake.feeder.repository.TopicRepository;
import org.onap.datalake.feeder.service.DbService;
import org.onap.datalake.feeder.service.DmaapService;
import org.onap.datalake.feeder.service.TopicService;
import org.onap.datalake.feeder.util.TestUtil;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TopicControllerTest {

	static String DEFAULT_TOPIC_NAME = "_DL_DEFAULT_";

	@Mock
	private HttpServletResponse httpServletResponse;

	@Mock
	private BindingResult mockBindingResult;

	@Mock
	private TopicRepository topicRepository;

	@Mock
	private TopicService topicService;

	@Mock
	private TopicNameRepository topicNameRepository;

	@InjectMocks
	TopicController topicController;

	@Mock
	private ApplicationConfiguration config;

	@Mock
	private DbService dbService;

	@Mock
	private DmaapService dmaapService;

	@Before
	public void setupTest() throws NoSuchFieldException, IllegalAccessException {
		// While the default boolean return value for a mock is 'false',
		// it's good to be explicit anyway:
		when(mockBindingResult.hasErrors()).thenReturn(false);
	}

	@Test
	public void testListTopic() throws IOException, NoSuchFieldException, IllegalAccessException {
	}

	@Test
	public void testCreateTopic() throws IOException {
		Topic a = TestUtil.newTopic("a");
		a.setId(1);
		a.setEnabled(true);

		TopicConfig ac = a.getTopicConfig();

		when(topicService.fillTopicConfiguration(ac)).thenReturn(a);
		PostReturnBody<TopicConfig> postTopic = topicController.createTopic(ac, mockBindingResult, httpServletResponse);
		assertEquals(postTopic.getStatusCode(), 200);

		when(mockBindingResult.hasErrors()).thenReturn(true);
		PostReturnBody<TopicConfig> topicConfig = topicController.createTopic(ac, mockBindingResult, httpServletResponse);
		assertEquals(null, topicConfig);
	}

	@Test
	public void testUpdateTopic() throws IOException {
		Topic a = TestUtil.newTopic("a");
		a.setId(1);
		a.setEnabled(true);

		TopicConfig ac = a.getTopicConfig();

		when(topicService.getTopic(1)).thenReturn(a);
		PostReturnBody<TopicConfig> postConfig1 = topicController.updateTopic(1, ac, mockBindingResult, httpServletResponse);
		assertEquals(200, postConfig1.getStatusCode());
		TopicConfig ret = postConfig1.getReturnBody();
		assertEquals("a", ret.getName());
		assertEquals(true, ret.isEnabled());

		topicController.updateTopic(0, ac, mockBindingResult, httpServletResponse);

		when(topicService.getTopic(1)).thenReturn(null);
		topicController.updateTopic(1, ac, mockBindingResult, httpServletResponse);

		when(mockBindingResult.hasErrors()).thenReturn(true);
		PostReturnBody<TopicConfig> postConfig2 = topicController.updateTopic(1, ac, mockBindingResult, httpServletResponse);
		assertNull(postConfig2);

	}

	@Test
	public void testGetTopic() throws IOException {
		Topic a = TestUtil.newTopic("a");
		a.setId(1);
		a.setEnabled(true);

		when(topicService.getTopic(1)).thenReturn(a);
		TopicConfig ac = topicController.getTopic(1, httpServletResponse);
		when(topicService.getTopic(1)).thenReturn(null);
		ac = topicController.getTopic(1, httpServletResponse);
	}

	@Test
	public void testDeleteTopic() throws IOException {
		Topic a = TestUtil.newTopic("a");
		a.setId(1);
		a.setEnabled(true);

		when(topicService.getTopic(1)).thenReturn(a);
		topicController.deleteTopic(1, httpServletResponse);
		when(topicService.getTopic(1)).thenReturn(null);
		topicController.deleteTopic(1, httpServletResponse);
	}

	@Test
	public void testList() {
		ArrayList<Topic> topics = new ArrayList<>();
		topics.add(TestUtil.newTopic("a"));
		topics.add(TestUtil.newTopic(DEFAULT_TOPIC_NAME));
		when(topicRepository.findAll()).thenReturn(topics);

		List<Integer> ids = topicController.list();
		for (Integer topic : ids) {
			System.out.println(topic);
		}
	}
}
