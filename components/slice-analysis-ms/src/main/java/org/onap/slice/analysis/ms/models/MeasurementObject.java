/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020-2021 Wipro Limited.
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

package org.onap.slice.analysis.ms.models;

import java.util.List;
import java.util.Map;

/**
 * Model class for the Measurement Object
 */
public class MeasurementObject {
    private String measurementObjectId;
    private Map<String, Integer> pmData;

    /**
     * Returns the index of the MeasurementObject
     */
    public static int findIndex(String measurementObjectId, List<MeasurementObject> list) {
        int index = -1;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (measurementObjectId.equals(list.get(i).getMeasurementObjectId())) {
                index = i;
            }
        }
        return index;
    }

    public String getMeasurementObjectId() {
        return measurementObjectId;
    }

    public void setMeasurementObjectId(String measurementObjectId) {
        this.measurementObjectId = measurementObjectId;
    }

    public Map<String, Integer> getPmData() {
        return pmData;
    }

    public void setPmData(Map<String, Integer> pmData) {
        this.pmData = pmData;
    }

    public MeasurementObject(String measurementObjectId, Map<String, Integer> pmData) {
        super();
        this.measurementObjectId = measurementObjectId;
        this.pmData = pmData;
    }

    public MeasurementObject(String measurementObjectId) {
        super();
        this.measurementObjectId = measurementObjectId;
    }

    public MeasurementObject() {

    }

    /**
     * Returns a hachcode value for the object
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((measurementObjectId == null) ? 0 : measurementObjectId.hashCode());
        result = prime * result + ((pmData == null) ? 0 : pmData.hashCode());
        return result;
    }

    /**
     * Checks whether the object matches with the MeasurementObject
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MeasurementObject other = (MeasurementObject) obj;
        if (measurementObjectId == null) {
            if (other.measurementObjectId != null)
                return false;
        } else if (!measurementObjectId.equals(other.measurementObjectId))
            return false;
        if (pmData == null) {
            if (other.pmData != null)
                return false;
        } else if (!pmData.equals(other.pmData))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MeasurementObject [measurementObjectId=" + measurementObjectId + ", pmData=" + pmData + "]";
    }

}
