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
package org.onap.slice.analysis.ms.models;
import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import nl.jqno.equalsverifier.EqualsVerifier;
@RunWith(SpringRunner.class)
@SpringBootTest(classes=MLOutputModelTest.class)
public class MLOutputModelTest {
    @InjectMocks
    MLOutputModel mlOutputModel =new MLOutputModel();
    @Test
    public void testGetterSetterMLOutputModel() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MLOutputModel.class);
        validateMd(pojoclass);
    } 
    @Test
    public void mlOutputModelHashcodeTest() {
    	EqualsVerifier.simple().forClass(MLOutputModel.class).verify();
    }
    @Test
    public void toStringTest() {
	String snssai="message";
	List<CUModel> data = null;
	mlOutputModel.setSnssai(snssai);
	mlOutputModel.setData(data);
	String expected="MLOutputModel [snssai=message, data=null]";
	assertEquals(expected,mlOutputModel.toString());
    }
    public void validateMd(PojoClass pojoclass) {
        Validator validator = ValidatorBuilder
                    .create()
                    .with(new SetterMustExistRule())
                    .with(new GetterMustExistRule())
                    .with(new SetterTester())
                    .with(new GetterTester())
                    .build();
        validator.validate(pojoclass);
    }
}
