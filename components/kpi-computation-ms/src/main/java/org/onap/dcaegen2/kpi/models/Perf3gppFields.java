/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 China Mobile.
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

package org.onap.dcaegen2.kpi.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.onap.dcaegen2.kpi.config.BaseModule;

/**
 * Perf3gppFields.
 *
 * @author Kai Lu
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Perf3gppFields extends BaseModule {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * perf3gppFieldsVersion.
     */
    private String perf3gppFieldsVersion;

    /**
     * measDataCollection.
     */
    private MeasDataCollection measDataCollection;

}
