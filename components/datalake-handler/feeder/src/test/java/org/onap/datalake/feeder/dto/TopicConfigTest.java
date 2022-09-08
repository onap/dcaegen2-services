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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashSet;

import org.junit.Test;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.util.TestUtil;

/**
 * Test Topic
 *
 * @author Guobiao Mo
 */

public class TopicConfigTest {
	@Test
	public void testIs() {
		Topic testTopic = TestUtil.newTopic("test");

		TopicConfig testTopicConfig = testTopic.getTopicConfig();
		testTopicConfig.setSinkdbs(null);
		testTopicConfig.setEnabledSinkdbs(null);

		testTopic.setDbs(null);
		testTopic.setKafkas(null);
		testTopicConfig = testTopic.getTopicConfig();

		testTopic.setDbs(new HashSet<>());
		Db esDb = TestUtil.newDb("Elasticsearch");
		esDb.setEnabled(true);
		testTopic.getDbs().add(esDb);
		
		esDb = TestUtil.newDb("MongoDB");
		esDb.setEnabled(false);
		testTopic.getDbs().add(esDb);


		testTopic.setKafkas(new HashSet<>());
		Kafka kafka = TestUtil.newKafka("k1");
		kafka.setEnabled(true);
		testTopic.getKafkas().add(kafka);
		testTopicConfig = testTopic.getTopicConfig();
		
		
		
		TopicConfig testTopicConfig2 = TestUtil.newTopic("test").getTopicConfig();
		assertNotEquals(testTopicConfig, testTopicConfig2);
		assertEquals(testTopicConfig, testTopicConfig);
		assertNotEquals(testTopicConfig.hashCode(), testTopicConfig2.hashCode());
		assertNotEquals(testTopicConfig, testTopic);
		assertNotEquals(testTopicConfig, null);
	}
}
