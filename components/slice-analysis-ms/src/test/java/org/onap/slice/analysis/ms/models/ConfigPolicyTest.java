/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020-2021 Wipro Limited.
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
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class ConfigPolicyTest {
    @Test
    public void configPolicyTest() {
        ConfigPolicy configPolicy = ConfigPolicy.getInstance();
        Map<String, Object> config = new HashMap<>();
        config.put("policyName", "pcims_policy");
        configPolicy.setConfig(config);
        assertEquals(config, configPolicy.getConfig());
    }
    @Test
    public void toStringTest() {
        ConfigPolicy configPolicy = ConfigPolicy.getInstance();
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("policyName", "pcims_policy");
        configPolicy.setConfig(config);
        String expected="ConfigPolicy [config={policyName=pcims_policy}]";
        assertEquals(expected,configPolicy.toString());
    }
}
