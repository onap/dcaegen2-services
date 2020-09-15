/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
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
    }
}
