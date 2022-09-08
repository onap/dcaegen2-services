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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.EffectiveTopic;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.domain.TopicName;
import org.onap.datalake.feeder.service.db.CouchbaseService;
import org.onap.datalake.feeder.service.db.ElasticsearchService;
import org.onap.datalake.feeder.service.db.HdfsService;
import org.onap.datalake.feeder.service.db.MongodbService;
import org.onap.datalake.feeder.util.TestUtil;
import org.springframework.context.ApplicationContext;

/**
 * Test StoreService
 * 
 * @author Guobiao Mo
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class StoreServiceTest {

	@InjectMocks
	private StoreService storeService = new StoreService();

	@Mock
	private ApplicationContext context;

	@Mock
	private ApplicationConfiguration config;

	@Mock
	private TopicConfigPollingService configPollingService;

	@Mock
	private MongodbService mongodbService;

	@Mock
	private CouchbaseService couchbaseService;

	@Mock
	private ElasticsearchService elasticsearchService;

	@Mock
	private HdfsService hdfsService;

	@Mock
	private Kafka kafka;

	@Before
	public void init() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
		Method init = storeService.getClass().getDeclaredMethod("init");
		init.setAccessible(true);
		init.invoke(storeService);
	}

	private EffectiveTopic createTopicConfig(String topicStr, String type) {
		Topic topic = new Topic();
		topic.setTopicName(new TopicName("unauthenticated.SEC_FAULT_OUTPUT"));
		topic.setDataFormat(type);
		topic.setSaveRaw(true);
		topic.setEnabled(true);
		

		EffectiveTopic effectiveTopic = new EffectiveTopic(topic, "test");
		List<EffectiveTopic> effectiveTopics = new ArrayList<>();
		effectiveTopics.add(effectiveTopic);

		when(configPollingService.getEffectiveTopic(kafka, topicStr)).thenReturn(effectiveTopics);

		return effectiveTopic;
	}

	@Test
	public void saveMessages() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
		EffectiveTopic effectiveTopic = createTopicConfig("test1", "JSON");
		effectiveTopic.getTopic().setAggregateArrayPath("/test");
		effectiveTopic.getTopic().setFlattenArrayPath("/test"); 

		effectiveTopic = createTopicConfig("test2", "XML");
		effectiveTopic.getTopic().setSaveRaw(false); 

		effectiveTopic = createTopicConfig("test3", "YAML");
		effectiveTopic.getTopic().setDbs(new HashSet<>());
		effectiveTopic.getTopic().getDbs().add(TestUtil.newDb("ES"));
		effectiveTopic.getTopic().getDbs().add(TestUtil.newDb("CB"));
		effectiveTopic.getTopic().getDbs().add(TestUtil.newDb("DRUID"));
		effectiveTopic.getTopic().getDbs().add(TestUtil.newDb("MONGO"));
		effectiveTopic.getTopic().getDbs().add(TestUtil.newDb("HDFS")); 
		//		effectiveTopic.getTopic().setEnabledSinkdbs(new ArrayList<>());
		//	effectiveTopic.getTopic().getEnabledSinkdbs().add("Elasticsearch");
		//assertTrue(topicConfig.supportElasticsearch());

		createTopicConfig("test4", "TEXT");
		
		effectiveTopic = createTopicConfig("test5", "TEXT");
		effectiveTopic.getTopic().setEnabled(false);

		when(config.getTimestampLabel()).thenReturn("ts");
		when(config.getRawDataLabel()).thenReturn("raw");

		//JSON
		List<Pair<Long, String>> messages = new ArrayList<>();
		messages.add(Pair.of(100L, "{test: 1}"));

		storeService.saveMessages(kafka, "test1", messages);

		//XML
		List<Pair<Long, String>> messagesXml = new ArrayList<>();
		messagesXml.add(Pair.of(100L, "<test></test>"));
		messagesXml.add(Pair.of(100L, "<test></test"));//bad xml to trigger exception

		storeService.saveMessages(kafka, "test2", messagesXml);

		//YAML
		List<Pair<Long, String>> messagesYaml = new ArrayList<>();
		messagesYaml.add(Pair.of(100L, "test: yes"));

		storeService.saveMessages(kafka, "test3", messagesYaml);

		//TEXT
		List<Pair<Long, String>> messagesText = new ArrayList<>();
		messagesText.add(Pair.of(100L, "test message"));

		storeService.saveMessages(kafka, "test4", messagesText);
		
		storeService.saveMessages(kafka, "test5", messagesText);

		//Null mesg
		storeService.saveMessages(kafka, "test", null);
	}

	@Test
	public void testFlush() {
		storeService.flush();
		storeService.flushStall();
	}
}