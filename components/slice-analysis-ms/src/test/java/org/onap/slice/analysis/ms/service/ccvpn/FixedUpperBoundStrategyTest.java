/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Huawei Canada Limited.
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
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FixedUpperBoundStrategyTest.class)
public class FixedUpperBoundStrategyTest {

    @Spy
    @InjectMocks
    BandwidthEvaluator bandwidthEvaluator;

    @Spy
    @InjectMocks
    FixedUpperBoundStrategy fixedUpperBoundStrategy;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void initTest() {
        fixedUpperBoundStrategy.init();
        Mockito.verify(fixedUpperBoundStrategy, Mockito.atLeastOnce()).init();
    }

    @Test
    public void executeTest() {
        Event evt = new SimpleEvent(null, "{}");
        fixedUpperBoundStrategy.execute(evt);
        Mockito.verify(fixedUpperBoundStrategy, Mockito.atLeastOnce())
                .execute(Mockito.any(Event.class));
    }

    @Test
    public void getNameTest() {
        fixedUpperBoundStrategy.getName();
        Mockito.verify(fixedUpperBoundStrategy, Mockito.atLeastOnce()).getName();
    }
}
