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
import org.onap.datalake.feeder.controller.domain.PostReturnBody;
import org.onap.datalake.feeder.controller.domain.TopicConfig;
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

    @Mock

    private TopicService topicServiceMock;

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
    public void testListTopic() throws IOException, NoSuchFieldException, IllegalAccessException{
        TopicController topicController = new TopicController();
        setAccessPrivateFields(topicController);
    }

    @Test
    public void testCreateTopic() throws IOException, NoSuchFieldException, IllegalAccessException {
        TopicController topicController = new TopicController();
        setAccessPrivateFields(topicController);
        //when(topicRepository.findById("ab")).thenReturn(Optional.of(new Topic("ab")));
       // when(config.getDefaultTopicName()).thenReturn(DEFAULT_TOPIC_NAME);
        PostReturnBody<TopicConfig> postTopic = topicController.createTopic(new TopicConfig(), mockBindingResult, httpServletResponse);
        assertEquals(postTopic.getStatusCode(), 200);
        when(mockBindingResult.hasErrors()).thenReturn(true);
        PostReturnBody<TopicConfig> topicConfig= topicController.createTopic(new TopicConfig(), mockBindingResult, httpServletResponse);
        assertEquals(null, topicConfig);
        when(mockBindingResult.hasErrors()).thenReturn(false);
        TopicConfig a = new TopicConfig();
        a.setName(DEFAULT_TOPIC_NAME);
        when(topicRepository.findById(DEFAULT_TOPIC_NAME)).thenReturn(Optional.of(new Topic(DEFAULT_TOPIC_NAME)));
        PostReturnBody<TopicConfig> postTopic2= topicController.createTopic(a, mockBindingResult, httpServletResponse);
        assertEquals(null, postTopic2);
    }

    @Test
    public void testUpdateTopic() throws IOException, NoSuchFieldException, IllegalAccessException {
        TopicController topicController = new TopicController();
        setAccessPrivateFields(topicController);
        PostReturnBody<TopicConfig> postTopic = topicController.updateTopic("a", new TopicConfig(), mockBindingResult, httpServletResponse);
        assertEquals(null, postTopic);
        Topic a = new Topic("a");
        a.setName("a");
        when(topicRepository.findById("a")).thenReturn(Optional.of(a));
        TopicConfig ac = new TopicConfig();
        ac.setName("a");
        ac.setEnable(true);
        PostReturnBody<TopicConfig> postConfig1 = topicController.updateTopic("a", ac, mockBindingResult, httpServletResponse);
        assertEquals(200, postConfig1.getStatusCode());
        TopicConfig ret = postConfig1.getReturnBody();
        assertEquals("a", ret.getName());
        assertEquals(true, ret.isEnable());
        when(mockBindingResult.hasErrors()).thenReturn(true);
        PostReturnBody<TopicConfig> postConfig2 = topicController.updateTopic("a", ac, mockBindingResult, httpServletResponse);
        assertEquals(null, postConfig2);

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
