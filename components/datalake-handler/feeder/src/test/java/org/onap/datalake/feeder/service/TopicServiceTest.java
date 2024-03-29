/*
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE
 * ================================================================================
 * Copyright 2019 China Mobile
 * Copyright (C) 2022 Wipro Limited.
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.DbType;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.domain.TopicName;
import org.onap.datalake.feeder.dto.TopicConfig;
import org.onap.datalake.feeder.repository.DbRepository;
import org.onap.datalake.feeder.repository.KafkaRepository;
import org.onap.datalake.feeder.repository.TopicNameRepository;
import org.onap.datalake.feeder.repository.TopicRepository;
import org.onap.datalake.feeder.service.db.ElasticsearchService;

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

    @Mock
    private DbService dbService;

    @Mock
    private DbRepository dbRepository;

    @Mock
    private TopicNameRepository topicNameRepository;

    @Mock
    private KafkaRepository kafkaRepository;

    @InjectMocks
    private TopicService topicService;

    @Test(expected = NullPointerException.class)
    public void testGetTopicException() throws IOException {
        List < Topic > topics = new ArrayList < > ();
        Topic topic = new Topic();
        DbType dbType = new DbType();
        Set < Kafka > kafkas = new HashSet < > ();
        Set < Db > dbs = new HashSet < > ();
        Db db = new Db();
        db.setName("Elasticsearch");
        dbs.add(db);

        dbType.setId("ES");
        db.setDbType(dbType);

        Kafka kafka = new Kafka();
        kafka.setName("1234");
        kafkas.add(kafka);

        TopicName topicName = new TopicName();
        topicName.setId("1234");

        topic.setTopicName(topicName);
        topic.setKafkas(kafkas);
        topic.setEnabled(true);
        topic.setDbs(dbs);
        topics.add(topic);
        when(topicRepository.findAll()).thenReturn(topics);
        when((ElasticsearchService) dbService.findDbStoreService(db)).thenReturn(new ElasticsearchService(db));
        topicService.findTopics(kafka, topicName.getId());
        topicService.getEnabledEffectiveTopic(kafka, topicName.getId(), true);

    }

    @Test
    public void testGetTopic() throws IOException {
        ArrayList < Topic > topics = new ArrayList < > ();
        Topic topic = new Topic();
        Set < Kafka > kafkas = new HashSet < > ();
        Kafka kafka = new Kafka();
        kafka.setName("1234");
        kafkas.add(kafka);
        TopicName topicName = new TopicName();
        topicName.setId(DEFAULT_TOPIC_NAME);
        topic.setTopicName(topicName);
        topic.setKafkas(kafkas);
        topics.add(topic);
        when(topicRepository.findAll()).thenReturn(topics);
        when(config.getDefaultTopicName()).thenReturn("_DL_DEFAULT_");
        topicService.getEnabledEffectiveTopic(new Kafka(), "test", true);
    }

    @Test
    public void testFindTopics() {
        ArrayList < Topic > topics = new ArrayList < > ();
        Topic topic = new Topic();
        Set < Kafka > kafkas = new HashSet < > ();
        Kafka kafka = new Kafka();
        kafka.setName("1234");
        kafkas.add(kafka);
        TopicName topicName = new TopicName();
        topicName.setId(DEFAULT_TOPIC_NAME);
        topic.setTopicName(topicName);
        topic.setKafkas(kafkas);
        topics.add(topic);
        when(topicRepository.findAll()).thenReturn(topics);
        topicService.findTopics(kafka, topicName.getId());
    }

    @Test
    public void testGetTopicNull() {
        Topic topic = new Topic();
        TopicName topicName = new TopicName();
        topicName.setId("_DL_DEFAULT_");
        topic.setId(1234);
        topic.setTopicName(topicName);
        Optional < Topic > optional = Optional.of(topic);
        when(topicRepository.findById(0)).thenReturn(optional);
        when(config.getDefaultTopicName()).thenReturn("_DL_DEFAULT_");
        assertEquals(topic, topicService.getTopic(0));
        assertTrue(topicService.isDefaultTopic(topic));
    }

    @Test
    public void testFillTopic() {
        TopicConfig tConfig = new TopicConfig();
        tConfig.setId(1234);
        tConfig.setName("1234");
        tConfig.setLogin("1234");
        tConfig.setPassword("1234");
        tConfig.setEnabled(true);
        tConfig.setSaveRaw(true);
        tConfig.setDataFormat("1234");
        tConfig.setTtl(1234);
        tConfig.setCorrelateClearedMessage(true);
        tConfig.setMessageIdPath("1234");
        tConfig.setAggregateArrayPath("1234");
        tConfig.setFlattenArrayPath("1234");
        List < Integer > sinkdbs = new ArrayList < > ();
        sinkdbs.add(1234);
        tConfig.setSinkdbs(sinkdbs);
        List < Integer > kafkas = new ArrayList < > ();
        kafkas.add(1);
        tConfig.setKafkas(kafkas);

        Db db = new Db();
        db.setId(1234);

        TopicName topicName = new TopicName();
        topicName.setId("1234");

        Optional < TopicName > optional = Optional.of(topicName);
        when(dbRepository.findById(1234)).thenReturn(Optional.of(db));
        when(kafkaRepository.findById(1)).thenReturn(Optional.of(new Kafka()));
        when(topicNameRepository.findById(tConfig.getName())).thenReturn(optional);
        topicService.fillTopicConfiguration(tConfig);
    }

    @Test
    public void testGetDefaultTopicFromFeeder() {
        when(topicRepository.findByTopicName_Id(config.getDefaultTopicName())).thenReturn(new Topic());
        topicService.getDefaultTopicFromFeeder();
    }

    @Test
    public void testGetDefaultTopic() {
        List < Topic > topics = new ArrayList < > ();
        Topic topic = new Topic();
        DbType dbType = new DbType();
        Set < Kafka > kafkas = new HashSet < > ();
        Set < Db > dbs = new HashSet < > ();
        Db db = new Db();
        db.setName("Elasticsearch");
        dbs.add(db);

        dbType.setId("ES");
        db.setDbType(dbType);

        Kafka kafka = new Kafka();
        kafka.setName("1234");
        kafkas.add(kafka);

        TopicName topicName = new TopicName();
        topicName.setId(DEFAULT_TOPIC_NAME);
        topic.setTopicName(topicName);
        topic.setKafkas(kafkas);
        topic.setEnabled(true);
        topic.setDbs(dbs);
        topics.add(topic);
        when(config.getDefaultTopicName()).thenReturn(DEFAULT_TOPIC_NAME);
        when(topicRepository.findAll()).thenReturn(topics);
        topicService.getDefaultTopic(kafka);
    }

    @Test
    public void testIsDefaultTopic() {
        assertEquals(false, topicService.isDefaultTopic(null));
    }

    /*
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
    */
}
