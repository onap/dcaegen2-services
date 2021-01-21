/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021 China Mobile.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.kpi.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.dcaegen2.kpi.computation.FileUtils;
import org.onap.dcaegen2.kpi.config.Kpi;
import org.onap.dcaegen2.kpi.config.KpiConfig;
import org.onap.dcaegen2.kpi.config.MethodForKpi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SerializableTester;
import com.openpojo.validation.test.impl.SetterTester;

public class ModelsTest {

    private static final String CBS_CONFIG_FILE = "kpi/cbs_config1.json";

    public void validateMd(PojoClass pojoclass) {
        Validator validator = ValidatorBuilder.create().with(new SetterMustExistRule()).with(new GetterMustExistRule())
                .with(new SetterTester()).with(new GetterTester()).with(new SerializableTester()).build();
        validator.validate(pojoclass);
    }

    @Test
    public void testGetterSetterPerformanceEvent() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(PerformanceEvent.class);
        validateMd(pojoclass);
    }

    @Test
    public void testGetterSetterKpiConfig() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(KpiConfig.class);
        validateMd(pojoclass);
    }

    @Test
    public void testGetterSetterMethodForKpi() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MethodForKpi.class);
        validateMd(pojoclass);
    }

    @Test
    public void testGetterSetterKpi() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(Kpi.class);
        validateMd(pojoclass);
    }

    @Test
    public void testGetterSetterConfiguration() {

        String strCbsConfig = FileUtils.getFileContents(CBS_CONFIG_FILE);
        JsonObject jsonObject = new JsonParser().parse(strCbsConfig).getAsJsonObject().getAsJsonObject("config");
        Configuration config = new Configuration();
        config.updateConfigurationFromJsonObject(jsonObject);

        assertEquals(config.getAafPassword(), "demo123456!");

    }

}
