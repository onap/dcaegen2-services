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
import org.onap.datalake.feeder.util.TestUtil;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test Topic
 *
 * @author Guobiao Mo
 */

public class TopicTest {

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

    @Test
    public void testIs() {
        Topic defaultTopic = TestUtil.newTopic("_DL_DEFAULT_");
        Topic testTopic = TestUtil.newTopic("test");
        testTopic.setId(1);
        Topic testTopic2 = TestUtil.newTopic("test2");
        testTopic2.setId(1);

        assertTrue(testTopic.equals(testTopic2));
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

        //assertEquals(defaultTopic.getTopicConfig().getDataFormat2(), DataFormat.XML);

        defaultTopic.setDataFormat(null);
        assertEquals(testTopic.getDataFormat(), null);

        Topic testTopic1 = TestUtil.newTopic("test");
        assertFalse(testTopic1.isCorrelateClearedMessage());
    }
}
