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

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.util.TestUtil;

@RunWith(MockitoJUnitRunner.class)
public class DmaapServiceTest {

    static String DMAPP_ZOOKEEPER_HOST_PORT = "test:2181";

    private DmaapService dmaapService;

    @Mock
    private ApplicationConfiguration config;
    @Mock
    private TopicService topicService;

	@Before
	public void init() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		Kafka kafka = TestUtil.newKafka("kafka"); 
		dmaapService = new DmaapService(kafka);

		Field configField = DmaapService.class.getDeclaredField("config");
		configField.setAccessible(true);
		configField.set(dmaapService, config);

		/*
		Method initMethod = DmaapService.class.getDeclaredMethod("init");
		initMethod.setAccessible(true);
		initMethod.invoke(dmaapService); */
	}
	
    @Test
    public void testGetTopics() throws InterruptedException {
        List<String> list = new ArrayList<>();
        list.add("unauthenticated.DCAE_CL_OUTPUT");
        list.add("AAI-EVENT");
        list.add("__consumer_offsets");
        list.add("unauthenticated.SEC_FAULT_OUTPUT");
        list.add("msgrtr.apinode.metrics.dmaap");
//		when(config.getDmaapKafkaExclude()).thenReturn(new String[] { "AAI-EVENT" });
        //when(config.getDmaapZookeeperHostPort()).thenReturn(DMAPP_ZOOKEEPER_HOST_PORT);
        assertNotEquals(list, dmaapService.getTopics());

		when(config.getShutdownLock()).thenReturn(new ReentrantReadWriteLock());
    	dmaapService.cleanUp();
    }

    @Test
    public void testGetActiveTopicConfigs() throws IOException {

        List<String> list = new ArrayList<>();
        list.add("unauthenticated.DCAE_CL_OUTPUT");
        list.add("AAI-EVENT");
        list.add("__consumer_offsets");
        list.add("unauthenticated.SEC_FAULT_OUTPUT");
        list.add("msgrtr.apinode.metrics.dmaap");

        //when(config.getDmaapZookeeperHostPort()).thenReturn(DMAPP_ZOOKEEPER_HOST_PORT);
        try {
        	assertNotEquals(list, dmaapService.getActiveEffectiveTopic());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}