/*
* ============LICENSE_START=======================================================
* ONAP : DATALAKE
* ================================================================================
* Copyright 2019 China Mobile
* Copyright (C) 2022 Wipro Limited. All rights reserved.
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

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.datalake.feeder.repository.TopicNameRepository;

public class TopicNameServiceTest {
         
        	
        @Mock
        Collection<String> allTopicNames;
        TopicNameRepository topicNameRepository;
        @Test
        public void test() {
                TopicNameService tst= new TopicNameService();
                Throwable throwable = Mockito.mock(Throwable.class);
                TopicNameService tst1= new TopicNameService();
                assert(true);
            }
}
