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
package org.onap.datalake.feeder.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.onap.datalake.feeder.dto.KafkaConfig;
import org.onap.datalake.feeder.util.TestUtil;

/**
 * Test TopicName
 *
 * @author Guobiao Mo
 */

public class KafkaTest {


	@Test
	public void test() {
		Kafka kafka = TestUtil.newKafka("test");
		kafka.setName(null); 
		kafka.setTopics(null);
		kafka.getTopics();
		kafka.hashCode();

		KafkaConfig kc = kafka.getKafkaConfig(); 
		
		assertEquals(kafka, kafka);
		assertNotEquals(kafka, null);
		assertNotEquals(kafka, "test");

	}

}
