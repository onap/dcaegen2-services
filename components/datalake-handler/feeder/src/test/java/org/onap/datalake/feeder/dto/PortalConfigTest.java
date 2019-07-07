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

package org.onap.datalake.feeder.dto;

import org.junit.Test;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Portal;
import org.onap.datalake.feeder.util.TestUtil;

import static org.junit.Assert.*;

public class PortalConfigTest {

    @Test
    public void testIs(){

        Portal testPortal = new Portal();
        testPortal.setName("Kibana");
        testPortal.setDb(TestUtil.newDb("Elasticsearch"));
        Portal testPortal2 = new Portal();
        testPortal2.setName("Kibana");
        testPortal2.setDb(TestUtil.newDb("Elasticsearch"));
        PortalConfig testPortalConfig = testPortal.getPortalConfig();
        assertNotEquals(testPortalConfig, testPortal2.getPortalConfig());
        assertNotEquals(testPortalConfig, testPortal);
        assertNotEquals(testPortalConfig, null);
        assertEquals(testPortalConfig.getHost(), null);
        assertEquals(testPortalConfig.getPort(), null);
        assertEquals(testPortalConfig.getEnabled(), null);
        assertEquals(testPortalConfig.getLogin(), null);
        assertEquals(testPortalConfig.getPass(), null);
        assertEquals(testPortalConfig.getDb(), "Elasticsearch");
    }
}