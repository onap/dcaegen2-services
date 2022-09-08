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
package org.onap.slice.analysis.ms.models.policy;

import org.junit.Test;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class PolicyModelsTest {
	@Test
    public void testGetterSetterPayload() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(Payload.class);
        validateMd(pojoclass);
    }
	
	@Test
    public void testGetterSetterAAI() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(AAI.class);
        validateMd(pojoclass);
    }
	
	@Test
    public void testGetterSetterOnsetMessage() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(OnsetMessage.class);
        validateMd(pojoclass);
    }
	
	@Test
    public void testGetterSetterAdditionalProperties() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(AdditionalProperties.class);
        validateMd(pojoclass);
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
