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

public class MeasInfoList {

    private MeasInfoId measInfoId;
    private MeasTypes measTypes;
    private List<MeasValuesList> measValuesList = null;

    public MeasInfoId getMeasInfoId() {
        return measInfoId;
    }

    public void setMeasInfoId(MeasInfoId measInfoId) {
        this.measInfoId = measInfoId;
    }

    public MeasTypes getMeasTypes() {
        return measTypes;
    }

    public void setMeasTypes(MeasTypes measTypes) {
        this.measTypes = measTypes;
    }

    public List<MeasValuesList> getMeasValuesList() {
        return measValuesList;
    }

    public void setMeasValuesList(List<MeasValuesList> measValuesList) {
        this.measValuesList = measValuesList;
    }

}
