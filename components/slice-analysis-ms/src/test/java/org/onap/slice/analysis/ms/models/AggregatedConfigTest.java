/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *  Copyright (C) 2022 CTC, Inc.
 *  ==============================================================================
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

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = AggregatedConfigTest.class)
public class AggregatedConfigTest {

    @Test
    public void AggregatedConfigTest() {

        AggregatedConfig aggregatedConfig = new AggregatedConfig();
        aggregatedConfig.setDLThptPerSlice(1);
        aggregatedConfig.setULThptPerSlice(2);
        aggregatedConfig.setMaxNumberOfConns(3);

        assertEquals(1, aggregatedConfig.getDLThptPerSlice());
        assertEquals(2, aggregatedConfig.getULThptPerSlice());
        assertEquals(3, aggregatedConfig.getMaxNumberOfConns());
    }

    @Test
    public void RelationshipListEqualsTest() {


        AggregatedConfig aggregatedConfig = new AggregatedConfig();
        aggregatedConfig.setDLThptPerSlice(1);
        aggregatedConfig.setULThptPerSlice(2);
        aggregatedConfig.setMaxNumberOfConns(3);


        AggregatedConfig aggregatedConfig2 = new AggregatedConfig();
        aggregatedConfig2.setDLThptPerSlice(1);
        aggregatedConfig2.setULThptPerSlice(2);
        aggregatedConfig2.setMaxNumberOfConns(3);


        assertTrue(aggregatedConfig2.equals(aggregatedConfig));
        assertTrue(StringUtils.isNotBlank(aggregatedConfig.toString()));
        assertTrue(aggregatedConfig.hashCode() != 0);
    }
}