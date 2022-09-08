/*
 * ================================================================================
 * Copyright (C) 2021 Deutsche Telekom AG. All rights reserved.
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
 * ============LICENSE_END=========================================================
 *
 */

package org.onap.dcaegen2.kpi.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Operand Values.
 *
 * @author curated code
 *
 */
@Data
@AllArgsConstructor
public class KpiOperand {

    /**
     * measValuesList[0].measObjInstId
     */
    private String measObjInstId;

    /**
     * measResult.svalue
     */
    private BigDecimal value;

}

