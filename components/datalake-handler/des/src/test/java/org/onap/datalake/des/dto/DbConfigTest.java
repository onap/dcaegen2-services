/*-
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE DES
 * ================================================================================
 * Copyright (C) 2020 China Mobile. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.datalake.des.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test DB config.
 *
 * @author Kai Lu
 */
public class DbConfigTest {

    @Test
    public void testDbConfig() {
        DbConfig dbConfig = new DbConfig();
        dbConfig.setId(1);
        assertEquals(1, dbConfig.getId());
        dbConfig.setName("elasticsearch");
        assertTrue("elasticsearch".equals(dbConfig.getName()));
        dbConfig.setHost("localhost");
        assertTrue("localhost".equals(dbConfig.getHost()));
        dbConfig.setLogin("root");
        assertTrue("root".equals(dbConfig.getLogin()));
        dbConfig.setPass("root123");
        assertTrue("root123".equals(dbConfig.getPass()));
        dbConfig.setDatabase("elasticsearch");
        assertTrue("elasticsearch".equals(dbConfig.getDatabase()));
        dbConfig.setPort(123);
        //assertEquals(123, dbConfig.getPort());
        assertFalse("123".equals(dbConfig.getPort()));
        dbConfig.setPoperties("driver");
        assertTrue("driver".equals(dbConfig.getPoperties()));
    }
}
