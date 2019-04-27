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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Topic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DmaapServiceTest {

    static String DMAPP_ZOOKEEPER_HOST_PORT = "message-router-zookeeper:2181";

    @InjectMocks
    private DmaapService dmaapService;

    @Mock
    private ApplicationConfiguration config;
    @Mock
    private TopicService topicService;

    @Test
    public void testGetTopics() {

        List<String> list = new ArrayList<>();
        list.add("unauthenticated.DCAE_CL_OUTPUT");
        list.add("AAI-EVENT");
        list.add("__consumer_offsets");
        list.add("unauthenticated.SEC_FAULT_OUTPUT");
        list.add("msgrtr.apinode.metrics.dmaap");
        when(config.getDmaapZookeeperHostPort()).thenReturn(DMAPP_ZOOKEEPER_HOST_PORT);
        assertNotEquals(list, dmaapService.getTopics());
    }

    /*@Test
    public void testGetActiveTopics() throws IOException {

        List<String> list = new ArrayList<>();
        list.add("unauthenticated.DCAE_CL_OUTPUT");
        list.add("AAI-EVENT");
        list.add("__consumer_offsets");
        list.add("unauthenticated.SEC_FAULT_OUTPUT");
        list.add("msgrtr.apinode.metrics.dmaap");

        when(config.getDmaapZookeeperHostPort()).thenReturn(DMAPP_ZOOKEEPER_HOST_PORT);
        try {
        	assertNotEquals(list, dmaapService.getActiveTopics());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}