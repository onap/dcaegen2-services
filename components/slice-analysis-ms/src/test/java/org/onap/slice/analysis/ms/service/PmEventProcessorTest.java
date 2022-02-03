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

package org.onap.slice.analysis.ms.service;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.onap.slice.analysis.ms.models.MeasurementObject;
import org.onap.slice.analysis.ms.models.pmnotification.Event;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PmEventProcessorTest.class)
public class PmEventProcessorTest {

    @InjectMocks
    PmEventProcessor pmEventProcessor;

    @Test
    public void processEventTest() {
        ObjectMapper obj = new ObjectMapper();
        Event input = null;
        Map<String, List<MeasurementObject>> output = null;
        try {
            input = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/event.json"))),
                    Event.class);
            output = obj.readValue(
                    new String(Files.readAllBytes(Paths.get("src/test/resources/eventProcessorOutput.json"))),
                    new TypeReference<Map<String, List<MeasurementObject>>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(output, pmEventProcessor.processEvent(input));
    }
}
