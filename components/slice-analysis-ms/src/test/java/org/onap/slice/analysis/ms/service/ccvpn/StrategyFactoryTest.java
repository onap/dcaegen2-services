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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class StrategyFactoryTest {

    private static final String STRATEGY = "FixedUpperBoundStrategy";
    @Mock
    FixedUpperBoundStrategy fixedUpperBoundStrategy;

    @Spy
    private List<EvaluationStrategy> strategies = new ArrayList<>();

    @Spy
    @InjectMocks
    StrategyFactory strategyFactory;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        strategies.add(fixedUpperBoundStrategy);
    }

    @Test
    public void getStrategyTest(){
        when(fixedUpperBoundStrategy.getName()).thenReturn(STRATEGY);
        assertEquals(STRATEGY, strategyFactory.getStrategy(STRATEGY).getName());
    }
}
