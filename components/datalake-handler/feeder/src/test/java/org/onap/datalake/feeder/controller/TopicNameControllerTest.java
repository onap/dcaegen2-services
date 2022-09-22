/*
 * ============LICENSE_START=======================================================
 * ONAP : DCAE
 * ================================================================================
 * Copyright (C) 2022 Wipro Limited.
 * =================================================================================
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
package org.onap.datalake.feeder.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.domain.TopicName;
import org.onap.datalake.feeder.repository.TopicNameRepository;

@RunWith(MockitoJUnitRunner.class)
public class TopicNameControllerTest {

    @Mock
    private TopicNameRepository topicNameRepository;

    @InjectMocks
    TopicNameController topicNameController;

    @Test
    public void testList() throws IOException {
        List < TopicName > topicNameList = new ArrayList < > ();

        TopicName topicName = new TopicName();
        topicName.setId("1");
        topicName.setDesigns(null);
        Topic topic = new Topic();
        topic.setId(1);
        Set < Topic > topics = new HashSet < > ();
        topics.add(topic);
        topicName.setTopics(topics);
        topicNameList.add(topicName);

        when(topicNameRepository.findAll()).thenReturn(topicNameList);
        List < String > retString = topicNameController.list();
        assertEquals("1", retString.get(0));

    }

}
