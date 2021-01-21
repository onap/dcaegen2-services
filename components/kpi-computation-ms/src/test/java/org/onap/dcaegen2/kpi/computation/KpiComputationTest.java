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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.onap.dcaegen2.kpi.computation.KpiComputation;
import org.onap.dcaegen2.kpi.models.Configuration;
import org.onap.dcaegen2.kpi.models.VesEvent;

public class KpiComputationTest {

    private static final String KPI_CONFIG_FILE = "kpi/kpi_config.json";
    private static final String VES_MESSAGE_FILE = "kpi/ves_message.json";

    @Test
    public void testKpiComputation() {

//        String vesMessage = "{\r\n"
//                + "    \"event\":{\r\n"
//                + "        \"commonEventHeader\":{\r\n"
//                + "            \"domain\":\"perf3gpp\",\r\n"
//                + "            \"eventId\":\"14618daf-c0ce-4ccc-9169-9e1ac79971f2\",\r\n"
//                + "            \"sequence\":0,\r\n"
//                + "            \"eventName\":\"perf3gpp_AcmeNode-Acme_pmMeasResult\",\r\n"
//                + "            \"sourceName\":\"oteNB5309\",\r\n"
//                + "            \"reportingEntityName\":\"\",\r\n"
//                + "            \"priority\":\"Normal\",\r\n"
//                + "            \"startEpochMicrosec\":1591099200000,\r\n"
//                + "            \"lastEpochMicrosec\":1591100100000,\r\n"
//                + "            \"version\":\"4.0\",\r\n"
//                + "            \"vesEventListenerVersion\":\"7.1\",\r\n"
//                + "            \"timeZoneOffset\":\"UTC+05:00\"\r\n"
//                + "        },\r\n"
//                + "        \"perf3gppFields\":{\r\n"
//                + "            \"perf3gppFieldsVersion\":\"1.0\",\r\n"
//                + "            \"measDataCollection\":{\r\n"
//                + "                \"granularityPeriod\":1591100100000,\r\n"
//                + "                \"measuredEntityUserName\":\"\",\r\n"
//                + "                \"measuredEntityDn\":\"UPFMeasurement\",\r\n"
//                + "                \"measuredEntitySoftwareVersion\":\"r0.1\",\r\n"
//                + "                \"measInfoList\":[\r\n"
//                + "                    {\r\n"
//                + "                        \"measInfoId\":{\r\n"
//                + "                            \"sMeasInfoId\":\"UPFFunction0\"\r\n"
//                + "                        },\r\n"
//                + "                        \"measTypes\":{\r\n"
//                + "                            \"sMeasTypesList\":[\r\n"
//                + "                                \"GTP.InDataOctN3UPF.08_010101\",\r\n"
//                + "                                \"GTP.OutDataOctN3UPF.08_010101\"\r\n"
//                + "                            ]\r\n"
//                + "                        },\r\n"
//                + "                        \"measValuesList\":[\r\n"
//                + "                            {\r\n"
//                + "                                \"measObjInstId\":\"some measObjLdn\",\r\n"
//                + "                                \"suspectFlag\":\"false\",\r\n"
//                + "                                \"measResults\":[\r\n"
//                + "                                    {\r\n"
//                + "                                        \"p\":1,\r\n"
//                + "                                        \"sValue\":\"10\"\r\n"
//                + "                                    },\r\n"
//                + "                                    {\r\n"
//                + "                                        \"p\":2,\r\n"
//                + "                                        \"sValue\":\"20\"\r\n"
//                + "                                    }\r\n"
//                + "                                ]\r\n"
//                + "                            }\r\n"
//                + "                        ]\r\n"
//                + "                    },\r\n"
//                + "                    {\r\n"
//                + "                        \"measInfoId\":{\r\n"
//                + "                            \"sMeasInfoId\":\"UPFFunction1\"\r\n"
//                + "                        },\r\n"
//                + "                        \"measTypes\":{\r\n"
//                + "                            \"sMeasTypesList\":[\r\n"
//                + "                                \"GTP.InDataOctN3UPF.08_010101\",\r\n"
//                + "                                \"GTP.OutDataOctN3UPF.08_010101\"\r\n"
//                + "                            ]\r\n"
//                + "                        },\r\n"
//                + "                        \"measValuesList\":[\r\n"
//                + "                            {\r\n"
//                + "                                \"measObjInstId\":\"some measObjLdn\",\r\n"
//                + "                                \"suspectFlag\":\"false\",\r\n"
//                + "                                \"measResults\":[\r\n"
//                + "                                    {\r\n"
//                + "                                        \"p\":1,\r\n"
//                + "                                        \"sValue\":\"30\"\r\n"
//                + "                                    },\r\n"
//                + "                                    {\r\n"
//                + "                                        \"p\":2,\r\n"
//                + "                                        \"sValue\":\"40\"\r\n"
//                + "                                    }\r\n"
//                + "                                ]\r\n"
//                + "                            }\r\n"
//                + "                        ]\r\n"
//                + "                    }\r\n"
//                + "                ]\r\n"
//                + "            }\r\n"
//                + "        }\r\n"
//                + "    }\r\n"
//                + "}";
//
//        String strKpiConfig = "{\r\n"
//                + "    \"domain\": \"measurementsForKpi\",\r\n"
//                + "    \"methodForKpi\": [{\r\n"
//                + "            \"eventName\": \"perf3gpp_CORE_AMF_pmMeasResult\",\r\n"
//                + "            \"controlLoopSchemaType\": \"SLICE\",\r\n"
//                + "            \"policyScope\": \"resource=networkSlice;type=configuration\",\r\n"
//                + "            \"policyName\": \"configuration.dcae.microservice.pm-mapper.xml\",\r\n"
//                + "            \"policyVersion\": \"v0.0.1\",\r\n"
//                + "            \"kpis\": [{\r\n"
//                + "                \"measType\": \"AMFRegNbr\",\r\n"
//                + "                \"operation\": \"SUM\",\r\n"
//                + "                \"operands\": \"RM.RegisteredSubNbrMean\"\r\n"
//                + "            }]\r\n"
//                + "        },\r\n"
//                + "        {\r\n"
//                + "            \"eventName\": \"perf3gpp_AcmeNode-Acme_pmMeasResult\",\r\n"
//                + "            \"controlLoopSchemaType\": \"SLICE\",\r\n"
//                + "            \"policyScope\": \"resource=networkSlice;type=configuration\",\r\n"
//                + "            \"policyName\": \"configuration.dcae.microservice.pm-mapper.xml\",\r\n"
//                + "            \"policyVersion\": \"v0.0.1\",\r\n"
//                + "            \"kpis\": [{\r\n"
//                + "                    \"measType\": \"UpstreamThr\",\r\n"
//                + "                    \"operation\": \"SUM\",\r\n"
//                + "                    \"operands\": \"GTP.InDataOctN3UPF\"\r\n"
//                + "\r\n"
//                + "                },\r\n"
//                + "                {\r\n"
//                + "                    \"measType\": \"DownstreamThr\",\r\n"
//                + "                    \"operation\": \"SUM\",\r\n"
//                + "                    \"operands\": \"GTP.OutDataOctN3UPF\"\r\n"
//                + "                }\r\n"
//                + "            ]\r\n"
//                + "        }\r\n"
//                + "    ]\r\n"
//                + "}";

        String strKpiConfig = FileUtils.getFileContents(KPI_CONFIG_FILE);

        String vesMessage = FileUtils.getFileContents(VES_MESSAGE_FILE);

        Configuration config = mock(Configuration.class);
        when(config.getKpiConfig()).thenReturn(strKpiConfig);
        List<VesEvent> vesList = new KpiComputation().checkAndDoComputation(vesMessage, config);

        VesEvent vesEvent = vesList.get(0);
        assertEquals(vesEvent.getEvent().getPerf3gppFields().getMeasDataCollection().getMeasInfoList().get(0)
                .getMeasValuesList().get(0).getMeasResults().get(0).getSvalue(), "40");
    }

}
