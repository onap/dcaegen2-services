/*-
* ============LICENSE_START=======================================================
* Copyright (C) 2022 Deutsche Telekom AG. All rights reserved.
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
* SumRatioKpiComputation.
*
* @author Tarun Agrawal
*/
public class SumRatioKpiComputation extends BaseKpiComputation {

    private static Logger logger = LoggerFactory.getLogger(SumRatioKpiComputation.class);

    @Override
    public List<VesEvent> handle(PerformanceEvent pmEvent, ControlLoopSchemaType schemaType,
            Map<String, List<KpiOperand>> measInfoMap, String measType, List<String> operands) {

        BigDecimal sumK1;
        BigDecimal sumK2;
        final List<VesEvent> vesEvents = new LinkedList<>();

        if (operands.size() == 2) {
            List<KpiOperand> k1 = measInfoMap.get(operands.get(0));
            List<KpiOperand> k2 = measInfoMap.get(operands.get(1));

            if (k1.size() != k2.size()) {
                return null;
            }

            sumK1 = k1.stream().map(KpiOperand::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            sumK2 = k2.stream().map(KpiOperand::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);

           if (sumK2.compareTo(BigDecimal.ZERO) == 0) {
               return null;
           }

            BigDecimal result =  sumK1.multiply(new BigDecimal("100")).divide(sumK2, 0, RoundingMode.HALF_UP);
            vesEvents.add(generateVesEvent(pmEvent, schemaType.toString(), result, measType));
        }
	else {
	    throw new KpiComputationException("Insufficient number of operands to perform SumRatio computation");
	}
        return vesEvents;
    }
}
