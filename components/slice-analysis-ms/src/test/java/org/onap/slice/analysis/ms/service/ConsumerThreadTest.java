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

package org.onap.slice.analysis.ms.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.slice.analysis.ms.configdb.IConfigDbService;
import org.onap.slice.analysis.ms.cps.CpsInterface;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.utils.BeanUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ BeanUtil.class })
@SpringBootTest(classes = ConsumerThreadTest.class)
public class ConsumerThreadTest {

    private ConsumerThread consumerThread;
    @Mock
    private PmDataQueue pmDataQueue;
    @Mock
    private IConfigDbService iConfigDbService;
    @Mock
    private CpsInterface cpsInterface;

    @Before
    public void before() throws IllegalAccessException {

        PowerMockito.mockStatic(BeanUtil.class);
        when(BeanUtil.getBean(PmDataQueue.class)).thenReturn(pmDataQueue);

        when(BeanUtil.getBean(IConfigDbService.class)).thenReturn(iConfigDbService);

        when(BeanUtil.getBean(CpsInterface.class)).thenReturn(cpsInterface);

        consumerThread = PowerMockito.spy(new ConsumerThread());

        MemberModifier.field(ConsumerThread.class, "cpsInterface")
                .set(consumerThread , cpsInterface);
        MemberModifier.field(ConsumerThread.class, "samples")
                .set(consumerThread , 1);
    }

    @Test
    public void run1Test() {
        String snssai = "snssai";
        doReturn("snssai").when(pmDataQueue).getSnnsaiFromQueue();
        List<String> nfs = new ArrayList<>();
        when(iConfigDbService.fetchNetworkFunctionsOfSnssai(Mockito.any())).thenReturn(nfs);
        when(pmDataQueue.checkSamplesInQueue(Mockito.any(), Mockito.anyInt())).thenReturn(true);


        SnssaiSamplesProcessor snssaiSamplesProcessor = PowerMockito.mock(SnssaiSamplesProcessor.class);
        when(BeanUtil.getBean(SnssaiSamplesProcessor.class)).thenReturn(snssaiSamplesProcessor);
        Mockito.when(snssaiSamplesProcessor.processSamplesOfSnnsai(snssai, nfs)).thenReturn(false);
        Mockito.doNothing().when(pmDataQueue).putSnssaiToQueue(snssai);
        doThrow(new RuntimeException()).when(pmDataQueue).putSnssaiToQueue(snssai);
        consumerThread.run();
        assertEquals(1, 1);
    }

    @Test
    public void run2Test() {
        Configuration.getInstance().setConfigDbEnabled(false);
        String snssai = "snssai";
        doReturn("snssai").when(pmDataQueue).getSnnsaiFromQueue();
        List<String> nfs = new ArrayList<>();
        when(iConfigDbService.fetchNetworkFunctionsOfSnssai(Mockito.any())).thenReturn(nfs);
        when(pmDataQueue.checkSamplesInQueue(Mockito.any(), Mockito.anyInt())).thenReturn(true);

        when(cpsInterface.fetchNetworkFunctionsOfSnssai(snssai)).thenThrow(new RuntimeException());

        SnssaiSamplesProcessor snssaiSamplesProcessor = PowerMockito.mock(SnssaiSamplesProcessor.class);
        when(BeanUtil.getBean(SnssaiSamplesProcessor.class)).thenReturn(snssaiSamplesProcessor);
        Mockito.when(snssaiSamplesProcessor.processSamplesOfSnnsai(snssai, nfs)).thenReturn(false);
        Mockito.doNothing().when(pmDataQueue).putSnssaiToQueue(snssai);
        doThrow(new RuntimeException()).when(pmDataQueue).putSnssaiToQueue(snssai);
        consumerThread.run();
        assertEquals(1, 1);
    }

    @Test
    public void checkForEnoughSamplesTest() {
        List<String> nfs = new ArrayList<>();
        nfs.add("");
        when(pmDataQueue.checkSamplesInQueue(Mockito.any(), Mockito.anyInt())).thenReturn(false);
        doNothing().when(pmDataQueue).putSnssaiToQueue(Mockito.any());
        assertFalse(consumerThread.checkForEnoughSamples(nfs, ""));

    }
}
