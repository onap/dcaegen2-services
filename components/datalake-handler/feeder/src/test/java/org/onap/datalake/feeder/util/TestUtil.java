/*
 * ============LICENSE_START=======================================================
 * ONAP : DCAE
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

package org.onap.datalake.feeder.util;
 
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.DbType;
import org.onap.datalake.feeder.domain.EffectiveTopic;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.domain.TopicName;
import org.onap.datalake.feeder.service.db.DbStoreService;
 

/**
 * test utils
 *
 * @author Guobiao Mo
 */
public class TestUtil {

    static int i=0;

    public static Kafka newKafka(String name) {
    	Kafka kafka  = new Kafka(); 
    	kafka.setId(i++);
    	kafka.setName(name); 
    	return kafka ;
    }

    public static Db newDb(String name) {
    	Db db = new Db();
    	db.setId(i++);
    	db.setName(name);   
    	db.setDbType(new DbType(name, name));
    	return db;
    }

    public static  Topic newTopic(String name) {
    	Topic topic = new Topic();
    	topic.setId(i++);
    	topic.setTopicName(new TopicName(name));
    	
    	return topic;
    }

	public static void testSaveJsons(ApplicationConfiguration config, DbStoreService dbStoreService) {
		Topic topic = new Topic();
		topic.setTopicName(new TopicName("unauthenticated.SEC_FAULT_OUTPUT"));
		topic.setCorrelateClearedMessage(true);
		topic.setMessageIdPath("/event/commonEventHeader/eventName,/event/commonEventHeader/reportingEntityName,/event/faultFields/specificProblem");
		String jsonString = "{\"event\":{\"commonEventHeader\":{\"sourceId\":\"vnf_test_999\",\"startEpochMicrosec\":2222222222222,\"eventId\":\"ab305d54-85b4-a31b-7db2-fb6b9e546016\",\"sequence\":1,\"domain\":\"fautt\",\"lastEpochMicrosec\":1234567890987,\"eventName\":\"Fault_MultiCloud_VMFailure\",\"sourceName\":\"vSBC00\",\"priority\":\"Low\",\"version\":3,\"reportingEntityName\":\"vnf_test_2_rname\"},\"faultFields\":{\"eventSeverity\":\"CRITILLL\",\"alarmCondition\":\"Guest_Os_FaiLLL\",\"faultFieldsVersion\":3,\"specificProblem\":\"Fault_MultiCloud_VMFailure\",\"alarmInterfaceA\":\"aaaa\",\"alarmAdditionalInformation\":[{\"name\":\"objectType3\",\"value\":\"VIN\"},{\"name\":\"objectType4\",\"value\":\"VIN\"}],\"eventSourceType\":\"single\",\"vfStatus\":\"Active\"}}}";
		String jsonString2 = "{\"event\":{\"commonEventHeader\":{\"sourceId\":\"vnf_test_999\",\"startEpochMicrosec\":2222222222222,\"eventId\":\"ab305d54-85b4-a31b-7db2-fb6b9e546016\",\"sequence\":1,\"domain\":\"fautt\",\"lastEpochMicrosec\":1234567890987,\"eventName\":\"Fault_MultiCloud_VMFailureCleared\",\"sourceName\":\"vSBC00\",\"priority\":\"Low\",\"version\":3,\"reportingEntityName\":\"vnf_test_2_rname\"},\"faultFields\":{\"eventSeverity\":\"CRITILLL\",\"alarmCondition\":\"Guest_Os_FaiLLL\",\"faultFieldsVersion\":3,\"specificProblem\":\"Fault_MultiCloud_VMFailure\",\"alarmInterfaceA\":\"aaaa\",\"alarmAdditionalInformation\":[{\"name\":\"objectType3\",\"value\":\"VIN\"},{\"name\":\"objectType4\",\"value\":\"VIN\"}],\"eventSourceType\":\"single\",\"vfStatus\":\"Active\"}}}";

		JSONObject jsonObject = new JSONObject(jsonString);
		JSONObject jsonObject2 = new JSONObject(jsonString2);

		List<JSONObject> jsons = new ArrayList<>();
		jsons.add(jsonObject);
		jsons.add(jsonObject2);

		EffectiveTopic effectiveTopic = new EffectiveTopic(topic, "test");

		dbStoreService.saveJsons(effectiveTopic, jsons);

	}
}
