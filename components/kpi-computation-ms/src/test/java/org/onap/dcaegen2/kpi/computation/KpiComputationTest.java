/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 China Mobile.
 *  Copyright (C) 2022 Deutsche Telekom AG. All rights reserved.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.onap.dcaegen2.kpi.computation.KpiComputation;
import org.onap.dcaegen2.kpi.models.Configuration;
import org.onap.dcaegen2.kpi.models.VesEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class KpiComputationTest {

    private static final String KPI_CONFIG_FILE = "kpi/kpi_config.json";
    private static final String VES_MESSAGE_FILE = "kpi/ves_message.json";
    private static final String KPI_CONFIG_RATIO_FILE = "kpi/kpi_config_ratio.json";
    private static final String KPI_CONFIG_SUMRATIO_FILE = "kpi/kpi_config_sumratio.json";
    private static final String VES_MESSAGE_EMPTY_FILE = "kpi/ves_message_empty.json";
    private static final String VES_MESSAGE_NULL_FILE = "kpi/ves_message_null.json";
    private static final String VES_MESSAGE_EVENTNAME_FILE = "kpi/ves_message_eventname.json";

    @Test
    public void testKpiComputation() {


        String strKpiConfig = FileUtils.getFileContents(KPI_CONFIG_FILE);

        String vesMessage = FileUtils.getFileContents(VES_MESSAGE_FILE);

        Configuration config = mock(Configuration.class);
        when(config.getKpiConfig()).thenReturn(strKpiConfig);
        List<VesEvent> vesList = new KpiComputation().checkAndDoComputation(vesMessage, config);

        VesEvent vesEvent = vesList.get(0);
        assertEquals(vesEvent.getEvent().getPerf3gppFields().getMeasDataCollection().getMeasInfoList().get(0)
                .getMeasValuesList().get(0).getMeasResults().get(0).getSvalue(), "40");
    }

    @Test
    public void testKpiComputationRatio() {
        String strKpiConfigRatio = FileUtils.getFileContents(KPI_CONFIG_RATIO_FILE);
        String vesMessage = FileUtils.getFileContents(VES_MESSAGE_FILE);
        Configuration config = mock(Configuration.class);
        when(config.getKpiConfig()).thenReturn(strKpiConfigRatio);
        List<VesEvent> vesList = new KpiComputation().checkAndDoComputation(vesMessage, config);
        VesEvent vesEvent = vesList.get(0);
        assertEquals(vesEvent.getEvent().getPerf3gppFields().getMeasDataCollection().getMeasInfoList().get(0)
                 .getMeasValuesList().get(0).getMeasResults().get(0).getSvalue(), "50");
    }

    @Test
    public void testKpiComputationSumRatio() {

        String strKpiConfigSumRatio  = FileUtils.getFileContents(KPI_CONFIG_SUMRATIO_FILE);
        String vesMessage = FileUtils.getFileContents(VES_MESSAGE_FILE);
        Configuration config = mock(Configuration.class);
        when(config.getKpiConfig()).thenReturn(strKpiConfigSumRatio);
        List<VesEvent> vesList = new KpiComputation().checkAndDoComputation(vesMessage, config);
        VesEvent vesEvent = vesList.get(0);
        assertEquals(vesEvent.getEvent().getPerf3gppFields().getMeasDataCollection().getMeasInfoList().get(0)
                .getMeasValuesList().get(0).getMeasResults().get(0).getSvalue(), "67");
    }

    @Test
    public void testKpiComputationSumRatioEmptyCheck() {
        String strKpiConfigSumRatio = FileUtils.getFileContents(KPI_CONFIG_SUMRATIO_FILE);

        String vesMessage = FileUtils.getFileContents(VES_MESSAGE_EMPTY_FILE);
        Configuration config = mock(Configuration.class);
        when(config.getKpiConfig()).thenReturn(strKpiConfigSumRatio);
        List<VesEvent> vesList = new KpiComputation().checkAndDoComputation(vesMessage, config);
        assertEquals(0, vesList.size());
    }

    @Test
    public void testKpiComputationSumRatioOperandsCheck() {
        String strKpiConfigSumRatio = FileUtils.getFileContents(KPI_CONFIG_SUMRATIO_FILE);

        String vesMessage = FileUtils.getFileContents(VES_MESSAGE_NULL_FILE);
        Configuration config = mock(Configuration.class);
        when(config.getKpiConfig()).thenReturn(strKpiConfigSumRatio);
        List<VesEvent> vesList = new KpiComputation().checkAndDoComputation(vesMessage, config);
        assertEquals(0, vesList.size());
    }

    @Test
    public void testKpiComputationSumRatioEventNameCheck() {
        String strKpiConfigSumRatio = FileUtils.getFileContents(KPI_CONFIG_SUMRATIO_FILE);

        String vesMessage = FileUtils.getFileContents(VES_MESSAGE_EVENTNAME_FILE);
        Configuration config = mock(Configuration.class);
        when(config.getKpiConfig()).thenReturn(strKpiConfigSumRatio);
        List<VesEvent> vesList = new KpiComputation().checkAndDoComputation(vesMessage, config);
        assertEquals(null, vesList);
    }

}
