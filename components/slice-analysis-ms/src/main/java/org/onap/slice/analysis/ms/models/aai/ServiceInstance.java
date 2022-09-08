/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Wipro Limited.
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

package org.onap.slice.analysis.ms.models.aai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for receiving ServiceInstance data from AAI
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class ServiceInstance {

    @JsonProperty("service-instance-id")
    private String serviceInstanceId;
    @JsonProperty("service-instance-name")
    private String serviceInstanceName;
    @JsonProperty("service-type;")
    private String serviceType;
    @JsonProperty("service-role")
    private String serviceRole;
    @JsonProperty("environment-context")
    private String environmentContext;
    @JsonProperty("workload-context")
    private String workloadContext;
    @JsonProperty("orchestration-status")
    private String orchestrationStatus;
    @JsonProperty("relationship-list")
    private RelationshipList relationshipList;

}
