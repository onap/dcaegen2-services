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

package org.onap.datalake.feeder.service.db;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.domain.TopicName;
import org.onap.datalake.feeder.service.DbService;
import org.onap.datalake.feeder.service.db.ElasticsearchService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchServiceTest {

    static String DEFAULT_TOPIC_NAME = "_DL_DEFAULT_";

    @InjectMocks
    private ElasticsearchService elasticsearchService;

    @Mock
    private ApplicationConfiguration config;

    @Mock
    private RestHighLevelClient client;

    @Mock
    ActionListener<BulkResponse> listener;

    @Mock
    private DbService dbService;

    @Test(expected = NullPointerException.class)
    public void testCleanUp() throws IOException {

        elasticsearchService.cleanUp();

    }

    @Test(expected = NullPointerException.class)
    public void testEnsureTableExist() throws IOException {

        elasticsearchService.ensureTableExist(DEFAULT_TOPIC_NAME);
    }

    @Test
    public void testSaveJsons() {

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
//        when(config.getElasticsearchType()).thenReturn("doc");
  //      when(config.isAsync()).thenReturn(true);

        //elasticsearchService.saveJsons(topic.getTopicConfig(), jsons);

    }
}