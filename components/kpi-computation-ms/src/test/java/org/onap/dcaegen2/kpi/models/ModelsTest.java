/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 China Mobile.
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

import org.junit.Assert;
import org.junit.Test;
import org.onap.dcaegen2.kpi.computation.FileUtils;
import org.onap.dcaegen2.kpi.config.BaseDynamicPropertiesProvider;
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
        Validator validator = ValidatorBuilder.create()
                .with(new SetterMustExistRule())
                .with(new GetterMustExistRule())
                .with(new SetterTester())
                .with(new GetterTester())
                .with(new SerializableTester())
                .build();
        validator.validate(pojoclass);
    }

    @Test
    public void testGetterSetterPerformanceEvent() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(PerformanceEvent.class);
        validateMd(pojoclass);
        PerformanceEvent x = new PerformanceEvent();
        PerformanceEvent y = new PerformanceEvent();
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());
    }

    @Test
    public void testMeasTypes() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MeasTypes.class);
        validateMd(pojoclass);
        MeasTypes x = new MeasTypes();
        MeasTypes y = new MeasTypes();
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());
    }

    @Test
    public void testMeasInfoId() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MeasInfoId.class);
        validateMd(pojoclass);
        MeasInfoId x = new MeasInfoId();
        MeasInfoId y = new MeasInfoId();
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());
    }

    @Test
    public void testVesEvent() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(VesEvent.class);
        validateMd(pojoclass);
        VesEvent x = new VesEvent();
        VesEvent y = new VesEvent();
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());
    }

    @Test
    public void testMeasResult() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MeasResult.class);
        validateMd(pojoclass);
        MeasResult x = new MeasResult();
        MeasResult y = new MeasResult();
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());
    }

    @Test
    public void testPerf3gppFields() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(Perf3gppFields.class);
        validateMd(pojoclass);
        Perf3gppFields x = new Perf3gppFields();
        Perf3gppFields y = new Perf3gppFields();
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());
    }

    @Test
    public void testMeasInfo() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MeasInfo.class);
        validateMd(pojoclass);
        MeasInfo x = new MeasInfo();
        MeasInfo y = new MeasInfo();
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());
    }

    @Test
    public void testMeasValues() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MeasValues.class);
        validateMd(pojoclass);
        MeasValues x = new MeasValues();
        MeasValues y = new MeasValues();
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());
    }

    @Test
    public void testMeasDataCollection() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MeasDataCollection.class);
        validateMd(pojoclass);
        MeasDataCollection x = new MeasDataCollection();
        MeasDataCollection y = new MeasDataCollection();
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());
    }

    @Test
    public void testCommonEventHeader() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(CommonEventHeader.class);
        validateMd(pojoclass);
        CommonEventHeader x = new CommonEventHeader();
        CommonEventHeader y = new CommonEventHeader();
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());
    }

    @Test
    public void testGetterSetterKpiConfig() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(KpiConfig.class);
        validateMd(pojoclass);
        KpiConfig x = new KpiConfig();
        KpiConfig y = new KpiConfig();
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());
    }

    @Test
    public void testGetterSetterMethodForKpi() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(MethodForKpi.class);
        validateMd(pojoclass);
        MethodForKpi x = new MethodForKpi();
        MethodForKpi y = new MethodForKpi();
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());
    }

    @Test
    public void testGetterSetterKpi() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(Kpi.class);
        validateMd(pojoclass);
        Kpi x = new Kpi();
        Kpi y = new Kpi();
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());
    }

    @Test
    public void testGetterSetterConfiguration() {

        String strCbsConfig = FileUtils.getFileContents(CBS_CONFIG_FILE);
        JsonObject jsonObject = new JsonParser().parse(strCbsConfig).getAsJsonObject().getAsJsonObject("config");
        Configuration config = new Configuration();
        config.updateConfigurationFromJsonObject(jsonObject);

        assertEquals(config.getAafPassword(), "demo123456!");

    }

    @Test
    public void testBaseDynamicPropertiesProvider() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(BaseDynamicPropertiesProvider.class);
        validateMd(pojoclass);
        BaseDynamicPropertiesProvider x = new BaseDynamicPropertiesProvider();
        BaseDynamicPropertiesProvider y = new BaseDynamicPropertiesProvider();
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.hashCode() == y.hashCode());
    }

}
