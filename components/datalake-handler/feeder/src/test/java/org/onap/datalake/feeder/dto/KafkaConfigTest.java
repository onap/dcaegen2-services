/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
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
package org.onap.datalake.feeder.dto;

import org.junit.Test;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.util.TestUtil;

import static org.junit.Assert.*;

/**
 * Test Kafka
 *
 * @author guochunmeng
 */
public class KafkaConfigTest {

    private static String ZOO_KEEPER = "test-zookeeper:2181";
    private static String BROKER_KAFKA = "test-kafka:9092";

    @Test
    public void testKafkaConfig(){
        Kafka testKafka = new Kafka();

        KafkaConfig testKafkaConfig = testKafka.getKafkaConfig();

        testKafkaConfig.setZooKeeper(ZOO_KEEPER);
        testKafkaConfig.setTimeout(1000);
        testKafkaConfig.setSecurityProtocol("");
        testKafkaConfig.setSecure(true);
        testKafkaConfig.setPass("pass");
        testKafkaConfig.setLogin("testLogin");
        testKafkaConfig.setName("test");
        testKafkaConfig.setIncludedTopic("");
        testKafkaConfig.setExcludedTopic("__consumer_offsets");
        testKafkaConfig.setGroup("testGroup");
        testKafkaConfig.setEnabled(true);
        testKafkaConfig.setConsumerCount(3);
        testKafkaConfig.setBrokerList(BROKER_KAFKA);
        testKafkaConfig.setId(1);

        KafkaConfig testKafkaConfig2 = TestUtil.newKafka("test").getKafkaConfig();
        assertNotEquals(testKafkaConfig, testKafkaConfig2);
        assertNotEquals(testKafkaConfig, null);
        assertNotEquals(testKafkaConfig.hashCode(), testKafkaConfig2.hashCode());
        assertEquals(BROKER_KAFKA, testKafkaConfig.getBrokerList());
        assertNotEquals("", testKafkaConfig.getExcludedTopic());
        assertEquals(true, testKafkaConfig.isSecure());
        assertEquals("testLogin", testKafkaConfig.getLogin());
        assertEquals("test", testKafkaConfig.getName());
        assertNotEquals("test", testKafkaConfig.getIncludedTopic());
        assertEquals("testGroup", testKafkaConfig.getGroup());
        assertEquals(true, testKafkaConfig.isEnabled());
        assertNotEquals("", testKafkaConfig.getConsumerCount());
        assertEquals(1, testKafkaConfig.getId());
        assertNotEquals("", testKafkaConfig.getPass());
        assertNotEquals("test", testKafkaConfig.getSecurityProtocol());
        assertEquals(ZOO_KEEPER, testKafkaConfig.getZooKeeper());
        assertNotEquals(null, testKafkaConfig.getTimeout());
    }

}