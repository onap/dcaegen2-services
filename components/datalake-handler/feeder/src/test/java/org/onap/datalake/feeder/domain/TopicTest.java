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

import org.junit.Test;
import org.onap.datalake.feeder.enumeration.DataFormat;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test Topic
 *
 * @author Guobiao Mo
 */

public class TopicTest {

    @Test
    public void getMessageIdFromMultipleAttributes() {
        Topic topic = new Topic("test getMessageId"); 
        Topic defaultTopic = new Topic("_DL_DEFAULT_");
        Topic testTopic = new Topic("test");

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
        assertFalse("true".equals(topic.getEnabled()));
        assertFalse("true".equals(topic.getSaveRaw()));
        assertFalse("true".equals(topic.getCorrelateClearedMessage()));
        assertTrue("/data/data2/value".equals(topic.getMessageIdPath()));
        assertFalse(topic.equals(null));
        assertFalse(topic.equals(new Db()));
    }

    @Test
    public void testIs() {
        Topic defaultTopic = new Topic("_DL_DEFAULT_");
        Topic testTopic = new Topic("test");

        assertTrue(testTopic.equals(new Topic("test")));
        assertEquals(testTopic.hashCode(), (new Topic("test")).hashCode());

        defaultTopic.setDbs(new HashSet<>());
        defaultTopic.getDbs().add(new Db("Elasticsearch"));

        assertEquals(defaultTopic.getDataFormat(), null);
        defaultTopic.setCorrelateClearedMessage(true);
        defaultTopic.setDataFormat("XML");
        defaultTopic.setEnabled(true);
        defaultTopic.setSaveRaw(true);
        assertTrue(defaultTopic.isCorrelateClearedMessage());
        assertTrue(defaultTopic.isEnabled());
        assertTrue(defaultTopic.isSaveRaw());

        assertEquals(defaultTopic.getTopicConfig().getDataFormat2(), DataFormat.XML);

        defaultTopic.setDataFormat(null);
        assertEquals(testTopic.getDataFormat(), null);

        Topic testTopic1 = new Topic("test");
        assertFalse(testTopic1.isCorrelateClearedMessage());
    }
}
