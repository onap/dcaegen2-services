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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.json.JSONObject;
import org.junit.Test;
import org.onap.datalake.feeder.enumeration.DataFormat; 
 
/**
 * Test Topic
 * 
 * @author Guobiao Mo
 *
 */
 
public class TopicTest {	

    @Test
    public void getMessageId() {
    	String text = "{ data: { data2 : { value : 'hello'}}}";
    	
    	JSONObject json = new JSONObject(text);
    	
    	Topic topic = new Topic("test getMessageId");
    	topic.setMessageIdPath("/data/data2/value");
    	
    	String value = topic.getMessageId(json);

        assertEquals(value, "hello");    	
    }

    @Test
    public void getMessageIdFromMultipleAttributes() {
    	String text = "{ data: { data2 : { value : 'hello'}, data3 : 'world'}}";
    	
    	JSONObject json = new JSONObject(text);
    	
    	Topic topic = new Topic("test getMessageId");
    	topic.setMessageIdPath("/data/data2/value,/data/data3");
    	
    	String value = topic.getMessageId(json);

        assertEquals(value, "hello^world");    	
    }

    @Test
	public void testIs() {
		Topic defaultTopic=new Topic("_DL_DEFAULT_");
		Topic testTopic = new Topic("test");
		testTopic.setDefaultTopic(defaultTopic);

		assertTrue(defaultTopic.isDefault());
		assertFalse(testTopic.isDefault());		

		assertTrue(testTopic.equals(new Topic("test")));
		assertEquals(testTopic.hashCode(), (new Topic("test")).hashCode());
		
		defaultTopic.setDbs(new HashSet<>());
		defaultTopic.getDbs().add(new Db("Elasticsearch"));		
		assertTrue(testTopic.supportElasticsearch());
		assertFalse(testTopic.supportCouchbase());
		assertFalse(testTopic.supportDruid());
		assertFalse(testTopic.supportMongoDB());		
		
		defaultTopic.getDbs().remove(new Db("Elasticsearch"));	
		assertFalse(testTopic.supportElasticsearch());
		
		defaultTopic.setCorrelateClearedMessage(true);
		defaultTopic.setDataFormat("XML");
		defaultTopic.setEnabled(true);
		defaultTopic.setSaveRaw(true);		
		assertTrue(testTopic.isCorrelateClearedMessage());
		assertTrue(testTopic.isEnabled());
		assertTrue(testTopic.isSaveRaw()); 
		
		assertEquals(defaultTopic.getDataFormat(), DataFormat.XML);
	}
}
