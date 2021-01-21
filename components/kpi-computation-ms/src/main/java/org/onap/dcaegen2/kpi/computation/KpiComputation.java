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

package org.onap.dcaegen2.kpi.computation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.onap.dcaegen2.kpi.config.ControlLoopSchemaType;
import org.onap.dcaegen2.kpi.config.Kpi;
import org.onap.dcaegen2.kpi.config.KpiConfig;
import org.onap.dcaegen2.kpi.config.KpiJsonConversion;
import org.onap.dcaegen2.kpi.config.MethodForKpi;
import org.onap.dcaegen2.kpi.config.Operation;
import org.onap.dcaegen2.kpi.exception.KpiComputationException;
import org.onap.dcaegen2.kpi.models.CommonEventHeader;
import org.onap.dcaegen2.kpi.models.Configuration;
import org.onap.dcaegen2.kpi.models.MeasDataCollection;
import org.onap.dcaegen2.kpi.models.MeasInfo;
import org.onap.dcaegen2.kpi.models.MeasResult;
import org.onap.dcaegen2.kpi.models.MeasValues;
import org.onap.dcaegen2.kpi.models.Perf3gppFields;
import org.onap.dcaegen2.kpi.models.PerformanceEvent;
import org.onap.dcaegen2.kpi.models.VesEvent;
import org.onap.dcaegen2.kpi.utils.VesJsonConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KPI computation.
 *
 * @author Kai Lu
 */
public class KpiComputation {

    private static Logger logger = LoggerFactory.getLogger(KpiComputation.class);

    /**
     * do KPI computation.
     *
     * @param ves    ves
     * @param config config
     * @return Kpi ves list
     *
     */
    public List<VesEvent> checkAndDoComputation(String ves, Configuration config) {

        if (ves == null || ves.equalsIgnoreCase("{}")) {
            return null;
        }

        KpiConfig kpiConfig = KpiJsonConversion.convertKpiConfig(config.getKpiConfig());
        if (kpiConfig == null) {
            logger.info("No kpi config.");
            return null;
        }

        logger.info("kpi config. {}", kpiConfig);
        VesEvent vesEvent = VesJsonConversion.convertVesEvent(ves);
        // Get event Name
        PerformanceEvent pmEvent = vesEvent.getEvent();
        String eventName = Optional.of(pmEvent).map(PerformanceEvent::getCommonEventHeader)
                .map(CommonEventHeader::getEventName)
                .orElseThrow(() -> new KpiComputationException("Required Field: EventName not present"));

        // Get Kpi's config per event name matching event name
        MethodForKpi methodForKpi = kpiConfig.getMethodForKpi().stream()
                .filter(m -> m.getEventName().equalsIgnoreCase(eventName)).findFirst().orElse(null);
        // if ves event not exist
        if (methodForKpi == null) {
            logger.info("No event name matched.");
            return null;
        }

        MeasDataCollection measDataCollection = Optional.of(pmEvent).map(PerformanceEvent::getPerf3gppFields)
                .map(Perf3gppFields::getMeasDataCollection)
                .orElseThrow(() -> new KpiComputationException("Required Field: MeasData not present"));
        // Do computation for each KPI
        List<VesEvent> events = new ArrayList<>();
        List<Kpi> kpis = methodForKpi.getKpis();
        kpis.forEach(k -> {
            Map<String, List<BigDecimal>> measInfoMap = getOperands(measDataCollection, k.getOperands());
            if (measInfoMap == null) {
                logger.info("No kpi need to do computation for {}", k.getOperands());
                return;
            }

            ControlLoopSchemaType schemaType = methodForKpi.getControlLoopSchemaType();
            String measType = k.getMeasType();
            Operation operation = k.getOperation();

            VesEvent kpiVesEvent = CommandHandler.handle(operation.value, pmEvent, schemaType, measInfoMap, measType);
            events.add(kpiVesEvent);
        });
        return events;
    }

    private Map<String, List<BigDecimal>> getOperands(MeasDataCollection measDataCollection, String operands) {
        List<BigDecimal> kpiOperands = new ArrayList<>();
        List<MeasInfo> measInfoList = measDataCollection.getMeasInfoList();
        String[] key = new String[1];
        measInfoList.forEach(m -> {
            List<String> measTypesList = m.getMeasTypes().getMeasTypesList();
            String measValue = measTypesList.stream()
                    .filter(s -> StringUtils.substring(s, 0, operands.length()).equalsIgnoreCase(operands)).findFirst()
                    .orElse(null);
            if (measValue != null) {
                key[0] = measValue.substring(operands.length() + 1);
                int index = measTypesList.indexOf(measValue);
                MeasValues measValues = m.getMeasValuesList().stream().findFirst().orElse(null);
                List<MeasResult> measResults = measValues.getMeasResults();
                kpiOperands.add(new BigDecimal(measResults.get(index).getSvalue()));
            }
        });
        if (kpiOperands.size() <= 0) {
            logger.info("No measureValues matched");
            return null;
        }
        Map<String, List<BigDecimal>> measInfoMap = new HashMap<>();
        measInfoMap.put(key[0], kpiOperands);
        logger.info("kpi operate: {}", kpiOperands);
        return measInfoMap;
    }
}
