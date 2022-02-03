
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

package org.onap.slice.analysis.ms.dmaap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.slice.analysis.ms.data.repository.PerformanceNotificationsRepository;
import org.onap.slice.analysis.ms.utils.BeanUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({BeanUtil.class})
@SpringBootTest(classes = PmNotificationCallbackTest.class)
public class PmNotificationCallbackTest {

    @Mock
    PerformanceNotificationsRepository performanceNotificationsRepository;

    @Mock
    NewPmNotification newPmNotif;

    @Test
    public void testActivateCallBack() {
        PowerMockito.mockStatic(BeanUtil.class);
        PowerMockito.when(BeanUtil.getBean(PerformanceNotificationsRepository.class))
                .thenReturn(performanceNotificationsRepository);
        PowerMockito.when(BeanUtil.getBean(NewPmNotification.class)).thenReturn(newPmNotif);
        PmNotificationCallback pmNotificationCallback = new PmNotificationCallback();
        pmNotificationCallback.activateCallBack("pmNotification");
    }

}
