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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.slice.analysis.ms.models.ConfigPolicy;
import org.onap.slice.analysis.ms.service.ccvpn.CCVPNPmDatastore;
import org.onap.slice.analysis.ms.utils.BeanUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ BeanUtil.class, ConfigPolicy.class })
@SpringBootTest(classes = ConfigThreadTest.class)
public class ConfigThreadTest {

    private ConfigThread configThread;

    @Spy
    @InjectMocks
    private CCVPNPmDatastore ccvpnPmDatastore ;

    @Before
    public void before() throws IllegalAccessException {
        PowerMockito.mockStatic(BeanUtil.class);
        when(BeanUtil.getBean(CCVPNPmDatastore.class)).thenReturn(ccvpnPmDatastore);
        configThread = PowerMockito.spy(new ConfigThread());
    }

    @Test
    public void runShouldFailTest() throws InterruptedException {
        ConfigPolicy configPolicy = mock(ConfigPolicy.class);
        PowerMockito.mockStatic(ConfigPolicy.class);
        PowerMockito.when(ConfigPolicy.getInstance()).thenReturn(configPolicy);
        when(configPolicy.getConfig()).thenReturn(null).thenThrow(new RuntimeException());

        Thread thread = new Thread(configThread);
        thread.start();
        thread.join(3000);
        Mockito.verify(configThread, Mockito.atLeastOnce()).run();
    }

    @Test
    public void runShouldSuccessTest() throws InterruptedException {
        ConfigPolicy configPolicy = ConfigPolicy.getInstance();
        Map<String, Object> config = new HashMap<>(){{
            put("cllId", "testCllId");
            put("closedLoopStatus", "true");
            put("originalBw", "300");
        }};
        configPolicy.setConfig(config);

        Thread thread = new Thread(configThread);
        final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        try {
            thread.start();
            thread.join(3000);
        } catch (Throwable e) {
            exceptions.add(e);
        }

        Assert.assertTrue(exceptions.isEmpty());
    }

}
