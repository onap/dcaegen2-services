/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 China Mobile.
 *  Copyright (C) 2021 Deutsche Telekom AG. All rights reserved.
 *  Copyright (C) 2022 Wipro Limited. All rights reserved.
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

import java.lang.String;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
import org.onap.dcaegen2.kpi.models.MeasTypes;
import org.onap.dcaegen2.kpi.models.MeasValues;
import org.onap.dcaegen2.kpi.models.Perf3gppFields;
import org.onap.dcaegen2.kpi.models.PerformanceEvent;
import org.onap.dcaegen2.kpi.models.VesEvent;
import org.onap.dcaegen2.kpi.models.KpiOperand;
import org.onap.dcaegen2.kpi.utils.VesJsonConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KPI computation.
 *
 * @author Kai Lu
 * @author Tarun Agrawal
 */
public class KpiComputation {

    private static Logger logger = LoggerFactory.getLogger(KpiComputation.class);
    String value = "";

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

        List<MeasInfo> measInfoList = Optional.of(pmEvent).map(PerformanceEvent::getPerf3gppFields)
                 .map(Perf3gppFields::getMeasDataCollection)
                 .map(MeasDataCollection::getMeasInfoList)
                 .orElseThrow(() -> new KpiComputationException("Required Field: MeasInfoList not present"));

  
        StringBuilder sb = new StringBuilder();
        for(MeasInfo measInfo: measInfoList){
           List<String> measTypes = measInfo.getMeasTypes().getMeasTypesList();
           if(!measTypes.isEmpty()){
              String anyString = measTypes.get(0);
              value = anyString.substring(anyString.lastIndexOf(".")+ 1);
              logger.info("The value string is {}",value); 
           }
           else{
              logger.info("MeasTypesList is empty");
           }
        }

        // Do computation for each KPI
        List<VesEvent> events = new LinkedList<>();
        List<Kpi> kpis = methodForKpi.getKpis();
        kpis.forEach(k -> {
            Map<String, List<KpiOperand>> measInfoMap = getOperands(measDataCollection, k.getOperands());
            if (measInfoMap == null) {
                logger.info("No kpi need to do computation for {}", k.getOperands());
                return;
            }

            ControlLoopSchemaType schemaType = methodForKpi.getControlLoopSchemaType();
            sb.append(k.getMeasType());
            sb.append(".");
            sb.append(value);
            String measType = sb.toString();
            logger.info("measType is {}", measType);
     
            Operation operation = k.getOperation();

            List<VesEvent> kpiVesEvent = CommandHandler.handle(operation.value, pmEvent, schemaType,
                                                  measInfoMap, measType, k.getOperands());
            if (kpiVesEvent != null && !kpiVesEvent.isEmpty()) {
                events.addAll(kpiVesEvent);
            }

        });

        return events;
    }

    private Map<String, List<KpiOperand>> getOperands(MeasDataCollection measDataCollection, List<String> operands) {
        Map<String, List<KpiOperand>> measInfoMap = new HashMap<>();
        List<MeasInfo> measInfoList = measDataCollection.getMeasInfoList();
        boolean flag;
        if (operands == null || operands.size() <= 0) {
            logger.info("No operands, no need to do computation ");
            return null;
        }

        // check all operands part of MeasInfo. else remove them from curated list.
        List<MeasInfo> curatedMeasInfoList = new ArrayList<>();
        for (MeasInfo measInfo : measInfoList) {
            flag = false;
            for (String operand : operands) {
                List<String> measTypesList = measInfo.getMeasTypes().getMeasTypesList();
                List<String> measValue = measTypesList.stream()
                        .filter(s -> StringUtils.substring(s, 0, operand.length()).equalsIgnoreCase(operand))
                        .collect(Collectors.toList());
                if (measValue == null) {
                    flag = true;
                }
            }
            if (!flag) {
                //add to new list
                curatedMeasInfoList.add(measInfo);
            }
        }

        for (String operand: operands) {
            String key = null;
            List<KpiOperand> kpiOperands = new ArrayList<>();
            for (MeasInfo m: curatedMeasInfoList) {
                List<String> measTypesList = m.getMeasTypes().getMeasTypesList();
                List<String> measValueList = measTypesList.stream()
                        .filter(s -> StringUtils.substring(s, 0, operand.length()).equalsIgnoreCase(operand))
                        .collect(Collectors.toList());
                if (measValueList != null) {
                  for(String measValue:measValueList){
                    key = new StringBuilder().append(operand).toString();
                    int index = measTypesList.indexOf(measValue);          
                    List<MeasValues> measValuesList = m.getMeasValuesList();
                    for ( MeasValues measValues : measValuesList) {
                         List<MeasResult> measResults = measValues.getMeasResults();
                         String measObjInstId = measValues.getMeasObjInstId();
                         MeasResult measResult = measResults.stream()
                                 .filter(v -> v.getPvalue() == (index + 1))
                                 .findFirst()
                                 .orElse(null);
                         if (measResult != null) {
                             KpiOperand newKpiOperand = new KpiOperand(measObjInstId, new BigDecimal(measResult.getSvalue()));
                             kpiOperands.add(newKpiOperand);
                         } else {
                             logger.info("measResults mis-matched - incorrect ves msg construction");
                         }
                    }
                  }
                }
            }
            if (kpiOperands.size() <= 0) {
                 logger.info("No measureValues matched");
                 return null;
            }
            if(key != null) {
                measInfoMap.put(key, kpiOperands);
                logger.info("kpi operate: {}", kpiOperands);
            } else {
              return null;
            }
        }
        return measInfoMap;
    }
}
