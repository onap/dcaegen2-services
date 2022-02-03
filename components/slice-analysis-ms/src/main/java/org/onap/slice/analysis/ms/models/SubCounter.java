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

package org.onap.slice.analysis.ms.models;

/**
 * Model class for the SubCounter Object which servers as key for PM data Queue
 */
public final class SubCounter {
    final String networkFunction;
    final String measuredObject;

    public SubCounter(String networkFunction, String measuredObject) {
        super();
        this.networkFunction = networkFunction;
        this.measuredObject = measuredObject;
    }

    public String getNetworkFunction() {
        return networkFunction;
    }

    public String getMeasuredObject() {
        return measuredObject;
    }

    /**
     * Returns a hashcode value for the object
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((networkFunction == null) ? 0 : networkFunction.hashCode());
        result = prime * result + ((measuredObject == null) ? 0 : measuredObject.hashCode());
        return result;
    }

    /**
     * Checks whether the object matches with the SubCounter
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SubCounter other = (SubCounter) obj;
        if (networkFunction == null) {
            if (other.networkFunction != null)
                return false;
        } else if (!networkFunction.equals(other.networkFunction))
            return false;
        if (measuredObject == null) {
            if (other.measuredObject != null)
                return false;
        } else if (!measuredObject.equals(other.measuredObject))
            return false;
        return true;
    }
}
