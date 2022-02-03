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

package org.onap.slice.analysis.ms.models.pmnotification;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

import org.junit.Test;

public class PmModelsTest {
    @Test
    public void testGetterSetterCommonEventHeader() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(CommonEventHeader.class);
        validateMd(pojoclass);
    }

    @Test
    public void testGetterSetterEvent() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(Event.class);
        validateMd(pojoclass);
    }

    @Test
    public void testGetterSetterMeasDataCollection() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MeasDataCollection.class);
        validateMd(pojoclass);
    }

    @Test
    public void testGetterSetterMeasInfoId() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MeasInfoId.class);
        validateMd(pojoclass);
    }

    @Test
    public void testGetterSetterMeasInfoList() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MeasInfoList.class);
        validateMd(pojoclass);
    }

    @Test
    public void testGetterSetterMeasResult() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MeasResult.class);
        validateMd(pojoclass);
    }

    @Test
    public void testGetterSetterMeasTypes() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MeasTypes.class);
        validateMd(pojoclass);
    }

    @Test
    public void testGetterSetterMeasValuesList() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MeasValuesList.class);
        validateMd(pojoclass);
    }

    @Test
    public void testGetterSetterPerf3gppFields() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(Perf3gppFields.class);
        validateMd(pojoclass);
    }

    @Test
    public void testGetterSetterPmNotification() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(PmNotification.class);
        validateMd(pojoclass);
    }

    public void validateMd(PojoClass pojoclass) {
        Validator validator = ValidatorBuilder.create().with(new SetterMustExistRule()).with(new GetterMustExistRule())
                .with(new SetterTester()).with(new GetterTester()).build();
        validator.validate(pojoclass);
    }
}
