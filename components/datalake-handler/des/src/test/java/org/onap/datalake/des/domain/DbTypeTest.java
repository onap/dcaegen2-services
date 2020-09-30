/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake DES
 * ================================================================================
 * Copyright 2020 China Mobile. All rights reserved.
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test Data Type.
 *
 * @author Kai Lu
 */
public class DbTypeTest {

    @Test
    public void test() {
        DbType dbType = new DbType("ES","Elasticsearch");

        dbType.setTool(false); 

        assertNotNull(dbType.toString());
        assertEquals(dbType, dbType);
        assertNotEquals(dbType, null);
        assertNotEquals(dbType, "ES");

        DbType dbType2 = new DbType("MONGO", "MongoDB");
        assertNotEquals(dbType, dbType2);
        assertNotNull(dbType.hashCode());

        assertEquals("MongoDB", dbType2.getName());
        dbType2.setName(null);
        dbType2.setDefaultPort(1);
        assertTrue(1 == dbType2.getDefaultPort());

        dbType2.setDbs(null);
        assertNull(dbType2.getDbs());
    }

}