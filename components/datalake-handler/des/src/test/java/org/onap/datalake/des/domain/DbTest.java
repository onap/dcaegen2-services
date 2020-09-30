/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake DES
 * ================================================================================
 * Copyright 2020 China Mobile
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

package org.onap.datalake.des.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.datalake.des.util.TestUtil;

/**
 * Test Db.
 *
 * @author Kai Lu
 */

public class DbTest {

    @Test
    public void testIs() {

        Db couchbase = TestUtil.newDb("Couchbase");
        Db mongoDb1 = TestUtil.newDb("MongoDB");
        Db mongoDb2 = TestUtil.newDb("MongoDB");
        assertNotEquals(couchbase.hashCode(), mongoDb1.hashCode());
        assertNotEquals(couchbase, mongoDb1);
        assertNotEquals(mongoDb1, mongoDb2);
        assertEquals(mongoDb1, mongoDb1);
        assertFalse(mongoDb2.equals(null));

        DbType dbType = new DbType("MONGO", "MongoDB");
        dbType.setTool(false);
        mongoDb1.setDbType(dbType);

        assertNotEquals(mongoDb2, dbType);
        assertFalse(mongoDb1.getDbType().isTool());

        mongoDb2.setHost("localhost");
        mongoDb2.setPort(1234);
        mongoDb2.setLogin("root");
        mongoDb2.setPass("root123");
        mongoDb2.setDatabase("mongoDB2");
        mongoDb2.setEncrypt(true);
        mongoDb2.setProperty1("property1");
        mongoDb2.setProperty2("property2");
        mongoDb2.setProperty3("property3");
        assertTrue("localhost".equals(mongoDb2.getHost()));
        assertFalse("1234".equals(mongoDb2.getPort()));
        assertTrue("root".equals(mongoDb2.getLogin()));
        assertTrue("root123".equals(mongoDb2.getPass()));
        assertTrue("mongoDB2".equals(mongoDb2.getDatabase()));
        assertFalse("true".equals(mongoDb2.isEncrypt()));
        assertTrue("property1".equals(mongoDb2.getProperty1()));
        assertTrue("property2".equals(mongoDb2.getProperty2()));
        assertTrue("property3".equals(mongoDb2.getProperty3()));
        assertEquals(mongoDb2.getDbConfig().getHost(), mongoDb2.getHost());
    }
}
