/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
 *   Copyright (C) 2022 Huawei Canada Limited.
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

package org.onap.slice.analysis.ms.models.policy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class for the AAI Object 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AAI {
    @JsonProperty("vserver.is-closed-loop-disabled")
    private String vserverIsClosedLoopDisabled;
    @JsonProperty("vserver.prov-status")
    private String vserverProvStatus;
    @JsonProperty("generic-vnf.vnf-id")
    private String genericVnfVNFId;
    @JsonProperty("generic-vnf.is-closed-loop-disabled")
    private String genericVnfIsClosedLoopDisabled;
    @JsonProperty("generic-vnf.prov-status")
    private String genericVnfProvStatus;
    @JsonProperty("generic-vnf.vnf-name")
    private String genericVnfVnfName;

    public String getVserverIsClosedLoopDisabled() {
        return vserverIsClosedLoopDisabled;
    }

    public void setVserverIsClosedLoopDisabled(String vserverIsClosedLoopDisabled) {
        this.vserverIsClosedLoopDisabled = vserverIsClosedLoopDisabled;
    }

    public String getVserverProvStatus() {
        return vserverProvStatus;
    }

    public void setVserverProvStatus(String vserverProvStatus) {
        this.vserverProvStatus = vserverProvStatus;
    }

    public String getGenericVnfVNFId() {
        return genericVnfVNFId;
    }

    public void setGenericVnfVNFId(String genericVnfVNFId) {
        this.genericVnfVNFId = genericVnfVNFId;
    }

    public String getGenericVnfProvStatus() {
        return genericVnfProvStatus;
    }

    public void setGenericVnfProvStatus(String genericVnfProvStatus) {
        this.genericVnfProvStatus = genericVnfProvStatus;
    }

    public String getGenericVnfIsClosedLoopDisabled() {
        return genericVnfIsClosedLoopDisabled;
    }

    public void setGenericVnfIsClosedLoopDisabled(String genericVnfIsClosedLoopDisabled) {
        this.genericVnfIsClosedLoopDisabled = genericVnfIsClosedLoopDisabled;
    }

    public String getGenericVnfVnfName() {
        return genericVnfVnfName;
    }

    public void setGenericVnfVnfName(String genericVnfVnfName) {
        this.genericVnfVnfName = genericVnfVnfName;
    }
}
