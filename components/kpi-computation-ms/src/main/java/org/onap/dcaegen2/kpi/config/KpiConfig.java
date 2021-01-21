/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 China Mobile.
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
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Kpi Config.
 *
 * @author Kai Lu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KpiConfig extends BaseModule {

    private static final long serialVersionUID = 1L;

    /**
     * Kpi domain which is associated with KPI incoming ves message domain.
     */
    private String domain;

    /**
     * Contains MethodForKpi metrics that needs to be applied to each Functional Role.
     */
    @SerializedName("methodForKpi")
    private List<MethodForKpi> methodForKpi;

}
