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
import java.math.RoundingMode;
import java.util.*;

import org.onap.dcaegen2.kpi.config.ControlLoopSchemaType;
import org.onap.dcaegen2.kpi.models.*;
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
    public List<VesEvent> handle(PerformanceEvent pmEvent, ControlLoopSchemaType schemaType,
            Map<String, List<KpiOperand>> measInfoMap, String measType, List<String> operands) {

	final List<VesEvent> vesEvents = new LinkedList<>();

	if(operands.size() == 2){
        List<KpiOperand> k1 = measInfoMap.get(operands.get(0));
        List<KpiOperand> k2 = measInfoMap.get(operands.get(1));
	

        if (k1.size() != k2.size() ) {
            return null;
        }

        ListIterator<KpiOperand> listIteratorK1 = k1.listIterator();
        ListIterator<KpiOperand> listIteratorK2 = k2.listIterator();

        while (listIteratorK1.hasNext()) {
            final KpiOperand myK1 = listIteratorK1.next();
            final KpiOperand myK2 = listIteratorK2.next();
	  
	  if(myK2.getValue().compareTo(BigDecimal.ZERO) != 0){
             final BigDecimal result =  myK1.getValue().multiply(new BigDecimal("100"))
                    .divide(myK2.getValue(), 0, RoundingMode.HALF_UP);
	    
		vesEvents.add(generateVesEvent(pmEvent,  schemaType.toString(),result, measType));
 	} 
	} 

        } 
	   
        return vesEvents;
    }

}
