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

import org.json.JSONObject;
import org.junit.Test;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Topic;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test Topic
 *
 * @author Guobiao Mo
 */

public class TopicConfigTest {

    @Test
    public void getMessageId() {
        String text = "{ data: { data2 : { value : 'hello'}}}";

        JSONObject json = new JSONObject(text);

        Topic topic = new Topic("test getMessageId");
        topic.setMessageIdPath("/data/data2/value");
        
        TopicConfig topicConfig = topic.getTopicConfig();

        String value = topicConfig.getMessageId(json);

        assertEquals(value, "hello");
    }

    @Test
    public void getMessageIdFromMultipleAttributes() {
        String text = "{ data: { data2 : { value : 'hello'}, data3 : 'world'}}";

        JSONObject json = new JSONObject(text);

        Topic topic = new Topic("test getMessageId");
        topic.setMessageIdPath("/data/data2/value,/data/data3");

        TopicConfig topicConfig = topic.getTopicConfig();
        
        String value = topicConfig.getMessageId(json);
        assertEquals(value, "hello^world");

        topic.setMessageIdPath("");
        topicConfig = topic.getTopicConfig();
        assertNull(topicConfig.getMessageId(json));

    }

    @Test
    public void testArrayPath() {
        Topic topic = new Topic("testArrayPath");
        topic.setAggregateArrayPath("/data/data2/value,/data/data3");
        topic.setFlattenArrayPath("/data/data2/value,/data/data3");

        TopicConfig topicConfig = topic.getTopicConfig();

        String[] value = topicConfig.getAggregateArrayPath2();
        assertEquals(value[0], "/data/data2/value");
        assertEquals(value[1], "/data/data3");

        value = topicConfig.getFlattenArrayPath2();
        assertEquals(value[0], "/data/data2/value");
        assertEquals(value[1], "/data/data3");
    }

    @Test
    public void testIs() {
        Topic testTopic = new Topic("test");

        TopicConfig testTopicConfig = testTopic.getTopicConfig();
        testTopicConfig.setSinkdbs(null);
        testTopicConfig.setEnabledSinkdbs(null);
        assertFalse(testTopicConfig.supportElasticsearch());
        assertNull(testTopicConfig.getDataFormat2());
                
        testTopic.setDbs(new HashSet<>());
        Db esDb = new Db("Elasticsearch");
        esDb.setEnabled(true);
        testTopic.getDbs().add(esDb);
        
        testTopicConfig = testTopic.getTopicConfig();

        //assertEquals(testTopicConfig, new Topic("test").getTopicConfig());
        assertNotEquals(testTopicConfig, testTopic);
        assertNotEquals(testTopicConfig, null);
        //assertEquals(testTopicConfig.hashCode(), (new Topic("test").getTopicConfig()).hashCode());
        
        assertTrue(testTopicConfig.supportElasticsearch());
        assertFalse(testTopicConfig.supportCouchbase());
        assertFalse(testTopicConfig.supportDruid());
        assertFalse(testTopicConfig.supportMongoDB());
        assertFalse(testTopicConfig.supportHdfs());

        testTopic.getDbs().remove(new Db("Elasticsearch"));
        testTopicConfig = testTopic.getTopicConfig();
        assertFalse(testTopicConfig.supportElasticsearch());
 
    }
}
