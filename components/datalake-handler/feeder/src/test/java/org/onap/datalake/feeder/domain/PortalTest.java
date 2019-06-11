/*
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE
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

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class PortalTest {

    @Test
    public void testIs() {

        Portal portal = new Portal();
        portal.setName("Kibana");
        portal.setEnabled(true);
        portal.setHost("localhost");
        portal.setPort(5601);
        portal.setLogin("admin");
        portal.setPass("password");
        portal.setDb(new Db("Elasticsearch"));
        assertTrue("Kibana".equals(portal.getName()));
        assertFalse("true".equals(portal.getEnabled()));
        assertTrue("localhost".equals(portal.getHost()));
        assertFalse("5601".equals(portal.getPort()));
        assertTrue("admin".equals(portal.getLogin()));
        assertTrue("password".equals(portal.getPass()));
        assertFalse("Elasticsearch".equals(portal.getDb()));
    }
}