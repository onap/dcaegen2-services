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

package org.onap.dcaegen2.kpi.computation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.dcaegen2.kpi.config.KpiConfig;
import org.onap.dcaegen2.kpi.config.KpiJsonConversion;
import org.onap.dcaegen2.kpi.models.VesEvent;
import org.onap.dcaegen2.kpi.utils.VesJsonConversion;

public class KpiTest {

    private static final String KPI_CONFIG_FILE = "kpi/kpi_config.json";
    private static final String VES_MESSAGE_FILE = "kpi/ves_message.json";

    @Test
    public void testKpiConfigValidate() {

        String strKpiConfig = FileUtils.getFileContents(KPI_CONFIG_FILE);

        KpiConfig kpiConfig = KpiJsonConversion.convertKpiConfig(strKpiConfig);
        assertEquals(kpiConfig.getDomain(), "measurementsForKpi");
    }

    @Test
    public void testVesEventValidate() {

        String vesMessage = FileUtils.getFileContents(VES_MESSAGE_FILE);

        VesEvent vesEvent = VesJsonConversion.convertVesEvent(vesMessage);
        assertEquals(vesEvent.getEvent().getCommonEventHeader().getDomain(), "perf3gpp");
    }

}
