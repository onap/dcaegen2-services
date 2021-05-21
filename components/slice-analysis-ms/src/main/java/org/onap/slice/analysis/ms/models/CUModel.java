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

import java.util.List;

/**
 * Model class for CU model
 */
public class CUModel {

	private String gNBCUName;
    private String nearRTRICId;
    private List<CellCUList> cellCUList;

    public String getgNBCUName() {
		return gNBCUName;
	}

	public void setgNBCUName(String gNBCUName) {
		this.gNBCUName = gNBCUName;
	}

	public String getNearRTRICId() {
		return nearRTRICId;
	}

	public void setNearRTRICId(String nearRTRICId) {
		this.nearRTRICId = nearRTRICId;
	}

	public List<CellCUList> getCellCUList() {
        return cellCUList;
    }

    public void setCellCUList(List<CellCUList> cellCUList) {
        this.cellCUList = cellCUList;
    }
    
    @Override
	public String toString() {
		return "CUModel [gNBCUName=" + gNBCUName + ", nearRTRICId=" + nearRTRICId + ", cellCUList=" + cellCUList + "]";
	}

	/**
	 * Returns a hashcode value for the object
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cellCUList == null) ? 0 : cellCUList.hashCode());
		result = prime * result + ((gNBCUName == null) ? 0 : gNBCUName.hashCode());
		result = prime * result + ((nearRTRICId == null) ? 0 : nearRTRICId.hashCode());
		return result;
	}

	/**
	 * Checks whether the object matches with the CUModel
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CUModel other = (CUModel) obj;
		if (cellCUList == null) {
			if (other.cellCUList != null)
				return false;
		} else if (!cellCUList.equals(other.cellCUList))
			return false;
		if (gNBCUName == null) {
			if (other.gNBCUName != null)
				return false;
		} else if (!gNBCUName.equals(other.gNBCUName))
			return false;
		if (nearRTRICId == null) {
			if (other.nearRTRICId != null)
				return false;
		} else if (!nearRTRICId.equals(other.nearRTRICId))
			return false;
		return true;
	}

}
