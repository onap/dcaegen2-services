/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
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
package org.onap.slice.analysis.ms.models.ccvpnnotification;

import javax.annotation.Generated;

/**
 * Model class for Ves ccvpnNotification.arrayOfNamedHashMap.hashMap
 */
@Generated("jsonschema2pojo")
public class HashMap {

    private String cllId;
    private String uniId;
    private String bandwidthValue;
    private String time;

    public String getCllId() {
        return cllId;
    }

    public void setCllId(String cllId) {
        this.cllId = cllId;
    }

    public String getUniId() {
        return uniId;
    }

    public void setUniId(String uniId) {
        this.uniId = uniId;
    }

    public String getBandwidthValue() {
        return bandwidthValue;
    }

    public void setBandwidthValue(String bandwidthValue) {
        this.bandwidthValue = bandwidthValue;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
