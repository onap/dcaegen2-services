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
import org.onap.datalake.feeder.util.TestUtil;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test Db
 *
 * @author Guobiao Mo
 */

public class DbTest {

    @Test
    public void testIs() {

        Db couchbase = TestUtil.newDb("Couchbase");
        Db mongoDB = TestUtil.newDb("MongoDB");
        Db mongoDB2 = TestUtil.newDb("MongoDB");
        assertNotEquals(couchbase.hashCode(), mongoDB.hashCode());
        assertNotEquals(couchbase, mongoDB);
        assertNotEquals(mongoDB, mongoDB2);
        assertEquals(mongoDB, mongoDB);
        assertFalse(mongoDB2.equals(null)); 
        
        DbType dbType = new DbType("MONGO", "MongoDB");
        dbType.setTool(false);
        mongoDB.setDbType(dbType);

        assertNotEquals(mongoDB2, dbType); 
        assertFalse(mongoDB.isTool()); 
        assertFalse(mongoDB.isHdfs()); 
        assertFalse(mongoDB.isElasticsearch()); 
        assertFalse(mongoDB.isCouchbase()); 
        assertFalse(mongoDB.isDruid());    
        assertTrue(mongoDB.isMongoDB());    
        assertFalse(mongoDB.getDbType().isTool());      
        System.out.println(mongoDB);
        
        new Db();
        mongoDB2.setHost("localhost");
        mongoDB2.setPort(1234);
        mongoDB2.setLogin("root");
        mongoDB2.setPass("root123");
        mongoDB2.setDatabase("mongoDB2");
        mongoDB2.setEncrypt(true);
        mongoDB2.setProperty1("property1");
        mongoDB2.setProperty2("property2");
        mongoDB2.setProperty3("property3");
        Set<Topic> hash_set = new HashSet<>();
        Topic topic = TestUtil.newTopic("topic1");
        topic.setId(1);
        hash_set.add(topic);
        mongoDB2.setTopics(hash_set);
        assertTrue("localhost".equals(mongoDB2.getHost()));
        assertFalse("1234".equals(mongoDB2.getPort()));
        assertTrue("root".equals(mongoDB2.getLogin()));
        assertTrue("root123".equals(mongoDB2.getPass()));
        assertTrue("mongoDB2".equals(mongoDB2.getDatabase()));
        assertFalse("true".equals(mongoDB2.isEncrypt()));
        assertTrue("property1".equals(mongoDB2.getProperty1()));
        assertTrue("property2".equals(mongoDB2.getProperty2()));
        assertTrue("property3".equals(mongoDB2.getProperty3()));
        assertFalse("topic1".equals(mongoDB2.getTopics()));
    }
}
