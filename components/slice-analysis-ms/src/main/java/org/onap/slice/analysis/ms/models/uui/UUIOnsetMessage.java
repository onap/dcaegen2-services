/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2023 Huawei Technologies Co., Ltd. All rights reserved.
 *   ==============================================================================
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     ============LICENSE_END=========================================================
 *
 *******************************************************************************/

package org.onap.slice.analysis.ms.models.uui;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * Model class for the OnsetMessage Object sent to UUI
 */
public class UUIOnsetMessage {

    // Source is used to describe which model sent the onset message
    @Getter
    @Setter
    private String source;

    // Time for the onset message
    @Getter
    @Setter
    private Date timestamp;

    // Entity includes id, operation, result, and reason, which are the details for uui onset message.
    @Getter
    @Setter
    private UUIEntity entity;

}
