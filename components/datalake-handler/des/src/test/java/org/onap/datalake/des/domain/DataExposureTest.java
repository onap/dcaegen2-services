/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2020 China Mobile
 *=================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.datalake.des.domain;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.onap.datalake.des.dto.DataExposureConfig;
import org.onap.datalake.des.util.TestUtil;

/**
 * Test Data Exposure.
 *
 * @author Kai Lu
 */
public class DataExposureTest {

    @Test
    public void test() {
        DataExposure dataExposure = new DataExposure("1",
                " select event.commonEventHeader.sourceName as name, "
                + " event.perf3gppFields.measDataCollection.measuredEntityDn as entity "
                + " from datalake where event.commonEventHeader.sourceName = '${name}' ");

        Db mongoDb = TestUtil.newDb("MongoDB");
        dataExposure.setDb(mongoDb);
        dataExposure.setNote("testsql");
        DataExposureConfig config = dataExposure.getDataExposureConfig();
        assertNotNull(config.getId());
        assertNotNull(config.getSqlTemplate());

    }

}
