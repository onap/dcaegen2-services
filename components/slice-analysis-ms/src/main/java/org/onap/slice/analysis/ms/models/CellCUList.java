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

/**
 * Model class for cell information
 */
public class CellCUList {

    private Integer cellLocalId;
    private ConfigData configData;

    /**
     * Returns a hashcode value for the object
     */
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cellLocalId == null) ? 0 : cellLocalId.hashCode());
		result = prime * result + ((configData == null) ? 0 : configData.hashCode());
		return result;
	}

	/**
	 * Checks whether the object matches with the CellCUList
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellCUList other = (CellCUList) obj;
		if (cellLocalId == null) {
			if (other.cellLocalId != null)
				return false;
		} else if (!cellLocalId.equals(other.cellLocalId))
			return false;
		if (configData == null) {
			if (other.configData != null)
				return false;
		} else if (!configData.equals(other.configData))
			return false;
		return true;
	}

	public Integer getCellLocalId() {
        return cellLocalId;
    }

    public void setCellLocalId(Integer cellLocalId) {
        this.cellLocalId = cellLocalId;
    }

    public ConfigData getConfigData() {
        return configData;
    }

    public void setConfigData(ConfigData configData) {
        this.configData = configData;
    }
    
    
}
