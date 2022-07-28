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

package org.onap.slice.analysis.ms.dmaap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.slice.analysis.ms.service.MLMessageProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = IntelligentSlicingCallbackTest.class)
public class IntelligentSlicingCallbackTest {


    @Spy
    @InjectMocks
    IntelligentSlicingCallback intelligentSlicingCallback;

    @Mock
    private MLMessageProcessor mlMsMessageProcessor;

    @Test
    public void activateCallBackTest() {
        Mockito.doNothing().when(mlMsMessageProcessor).processMLMsg(Mockito.any());
        intelligentSlicingCallback.activateCallBack("{}");
        verify(mlMsMessageProcessor, times(1)).processMLMsg(any());
    }
}
