/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Huawei Technologies Co., Ltd.
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
package org.onap.slice.analysis.ms.service.ccvpn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FixedUpperBoundStrategyTest.class)
public class FlexibleThresholdStrategyTest {

    @Spy
    @InjectMocks
    BandwidthEvaluator bandwidthEvaluator;

    @Spy
    @InjectMocks
    FlexibleThresholdStrategy flexibleThresholdStrategy;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void initTest() {
        flexibleThresholdStrategy.init();
        Mockito.verify(flexibleThresholdStrategy, Mockito.atLeastOnce()).init();
    }

    @Test
    public void executeTest() {
        Event evt = new SimpleEvent(null, "{}");
        flexibleThresholdStrategy.execute(evt);
        Mockito.verify(flexibleThresholdStrategy, Mockito.atLeastOnce())
            .execute(Mockito.any(Event.class));
    }

    @Test
    public void getNameTest() {
        flexibleThresholdStrategy.getName();
        Mockito.verify(flexibleThresholdStrategy, Mockito.atLeastOnce()).getName();
    }
}
