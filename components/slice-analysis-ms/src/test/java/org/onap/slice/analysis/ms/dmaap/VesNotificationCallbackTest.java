/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
 *   Copyright (C) 2022 Huawei Canada Limited.
 *   Copyright (C) 2022 Huawei Technologies Co., Ltd.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.slice.analysis.ms.aai.AaiService;
import org.onap.slice.analysis.ms.service.ccvpn.CCVPNPmDatastore;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VesNotificationCallbackTest.class)
public class VesNotificationCallbackTest {
    ObjectMapper obj = new ObjectMapper();

    @Mock
    AaiService aaiService;

    @Mock
    CCVPNPmDatastore ccvpnPmDatastore;

    @Spy
    @InjectMocks
    VesNotificationCallback vesNotificationCallback;

    @Test
    public void initTest() {
        vesNotificationCallback.init();
        Mockito.verify(vesNotificationCallback, Mockito.atLeastOnce()).init();
    }

    @Test
    public void activateCallBackTest() {
        String input = null;
        try {
            input = new String(Files.readAllBytes(Paths.get("src/test/resources/vesCCVPNNotiModel.json")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Set<String> cllInstances = new HashSet<>();
        cllInstances.add("cll-01");
        cllInstances.add("cll-02");
        Mockito.when(aaiService.fetchAllCllInstances()).thenReturn(cllInstances);
        Mockito.doNothing().when(ccvpnPmDatastore).updateCllInstances(Mockito.any());
        vesNotificationCallback.activateCallBack(input);
        Mockito.verify(vesNotificationCallback, Mockito.atLeastOnce()).activateCallBack(Mockito.anyString());
    }
}
