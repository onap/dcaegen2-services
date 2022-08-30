/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021 Deutsche Telekom AG. All rights reserved.
 * Copyright (C) 2022 Wipro Limited. All rights reserved.
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.onap.dcaegen2.kpi.config.ControlLoopSchemaType;
import org.onap.dcaegen2.kpi.exception.KpiComputationException;
import org.onap.dcaegen2.kpi.models.CommonEventHeader;
import org.onap.dcaegen2.kpi.models.KpiOperand;
import org.onap.dcaegen2.kpi.models.MeasDataCollection;
import org.onap.dcaegen2.kpi.models.MeasInfo;
import org.onap.dcaegen2.kpi.models.MeasInfoId;
import org.onap.dcaegen2.kpi.models.MeasResult;
import org.onap.dcaegen2.kpi.models.MeasTypes;
import org.onap.dcaegen2.kpi.models.MeasValues;
import org.onap.dcaegen2.kpi.models.Perf3gppFields;
import org.onap.dcaegen2.kpi.models.PerformanceEvent;
import org.onap.dcaegen2.kpi.models.VesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RatioKpiComputation.
 *
 * @author Tarun Agrawal
 */
public class RatioKpiComputation extends BaseKpiComputation {

    private static Logger logger = LoggerFactory.getLogger(RatioKpiComputation.class);

    @Override
    public List <VesEvent> handle(PerformanceEvent pmEvent, ControlLoopSchemaType schemaType,
           Map <String,List <KpiOperand>> measInfoMap, String measType, List < String > operands) {

        final List < VesEvent > vesEvents = new LinkedList < > ();
        if (operands.size() == 2) {
            List <KpiOperand> k1 = measInfoMap.get(operands.get(0));
            List <KpiOperand> k2 = measInfoMap.get(operands.get(1));
            if (k1.size() != k2.size()) {
                return null;
            }
            ListIterator <KpiOperand> listIteratorK1 = k1.listIterator();
            ListIterator <KpiOperand> listIteratorK2 = k2.listIterator();
            while (listIteratorK1.hasNext()) {
                final KpiOperand myK1 = listIteratorK1.next();
                final KpiOperand myK2 = listIteratorK2.next();

                String value = myK1.getValue().toString();
                List<MeasInfo> measInfoList = Optional.of(pmEvent).map(PerformanceEvent::getPerf3gppFields)
                      .map(Perf3gppFields::getMeasDataCollection)
                      .map(MeasDataCollection::getMeasInfoList)
                      .orElseThrow(() -> new KpiComputationException("MeasInfoList not present"));

                int pValue = 0;

                for(MeasInfo meas: measInfoList){
                   for(MeasValues measValue: meas.getMeasValuesList()){
                      for(MeasResult measResult: measValue.getMeasResults()){
                          String s = measResult.getSvalue();
                          if(s.equalsIgnoreCase(value)){
                               pValue = measResult.getPvalue();
                          }
                      }
                   }
                }

                String operand = null;

                for(MeasInfo measInfo: measInfoList){
                   List<String> measTypesList = measInfo.getMeasTypes().getMeasTypesList();
                   if(!measTypesList.isEmpty()){
                      for(String s : measTypesList){
                         int index = measTypesList.indexOf(s);
                         if( index == (pValue-1)){
                            operand = s;
                         }
                      }
                   }
               }

                StringBuilder sb = new StringBuilder();
                if(!operand.isEmpty()){
                   char[] chars = operand.toCharArray();
                   for(char c : chars){
                      if(Character.isDigit(c)){
                         sb.append(c);
                      }
                   }
                }
                else{
                   logger.info("operand is empty");
                }

                String snssai = sb.toString();

                StringBuilder sb1 = new StringBuilder();
                if(!measType.isEmpty()){
                   char[] chars = measType.toCharArray();
                   for(char c : chars){
                      if(!Character.isDigit(c)){
                        sb1.append(c);
                      }
                   }
                }
                else{
                   logger.info("measType is empty");
                }

                String meas = sb1.toString();
                String measTypes = meas + snssai;

                if (myK2.getValue().compareTo(BigDecimal.ZERO) != 0) {
                    final BigDecimal result = myK1.getValue().multiply(new BigDecimal("100"))
                        .divide(myK2.getValue(), 0, RoundingMode.HALF_UP);
                    vesEvents.add(generateVesEvent(pmEvent, schemaType.toString(), result, measTypes));
                }
            }
        }
        else {
           throw new KpiComputationException("Insufficient number of operands to perform Ratio computation");
        }
        return vesEvents;
    }
}

