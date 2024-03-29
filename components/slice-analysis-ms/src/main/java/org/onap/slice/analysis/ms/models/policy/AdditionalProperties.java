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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Model class for the AdditionalProperties Object
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdditionalProperties<T> {
    private String modifyAction;
    private List<String> snssaiList;
    private String sliceProfileId;
    private T resourceConfig;
    private Map<String, String> nsiInfo;
    private String scriptName;
    // Extra attributes for CCVPN CloseLoop
    private String enableSdnc;
    private List<TransportNetwork> transportNetworks;

    public String getModifyAction() {
        return modifyAction;
    }
    public void setModifyAction(String modifyAction) {
        this.modifyAction = modifyAction;
    }
    public List<String> getSnssaiList() {
        return snssaiList;
    }
    public void setSnssaiList(List<String> snssaiList) {
        this.snssaiList = snssaiList;
    }
    public String getSliceProfileId() {
        return sliceProfileId;
    }
    public void setSliceProfileId(String sliceProfileId) {
        this.sliceProfileId = sliceProfileId;
    }
    public T getResourceConfig() {
        return resourceConfig;
    }
    public void setResourceConfig(T resourceConfig) {
        this.resourceConfig = resourceConfig;
    }
    public Map<String, String> getNsiInfo() {
        return nsiInfo;
    }
    public void setNsiInfo(Map<String, String> nsiInfo) {
        this.nsiInfo = nsiInfo;
    }
    public String getScriptName() {
        return scriptName;
    }
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }
    // Extra attributes setter/getter for CCVPN CloseLoop
    public String getEnableSdnc() {
        return enableSdnc;
    }

    public void setEnableSdnc(String enableSdnc) {
        this.enableSdnc = enableSdnc;
    }

    public List<TransportNetwork> getTransportNetworks() {
        return transportNetworks;
    }

    public void setTransportNetworks(List<TransportNetwork> transportNetworks) {
        this.transportNetworks = transportNetworks;
    }
}
