/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
 *   ==============================================================================
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     ============LICENSE_END=========================================================
 *
 *******************************************************************************/


package org.onap.slice.analysis.ms.beans;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;


public class ConfigurationTest {
    Configuration configuration = Configuration.getInstance();

    @Test
    public void configurationTest() {

        List<String> list = new ArrayList<String>();
        list.add("server");
        Map<String, Object> subscribes = new HashMap<>();
        
        configuration.setStreamsSubscribes(subscribes);
        configuration.setStreamsPublishes(subscribes);
        configuration.setDmaapServers(list);
        configuration.setCg("cg");
        configuration.setCid("cid");
        configuration.setAafPassword("password");
        configuration.setAafUsername("user");
        configuration.setPgHost("pg");
        configuration.setPgPort(5432);
        configuration.setPgPassword("password");
        configuration.setPgUsername("user");
        configuration.setPollingInterval(30);
        configuration.setPollingTimeout(100);
        configuration.setConfigDbService("sdnrService");
     
        assertEquals("cg", configuration.getCg());
        assertEquals("cid", configuration.getCid());
        assertEquals("user", configuration.getAafUsername());
        assertEquals("password", configuration.getAafPassword());
        assertEquals("user", configuration.getPgUsername());
        assertEquals("password", configuration.getPgPassword());
        assertEquals("pg", configuration.getPgHost());
        assertEquals(5432, configuration.getPgPort());
        assertEquals(30, configuration.getPollingInterval());
        assertEquals(100, configuration.getPollingTimeout());
        assertEquals("sdnrService", configuration.getConfigDbService());
        assertEquals(list, configuration.getDmaapServers());
    }
}
