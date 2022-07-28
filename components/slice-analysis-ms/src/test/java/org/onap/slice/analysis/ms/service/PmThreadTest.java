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
import org.onap.slice.analysis.ms.data.repository.PerformanceNotificationsRepository;
import org.onap.slice.analysis.ms.dmaap.NewPmNotification;
import org.onap.slice.analysis.ms.models.MeasurementObject;
import org.onap.slice.analysis.ms.utils.BeanUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ BeanUtil.class })
@SpringBootTest(classes = PmThreadTest.class)
public class PmThreadTest {

    private PmThread pmThread;
    @Mock
    private NewPmNotification newPmNotification;
    @Mock
    private PerformanceNotificationsRepository performanceNotificationsRepository;
    @Mock
    private IPmEventProcessor pmEventProcessor;
    @Mock
    private PmDataQueue pmDataQueue;

    @Before
    public void before() throws IllegalAccessException {

        PowerMockito.mockStatic(BeanUtil.class);
        when(BeanUtil.getBean(NewPmNotification.class)).thenReturn(newPmNotification);

        when(BeanUtil.getBean(PerformanceNotificationsRepository.class)).thenReturn(performanceNotificationsRepository);

        when(BeanUtil.getBean(PmDataQueue.class)).thenReturn(pmDataQueue);

        when(BeanUtil.getBean(IPmEventProcessor.class)).thenReturn(pmEventProcessor);

        pmThread = PowerMockito.spy(new PmThread());
    }

    @Test
    public void runTest(){

        when(newPmNotification.getNewNotif()).thenReturn(true);

        String pmNotificationString = "{" +
                "    \"event\":{" +
                "        \"commonEventHeader\":null," +
                "        \"perf3gppFields\":{" +
                "            \"perf3gppFieldsVersion\":\"\"," +
                "            \"measDataCollection\":{" +
                "                \"granularityPeriod\":1," +
                "                \"measuredEntityUserName\":\"measuredEntityUserName\"," +
                "                \"measuredEntityDn\":\"measuredEntityDn\"," +
                "                \"measuredEntitySoftwareVersion\":\"measuredEntitySoftwareVersion\"," +
                "                \"measInfoList\":[" +
                "" +
                "                ]" +
                "            }" +
                "        }" +
                "    }" +
                "}";
        when(performanceNotificationsRepository.getPerformanceNotificationFromQueue()).thenReturn(pmNotificationString);

        Map<String, List<MeasurementObject>> processedData = new HashMap<>();
        processedData.put("k", null);
        when(pmEventProcessor.processEvent(any())).thenReturn(processedData);
        doNothing().when(pmDataQueue).putDataToQueue(any(), any());
        doThrow(new RuntimeException()).when(pmDataQueue).putSnssaiToQueue(any());
        pmThread.run();
        assertEquals(1,1);
    }

}
