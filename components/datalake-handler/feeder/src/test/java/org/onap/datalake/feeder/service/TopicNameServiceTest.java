/*
 * ============LICENSE_START=======================================================
 * ONAP : DCAE
 * ================================================================================
 * Copyright 2022 Wipro Limited.
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
package org.onap.datalake.feeder.service;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.domain.TopicName;
import org.onap.datalake.feeder.repository.TopicNameRepository;

@RunWith(MockitoJUnitRunner.class)
public class TopicNameServiceTest {

    @Mock
    private TopicNameRepository topicNameRepository;

    @InjectMocks
    private TopicNameService topicNameService;

    @Test
    public void testUpdate() {
        List < TopicName > topicNameList = new ArrayList < > ();
        topicNameList.add(new TopicName("test"));
        Collection < String > allTopicNames = new ArrayList < > ();
        allTopicNames.add("MONGODB");
        when(topicNameRepository.findAll()).thenReturn(topicNameList);
        topicNameService.update(allTopicNames);
    }

}
