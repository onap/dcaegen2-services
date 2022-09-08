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

import org.json.JSONObject;
import org.junit.Test;
import org.onap.datalake.feeder.dto.TopicConfig;
import org.onap.datalake.feeder.enumeration.DataFormat;
import org.onap.datalake.feeder.util.TestUtil;

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

public class TopicTest {


	@Test
	public void getMessageId() {
		String text = "{ data: { data2 : { value : 'hello'}}}";

		JSONObject json = new JSONObject(text);

		Topic topic = TestUtil.newTopic("test getMessageId");
		topic.setMessageIdPath("/data/data2/value");
	}

	@Test
	public void getMessageIdFromMultipleAttributes() {
		String text = "{ data: { data2 : { value : 'hello'}, data3 : 'world'}}";

		JSONObject json = new JSONObject(text);

		Topic topic = TestUtil.newTopic("test getMessageId");
		topic.setMessageIdPath("/data/data2/value,/data/data3");

		assertEquals("hello^world", topic.getMessageId(json));
		
		topic.setMessageIdPath("");
		assertNull(topic.getMessageId(json));
	}
/*
	@Test
	public void testArrayPath() {
		Topic topic = TestUtil.newTopic("testArrayPath");
		topic.setAggregateArrayPath("/data/data2/value,/data/data3");
		topic.setFlattenArrayPath("/data/data2/value,/data/data3");

		TopicConfig topicConfig = topic.getTopicConfig();
	}
 
    @Test
    public void getMessageIdFromMultipleAttributes() {
        Topic topic = TestUtil.newTopic("test getMessageId"); 
        Topic defaultTopic = TestUtil.newTopic("_DL_DEFAULT_");
        Topic testTopic = TestUtil.newTopic("test");

        assertEquals(3650, testTopic.getTtl());
        defaultTopic.setTtl(20);
        assertEquals(20, defaultTopic.getTtl());
        topic.setLogin("root");
        topic.setPass("root123");
        topic.setEnabled(true);
        topic.setSaveRaw(true);
        topic.setCorrelateClearedMessage(true);
        topic.setMessageIdPath("/data/data2/value");
        assertTrue("root".equals(topic.getLogin()));
        assertTrue("root123".equals(topic.getPass()));
        assertFalse("true".equals(topic.isEnabled()));
        assertFalse("true".equals(topic.isSaveRaw()));
        assertFalse("true".equals(topic.isCorrelateClearedMessage()));
        assertTrue("/data/data2/value".equals(topic.getMessageIdPath()));
        assertFalse(topic.equals(null));
        assertFalse(topic.equals(new Db()));
    }
*/
    @Test
    public void testAggregate() {
        Topic defaultTopic = TestUtil.newTopic("_DL_DEFAULT_");
        Topic testTopic = TestUtil.newTopic("test");
        testTopic.setId(1);
        Topic testTopic2 = TestUtil.newTopic("test2");
        testTopic2.setId(2);
    	
        //test null cases
        testTopic.getAggregateArrayPath2() ;
        testTopic.getFlattenArrayPath2() ;

        //test not null cases
        testTopic.setAggregateArrayPath("/data/data2/value,/data/data3");
        testTopic.setFlattenArrayPath("/data/data2/value,/data/data3");

        testTopic.getAggregateArrayPath2() ;
        testTopic.getFlattenArrayPath2() ;
        
    }
    
    
    @Test
    public void testIs() {
        Topic defaultTopic = TestUtil.newTopic("_DL_DEFAULT_");
        Topic testTopic = TestUtil.newTopic("test");
        testTopic.setId(1);
        Topic testTopic2 = TestUtil.newTopic("test2");
        testTopic2.setId(1);

        assertEquals(testTopic, testTopic2);
        assertNotEquals(testTopic, null);
        assertNotEquals(testTopic, "test");
        assertEquals(testTopic.hashCode(), testTopic2.hashCode());
        assertNotEquals(testTopic.toString(), "test");

        defaultTopic.setDbs(new HashSet<>());
        defaultTopic.getDbs().add(TestUtil.newDb("Elasticsearch"));

        assertEquals(defaultTopic.getDataFormat(), null);
        defaultTopic.setCorrelateClearedMessage(true);
        defaultTopic.setDataFormat("XML");
        defaultTopic.setEnabled(true);
        defaultTopic.setSaveRaw(true);
        assertTrue(defaultTopic.isCorrelateClearedMessage());
        assertTrue(defaultTopic.isEnabled());
        assertTrue(defaultTopic.isSaveRaw());

        assertEquals(defaultTopic.getDataFormat2(), DataFormat.XML);
        defaultTopic.setDataFormat(null);
        assertNull(defaultTopic.getDataFormat2());

        defaultTopic.setDataFormat(null);
        assertEquals(testTopic.getDataFormat(), null);

        Topic testTopic1 = TestUtil.newTopic("test");
        assertFalse(testTopic1.isCorrelateClearedMessage());
        

        testTopic.setPass("root123");
        assertTrue("root123".equals(testTopic.getPass()));
        
        assertEquals(3650, testTopic.getTtl());
        defaultTopic.setTtl(20);
        assertEquals(20, defaultTopic.getTtl());
    }
}
