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

public enum Operation {

    SUM("org.onap.dcaegen2.kpi.computation.SumKpiComputation"),
    RATIO("org.onap.dcaegen2.kpi.computation.RatioKpiComputation"),
    MEAN("org.onap.dcaegen2.kpi.computation.MEAN"),
    SUMRATIO("org.onap.dcaegen2.kpi.computation.SumRatioKpiComputation");

    public final String value;

    private Operation(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
