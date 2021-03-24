/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2021 Wipro Limited.
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
package org.onap.slice.analysis.ms.models;
import static org.junit.Assert.assertEquals;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import nl.jqno.equalsverifier.EqualsVerifier;
@RunWith(SpringRunner.class)
@SpringBootTest(classes=MLOutputModelTest.class)
public class MLOutputModelTest {
    @InjectMocks
    private final MLOutputModel mlOutputModel =new MLOutputModel();
    @Test
    public void mlOutputModelEqualHashcodeTest() {
        EqualsVerifier.simple().forClass(MLOutputModel.class).verify();
    }
    @Test
    public void mlOutputModelMethodTest() {
	List<CUModel> data = Collections.emptyList();
	mlOutputModel.setSnssai("message");
	mlOutputModel.setData(data);
	assertEquals("message",mlOutputModel.getSnssai());
	assertEquals(data,mlOutputModel.getData());
	assertEquals("MLOutputModel [snssai=message, data=[]]",mlOutputModel.toString());
    }
}
