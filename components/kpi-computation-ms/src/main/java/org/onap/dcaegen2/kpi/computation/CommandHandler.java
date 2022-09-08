/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021 China Mobile.
 *  Copyright (C) 2021 Deutsche Telekom AG. All rights reserved.
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
import java.util.List;
import java.util.Map;

import org.onap.dcaegen2.kpi.config.ControlLoopSchemaType;
import org.onap.dcaegen2.kpi.models.KpiOperand;
import org.onap.dcaegen2.kpi.models.PerformanceEvent;
import org.onap.dcaegen2.kpi.models.VesEvent;

/**
 * Get special methods to do computation.
 *
 * @author Kai Lu
 * @author Tarun Agrawal
 *
 */
public class CommandHandler {

    /**
     * The method to handle data.
     *
     * @param className   class name
     * @param pmEvent     pmEvent
     * @param schemaType  schemaType
     * @param measInfoMap measInfoMap
     * @param measType    measType
     * @param operands    operands list of measurements
     * @return VesEvent VesEvent
     */
    public static List<VesEvent> handle(String className, PerformanceEvent pmEvent, ControlLoopSchemaType schemaType,
            Map<String, List<KpiOperand>> measInfoMap, String measType, List<String> operands) {

        try {
            // Load Command Object
            Command command = (Command) Class.forName(className).getDeclaredConstructor().newInstance();
            return command.handle(pmEvent, schemaType, measInfoMap, measType, operands);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
