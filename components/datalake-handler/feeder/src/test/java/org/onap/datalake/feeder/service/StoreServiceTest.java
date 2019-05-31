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
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.dto.TopicConfig;
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

	public void testInit() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
		Method init = storeService.getClass().getDeclaredMethod("init");
		init.setAccessible(true);
		init.invoke(storeService);
	}

	private TopicConfig createTopicConfig(String topicStr, String type) {

		TopicConfig topicConfig = new TopicConfig();
		topicConfig.setName(topicStr);
		topicConfig.setDataFormat(type);
		topicConfig.setSaveRaw(true);

		when(configPollingService.getEffectiveTopicConfig(topicStr)).thenReturn(topicConfig);

		return topicConfig;
	}

	@Test
	public void saveMessages() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
		testInit();

		TopicConfig topicConfig = createTopicConfig("test1", "JSON");

		topicConfig = createTopicConfig("test2", "XML");
		topicConfig.setSaveRaw(false);

		topicConfig = createTopicConfig("test3", "YAML");

		topicConfig.setSinkdbs(new ArrayList<>());
		topicConfig.getSinkdbs().add("Elasticsearch");
		topicConfig.getSinkdbs().add("Couchbase");
		topicConfig.getSinkdbs().add("Druid");
		topicConfig.getSinkdbs().add("MongoDB");
		topicConfig.getSinkdbs().add("HDFS");

		createTopicConfig("test4", "TEXT");

		when(config.getTimestampLabel()).thenReturn("ts");
		when(config.getRawDataLabel()).thenReturn("raw");

		//JSON
		List<Pair<Long, String>> messages = new ArrayList<>();
		messages.add(Pair.of(100L, "{test: 1}"));

		storeService.saveMessages("test1", messages);

		//XML
		List<Pair<Long, String>> messagesXml = new ArrayList<>();
		messagesXml.add(Pair.of(100L, "<test></test>")); 
		messagesXml.add(Pair.of(100L, "<test></test"));//bad xml to trigger exception

		storeService.saveMessages("test2", messagesXml);

		//YAML
		List<Pair<Long, String>> messagesYaml = new ArrayList<>();
		messagesYaml.add(Pair.of(100L, "test: yes"));

		storeService.saveMessages("test3", messagesYaml);

		//TEXT
		List<Pair<Long, String>> messagesText = new ArrayList<>();
		messagesText.add(Pair.of(100L, "test message"));

		storeService.saveMessages("test4", messagesText);

		//Null mesg
		storeService.saveMessages("test", null);
	}

	@Test
	public void testFlush() {
		storeService.flush();
		storeService.flushStall();
	}
}