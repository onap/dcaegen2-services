/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
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

package org.onap.slice.analysis.ms.models.pmnotification;

import java.util.List;

/**
 * Model class for the MeasDataCollection Object
 */
public class MeasDataCollection {

    private long granularityPeriod;
    private String measuredEntityUserName;
    private String measuredEntityDn;
    private String measuredEntitySoftwareVersion;
    private List<MeasInfoList> measInfoList;

    public long getGranularityPeriod() {
        return granularityPeriod;
    }

    public void setGranularityPeriod(long granularityPeriod) {
        this.granularityPeriod = granularityPeriod;
    }

    public String getMeasuredEntityUserName() {
        return measuredEntityUserName;
    }

    public void setMeasuredEntityUserName(String measuredEntityUserName) {
        this.measuredEntityUserName = measuredEntityUserName;
    }

    public String getMeasuredEntityDn() {
        return measuredEntityDn;
    }

    public void setMeasuredEntityDn(String measuredEntityDn) {
        this.measuredEntityDn = measuredEntityDn;
    }

    public String getMeasuredEntitySoftwareVersion() {
        return measuredEntitySoftwareVersion;
    }

    public void setMeasuredEntitySoftwareVersion(String measuredEntitySoftwareVersion) {
        this.measuredEntitySoftwareVersion = measuredEntitySoftwareVersion;
    }

    public List<MeasInfoList> getMeasInfoList() {
        return measInfoList;
    }

    public void setMeasInfoList(List<MeasInfoList> measInfoList) {
        this.measInfoList = measInfoList;
    }

}
