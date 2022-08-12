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

package org.onap.slice.analysis.ms.models;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = SliceConfigResponseTest.class)
public class SliceConfigResponseTest {

    @Test
    public void SliceConfigResponseTest() {

        List<SliceConfigDetails> sliceConfigDetails = new ArrayList<>();

        SliceConfigResponse sliceConfigResponse = new SliceConfigResponse();
        sliceConfigResponse.setSliceConfigDetails(sliceConfigDetails);


        assertEquals(sliceConfigDetails, sliceConfigResponse.getSliceConfigDetails());
    }

    @Test
    public void SliceConfigResponseEqualsTest() {

        List<SliceConfigDetails> sliceConfigDetails = new ArrayList<>();

        SliceConfigResponse sliceConfigResponse = new SliceConfigResponse();
        sliceConfigResponse.setSliceConfigDetails(sliceConfigDetails);

        SliceConfigResponse sliceConfigResponse1 = new SliceConfigResponse();
        sliceConfigResponse1.setSliceConfigDetails(sliceConfigDetails);


        assertTrue(sliceConfigResponse1.equals(sliceConfigResponse));
        assertTrue(StringUtils.isNotBlank(sliceConfigResponse.toString()));
        assertTrue(sliceConfigResponse.hashCode() != 0);
    }
    @Test
    public void equalsContract() {
        EqualsVerifier.simple().forClass(SliceConfigResponse.class)
                .withIgnoredAnnotations(NotEmpty.class)
                .verify();
    }
}
