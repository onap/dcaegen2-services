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
 * Model class for the config data
 */
public class ConfigData {

    private String maxNumberofConns;
    private String predictedMaxNumberofConns;
    private String lastUpdatedTS;

    public String getMaxNumberofConns() {
        return maxNumberofConns;
    }

    public void setMaxNumberofConns(String maxNumberofConns) {
        this.maxNumberofConns = maxNumberofConns;
    }

    public String getPredictedMaxNumberofConns() {
        return predictedMaxNumberofConns;
    }

    public void setPredictedMaxNumberofConns(String predictedMaxNumberofConns) {
        this.predictedMaxNumberofConns = predictedMaxNumberofConns;
    }

    public String getLastUpdatedTS() {
        return lastUpdatedTS;
    }

    public void setLastUpdatedTS(String lastUpdatedTS) {
        this.lastUpdatedTS = lastUpdatedTS;
    }

	/**
	 * Returns a hashcode value for the object
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lastUpdatedTS == null) ? 0 : lastUpdatedTS.hashCode());
		result = prime * result + ((maxNumberofConns == null) ? 0 : maxNumberofConns.hashCode());
		result = prime * result + ((predictedMaxNumberofConns == null) ? 0 : predictedMaxNumberofConns.hashCode());
		return result;
	}

	/**
	 * Checks whether the object matches with ConfigData
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigData other = (ConfigData) obj;
		if (lastUpdatedTS == null) {
			if (other.lastUpdatedTS != null)
				return false;
		} else if (!lastUpdatedTS.equals(other.lastUpdatedTS))
			return false;
		if (maxNumberofConns == null) {
			if (other.maxNumberofConns != null)
				return false;
		} else if (!maxNumberofConns.equals(other.maxNumberofConns))
			return false;
		if (predictedMaxNumberofConns == null) {
			if (other.predictedMaxNumberofConns != null)
				return false;
		} else if (!predictedMaxNumberofConns.equals(other.predictedMaxNumberofConns))
			return false;
		return true;
	}
    
}
