/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2021-2022 Wipro Limited.
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

package org.onap.slice.analysis.ms.cps;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.restclients.CpsRestClient;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({CpsService.class, Configuration.class})
@SpringBootTest(classes = CpsInterfaceServiceTest.class)
public class CpsInterfaceServiceTest {
    @InjectMocks
    CpsService cpsService;

    @Mock
    CpsRestClient restClient;

    @Test
    public void fetchCurrentConfigurationOfRICTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("dLThptPerSlice", 10);
        map.put("uLThptPerSlice", 10);
        map.put("maxNumberOfConns", 10);
        Map<String, Map<String, Object>> responseMap = new HashMap<>();
        responseMap.put("11", map);
        try {
            String serviceInstance = new String(Files.readAllBytes(Paths.get("src/test/resources/sliceConfig.json")));
            Mockito.when(restClient.sendPostRequest(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(new ResponseEntity<>(serviceInstance, HttpStatus.OK));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(responseMap, cpsService.fetchCurrentConfigurationOfRIC("111-1111"));
    }

    @Test
    public void fetchNetworkFunctionsOfSnssaiTest() {
        List<String> responseList = new ArrayList<>();
        responseList.add("22");
        responseList.add("23");
        try {
            String serviceInstance = new String(Files.readAllBytes(Paths.get("src/test/resources/DUList.json")));
            Mockito.when(restClient.sendPostRequest(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(new ResponseEntity<>(serviceInstance, HttpStatus.OK));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(responseList, cpsService.fetchNetworkFunctionsOfSnssai("111-1111"));
    }

    @Test
    public void fetchRICsOfSnssaiTest() {
        Map<String, List<String>> responseMap = new HashMap<>();
        List<String> cellslist = new ArrayList<>();
        cellslist.add("1599");
        cellslist.add("1598");
        responseMap.put("11", cellslist);
        try {
            String serviceInstance = new String(Files.readAllBytes(Paths.get("src/test/resources/DUCellsList.json")));
            Mockito.when(restClient.sendPostRequest(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(new ResponseEntity<>(serviceInstance, HttpStatus.OK));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(responseMap, cpsService.fetchRICsOfSnssai("111-1111"));
    }
}
