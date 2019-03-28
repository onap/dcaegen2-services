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

import org.json.JSONObject;
import org.junit.Test; 
 
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
	public void testIs() {
		Topic defaultTopic=new Topic("default");
		Topic testTopic = new Topic("test");
		testTopic.setDefaultTopic(defaultTopic);
		
		defaultTopic.setSupportElasticsearch(true);		
		boolean b = testTopic.isSupportElasticsearch();
		assertTrue(b);
		
		defaultTopic.setSupportElasticsearch(false);		
		b = testTopic.isSupportElasticsearch();
		assertFalse(b);
	}
}
