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
package org.onap.datalake.feeder.controller;


import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.datalake.feeder.repository.TopicNameRepository;

public class TopicNameControllerTest {
        @Mock
        private TopicNameRepository topicNameRepository;

        @Test
        public void test() {
                TopicNameController tst= new TopicNameController();
                Throwable throwable = Mockito.mock(Throwable.class);
                TopicNameController tst1= new TopicNameController();
                assert(true);
                
          }
          
}
