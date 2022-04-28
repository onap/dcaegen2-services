/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020-2022 Wipro Limited.
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

package org.onap.slice.analysis.ms.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
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
import org.onap.slice.analysis.ms.aai.AaiService;
import org.onap.slice.analysis.ms.configdb.IConfigDbService;
import org.onap.slice.analysis.ms.cps.CpsService;
import org.onap.slice.analysis.ms.models.MLOutputModel;
import org.onap.slice.analysis.ms.models.policy.AdditionalProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MLMessageProcessorTest.class)
public class MLMessageProcessorTest {
    ObjectMapper obj = new ObjectMapper();

    @InjectMocks
    private MLMessageProcessor mlMessageProcessor;

    @Mock
    private IConfigDbService configDbService;

    @Mock
    AaiService aaiService;

    @Mock
    CpsService cpsService;

    @Mock
    private PolicyService policyService;

    @SuppressWarnings({"unchecked"})
    @Test
    public void processMLMsgTest() {
        MLOutputModel mloutput = null;
        MLOutputModel mloutputExp = null;

        Map<String, List<String>> ricToCellMapping = new HashMap<>();
        List<String> myList = new ArrayList<String>();
        myList.add("111");
        myList.add("112");
        ricToCellMapping.put("12", myList);
        myList = new ArrayList<String>();
        myList.add("113");
        myList.add("114");
        ricToCellMapping.put("13", myList);

        try {
            mloutput =
                    obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/MLOutputModel1.json"))),
                            new TypeReference<MLOutputModel>() {});
            mloutputExp =
                    obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/MLOutputModel.json"))),
                            new TypeReference<MLOutputModel>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        when(configDbService.fetchCUCPCellsOfSnssai("0001-0111")).thenReturn(ricToCellMapping);
        AdditionalProperties<MLOutputModel> addProps = new AdditionalProperties<>();
        addProps.setResourceConfig(mloutputExp);
        doNothing().when(policyService).sendOnsetMessageToPolicy(anyString(), any(AdditionalProperties.class),
                anyMap());
        mlMessageProcessor.processMLMsg(mloutput);
        assertEquals(mloutputExp, mloutput);
    }

}
