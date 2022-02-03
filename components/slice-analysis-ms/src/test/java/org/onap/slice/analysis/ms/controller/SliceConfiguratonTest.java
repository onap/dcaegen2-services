/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Wipro Limited.
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

package org.onap.slice.analysis.ms.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.slice.analysis.ms.models.SliceConfigRequest;
import org.onap.slice.analysis.ms.models.SliceConfigResponse;
import org.onap.slice.analysis.ms.service.SliceUtilization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(SliceConfiguraton.class)
public class SliceConfiguratonTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SliceUtilization sliceUtilization;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    private static final String UTF8 = StandardCharsets.UTF_8.name();

    @Test
    public void getSnssaiListTest() throws Exception {
        SliceConfigRequest sliceConfigRequest = new SliceConfigRequest();
        List<String> sliceIdentifiersList = new ArrayList<>();
        sliceIdentifiersList.add("14559ead-f4fe-4c1c-a94c-8015fad3ea35");
        sliceIdentifiersList.add("14559ead-f4fe-4c1c-a94c-8015fad3ea36");
        sliceConfigRequest.setSliceIdentifiers(sliceIdentifiersList);
        List<String> configParamsList = new ArrayList<>();
        configParamsList.add("dLThptPerSlice");
        configParamsList.add("uLThptPerSlice");
        sliceConfigRequest.setConfigParams(configParamsList);

        String executePath = "/api/v1/slices-config";
        String sliceConfigResquestString =
                new String(Files.readAllBytes(Paths.get("src/test/resources/sliceConfigRequest.json")));
        ObjectMapper objectMapper = new ObjectMapper();
        SliceConfigResponse sliceConfigResponse = objectMapper
                .readValue(new File("src/test/resources/sliceConfigResponse.json"), SliceConfigResponse.class);
        Mockito.when(sliceUtilization.getSliceUtilizationData(ArgumentMatchers.any())).thenReturn(sliceConfigResponse);

        mockMvc.perform(get(executePath).contentType(MediaType.APPLICATION_JSON).characterEncoding(UTF8)
                .content(objectMapper.writeValueAsString(sliceConfigRequest)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(sliceConfigResponse)));
    }
}
