/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 China Mobile.
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

package org.onap.dcaegen2.kpi.config;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * KPI Formula.
 *
 * @author Kai Lu
 * @author Tarun Agrawal
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Kpi extends BaseModule {

    private static final long serialVersionUID = 1L;

    /**
     * measType.
     */
    @SerializedName("measType")
    private String measType;

    /**
     * operation.
     */
    private Operation operation;

    /**
     * operands.
     */
    private List<String> operands;

    /**
     * condition.
     *
     */
    private String condition;

}
