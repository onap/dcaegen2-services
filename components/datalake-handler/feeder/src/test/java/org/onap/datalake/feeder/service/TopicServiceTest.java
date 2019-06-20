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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.repository.TopicRepository;

/**
 * Test Service for Topic
 * 
 * @author Guobiao Mo
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TopicServiceTest {

	static String DEFAULT_TOPIC_NAME = "_DL_DEFAULT_";

	@Mock
	private ApplicationConfiguration config;
	
	@Mock
	private TopicRepository topicRepository;

	@Mock
	private ElasticsearchService elasticsearchService;

	@InjectMocks
	private TopicService topicService;

	@Test
	public void testGetTopic() {
		String name = "a";
		when(topicRepository.findById(name)).thenReturn(Optional.of(new Topic(name)));
		assertEquals(topicService.getTopic(name), new Topic(name));
		
		assertFalse(topicService.istDefaultTopic(new Topic(name)));
	}

	@Test
	public void testGetTopicNull() {
		String name = null;
		when(topicRepository.findById(name)).thenReturn(Optional.empty());
		assertNull(topicService.getTopic(name));
	}


	@Test
	public void testGetEffectiveTopic() throws IOException {
		String name = "a";
		Topic topic = new Topic(name);
		topic.setEnabled(true);
		Set<Db> dbSet = new HashSet<>();
		dbSet.add(new Db("Elasticsearch"));
		topic.setDbs(dbSet);

		when(config.getDefaultTopicName()).thenReturn(DEFAULT_TOPIC_NAME);
		when(topicRepository.findById(DEFAULT_TOPIC_NAME)).thenReturn(Optional.of(topic));
		when(topicRepository.findById(name)).thenReturn(Optional.of(topic));
		when(topicRepository.findById(null)).thenReturn(Optional.empty());

		assertEquals(topicService.getEffectiveTopic(name), topicService.getEffectiveTopic(name, false));

		assertNotNull(topicService.getEffectiveTopic(null));

		topicService.getEffectiveTopic(name, true);
	}
}
