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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.repository.TopicRepository;
import org.onap.datalake.feeder.service.DbService;
import org.onap.datalake.feeder.service.DmaapService;
import org.onap.datalake.feeder.service.TopicService;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
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

    @InjectMocks
    private TopicService topicService1;

    @Mock
    private ApplicationConfiguration config;

    @Mock
    private DbService dbService1;

    @Mock
    private DmaapService dmaapService1;

    @Before
    public void setupTest() {
        MockitoAnnotations.initMocks(this);
        // While the default boolean return value for a mock is 'false',
        // it's good to be explicit anyway:
        when(mockBindingResult.hasErrors()).thenReturn(false);
    }

    public void setAccessPrivateFields(TopicController topicController) throws NoSuchFieldException,
            IllegalAccessException {
        Field topicService = topicController.getClass().getDeclaredField("topicService");
        topicService.setAccessible(true);
        topicService.set(topicController, topicService1);
        Field topicRepository1 = topicController.getClass().getDeclaredField("topicRepository");
        topicRepository1.setAccessible(true);
        topicRepository1.set(topicController, topicRepository);
        Field dbService = topicController.getClass().getDeclaredField("dbService");
        dbService.setAccessible(true);
        dbService.set(topicController, dbService1);
    }

    @Test
    public void testCreateTopic() throws IOException, NoSuchFieldException, IllegalAccessException {
        TopicController topicController = new TopicController();
        setAccessPrivateFields(topicController);
        when(topicRepository.findById(DEFAULT_TOPIC_NAME)).thenReturn(Optional.of(new Topic(DEFAULT_TOPIC_NAME)));
        when(config.getDefaultTopicName()).thenReturn(DEFAULT_TOPIC_NAME);
        Topic topicName = topicController.createTopic(new Topic("a"), mockBindingResult, httpServletResponse);
        assertEquals(new Topic("a"), topicName);
        when(mockBindingResult.hasErrors()).thenReturn(true);
        topicName = topicController.createTopic(new Topic("a"), mockBindingResult, httpServletResponse);
        assertEquals(null, topicName);
        when(mockBindingResult.hasErrors()).thenReturn(false);
        Topic a = new Topic("a");
        a.setName("a");
        when(topicRepository.findById("a")).thenReturn(Optional.of(a));
        topicName = topicController.createTopic(new Topic("a"), mockBindingResult, httpServletResponse);
        assertEquals(null, topicName);
    }

    @Test
    public void testUpdateTopic() throws IOException, NoSuchFieldException, IllegalAccessException {
        TopicController topicController = new TopicController();
        setAccessPrivateFields(topicController);
        Topic topicName = topicController.updateTopic(new Topic("a"), mockBindingResult, httpServletResponse);
        assertEquals(null, topicName);
        Topic a = new Topic("a");
        a.setName("a");
        when(topicRepository.findById("a")).thenReturn(Optional.of(a));
        topicName = topicController.updateTopic(new Topic("a"), mockBindingResult, httpServletResponse);
        assertEquals(new Topic("a"), topicName);
        when(mockBindingResult.hasErrors()).thenReturn(true);
        topicName = topicController.updateTopic(new Topic("a"), mockBindingResult, httpServletResponse);
        assertEquals(null, topicName);

        ArrayList<Topic> topics = new ArrayList<>();
        topics.add(a);
        when(topicRepository.findAll()).thenReturn(topics);
        Iterable<Topic> list = topicController.list();
        for (Topic newTopic : list) {
            assertEquals(a, newTopic);
        }
    }

    @Test
    public void testAddDb() throws NoSuchFieldException, IllegalAccessException, IOException {
        TopicController topicController = new TopicController();
        setAccessPrivateFields(topicController);
        String dbName = "Elecsticsearch";
        String name = "a";
        Topic topic = new Topic(name);
        topic.setEnabled(true);
        Set<Db> dbSet = new HashSet<>();
        dbSet.add(new Db(dbName));
        topic.setDbs(dbSet);

        when(topicRepository.findById(name)).thenReturn(Optional.of(topic));
        topicController.addDb("a", dbName, httpServletResponse);
        topicController.deleteDb("a", dbName, httpServletResponse);
    }

    @Test
    public void testGetTopicDbs() throws NoSuchFieldException, IllegalAccessException, IOException {
        TopicController topicController = new TopicController();
        setAccessPrivateFields(topicController);
        String dbName = "Elecsticsearch";
        String name = "a";
        Topic topic = new Topic(name);
        topic.setEnabled(true);
        Set<Db> dbSet = new HashSet<>();
        dbSet.add(new Db(dbName));
        topic.setDbs(dbSet);

        when(topicRepository.findById(name)).thenReturn(Optional.of(topic));
        topicController.getTopicDbs("a");
    }

    @Test
    public void testListDmaapTopics() throws NoSuchFieldException, IllegalAccessException, IOException {
        TopicController topicController = new TopicController();
        Field dmaapService = topicController.getClass().getDeclaredField("dmaapService");
        dmaapService.setAccessible(true);
        dmaapService.set(topicController, dmaapService1);
        ArrayList<String> topics = new ArrayList<>();
        topics.add("a");
        when(dmaapService1.getTopics()).thenReturn(topics);
        List<String> strings = topicController.listDmaapTopics();
        for (String topic : strings) {
            assertEquals("a", topic);
        }
    }
}
