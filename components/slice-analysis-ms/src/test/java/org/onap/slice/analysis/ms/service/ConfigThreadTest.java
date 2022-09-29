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

package org.onap.slice.analysis.ms.service;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.slice.analysis.ms.models.ConfigPolicy;
import org.onap.slice.analysis.ms.service.ccvpn.CCVPNPmDatastore;
import org.onap.slice.analysis.ms.utils.BeanUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ BeanUtil.class, ConfigPolicy.class })
@SpringBootTest(classes = ConfigThreadTest.class)
public class ConfigThreadTest {

    private ConfigThread configThread;

    @Mock
    private CCVPNPmDatastore ccvpnPmDatastore;

    @Before
    public void before() throws IllegalAccessException {
        PowerMockito.mockStatic(BeanUtil.class);
        when(BeanUtil.getBean(CCVPNPmDatastore.class)).thenReturn(ccvpnPmDatastore);
        configThread = PowerMockito.spy(new ConfigThread());
        MemberModifier.field(ConfigThread.class, "ccvpnPmDatastore")
            .set(configThread , ccvpnPmDatastore);
    }

    @Test
    public void runShouldFailTest() {
        ConfigPolicy configPolicy = mock(ConfigPolicy.class);
        PowerMockito.mockStatic(ConfigPolicy.class);
        PowerMockito.when(ConfigPolicy.getInstance()).thenReturn(configPolicy);
        when(configPolicy.getConfig()).thenReturn(null).thenThrow(new RuntimeException());
        configThread.run();
        Mockito.verify(configThread, Mockito.atLeastOnce()).run();
    }

    @Test
    public void runShouldSucceedTest() {
        ConfigPolicy configPolicy = mock(ConfigPolicy.class);
        PowerMockito.mockStatic(ConfigPolicy.class);
        PowerMockito.when(ConfigPolicy.getInstance()).thenReturn(configPolicy);
        Map<String, Object> config = mock(HashMap.class);
        when(configPolicy.getConfig()).thenReturn(config);
        when(config.containsKey("cllId")).thenReturn(true, false);
        when(config.get("cllId")).thenReturn("testCllId");
        when(config.containsKey("closedLoopStatus")).thenReturn(true, false);
        when(config.get("closedLoopStatus")).thenReturn("true");
        when(config.containsKey("originalBw")).thenReturn(true, false);
        when(config.get("originalBw")).thenReturn("300");
        doThrow(new RuntimeException()).when(ccvpnPmDatastore).updateConfigFromPolicy("testCllId", true, 300);

        configThread.run();
        Mockito.verify(configThread, Mockito.atLeastOnce()).run();
    }

}
