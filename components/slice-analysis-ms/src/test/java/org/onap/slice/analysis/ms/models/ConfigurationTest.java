/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020-2021 Wipro Limited.
 *   Copyright (C) 2022 Huawei Canada Limited.
 *   Copyright (C) 2022 Huawei Technologies Co., Ltd.
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
package org.onap.slice.analysis.ms.models;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
public class ConfigurationTest {
    Configuration configuration = Configuration.getInstance();
    @Test
    public void configurationTest() {
        List<String> list = new ArrayList<String>();
        list.add("server");
        Map<String, Object> subscribes = Collections.emptyMap();
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
        configuration.setCpsUrl("");
        configuration.setAaiUrl("");
        configuration.setConfigDbEnabled(true);
        configuration.setSamples(10);
        configuration.setMinPercentageChange(50);
        configuration.setInitialDelaySeconds(1000);
        configuration.setVesNotifPollingInterval(5);
        configuration.setVesNotifChangeIdentifier("PM_BW_UPDATE");
        configuration.setVesNotifChangeType("BandwidthChanged");
        configuration.setCcvpnEvalInterval(5);
        configuration.setCcvpnEvalPrecision(100);
        configuration.setCcvpnEvalUpperThreshold(0.8);
        configuration.setCcvpnEvalLowerThreshold(0.3);
        configuration.setCcvpnEvalStrategy("FlexibleThresholdStrategy");
        assertEquals(true,configuration.isSecured());
        assertEquals("user", configuration.getAafUsername());
        assertEquals("password", configuration.getAafPassword());
        assertEquals(subscribes,configuration.getStreamsSubscribes());
        assertEquals(subscribes,configuration.getStreamsPublishes());
        assertEquals("cg", configuration.getCg());
        assertEquals("cid", configuration.getCid());
        assertEquals(30, configuration.getPollingInterval());
        assertEquals(100, configuration.getPollingTimeout());
        assertEquals("pg", configuration.getPgHost());
        assertEquals(5432, configuration.getPgPort());
        assertEquals("user", configuration.getPgUsername());
        assertEquals("password", configuration.getPgPassword());
        assertEquals(list, configuration.getDmaapServers());
        assertEquals("sdnrService", configuration.getConfigDbService());
        assertEquals("",configuration.getCpsUrl());
        assertEquals("",configuration.getAaiUrl());
        assertEquals(true,configuration.getConfigDbEnabled());
        assertEquals(10,configuration.getSamples());
        assertEquals(50,configuration.getMinPercentageChange());
        assertEquals(1000,configuration.getInitialDelaySeconds());
        assertEquals(5, configuration.getVesNotifPollingInterval());
        assertEquals("PM_BW_UPDATE", configuration.getVesNotifChangeIdentifier());
        assertEquals("BandwidthChanged", configuration.getVesNotifChangeType());
        assertEquals(5, configuration.getCcvpnEvalInterval());
        assertEquals(100.0, configuration.getCcvpnEvalPrecision(), 0.001);
        assertEquals(0.8, configuration.getCcvpnEvalUpperThreshold(), 0.001);
        assertEquals(0.3, configuration.getCcvpnEvalLowerThreshold(), 0.001);
        assertEquals("FlexibleThresholdStrategy", configuration.getCcvpnEvalStrategy());
    }
}
