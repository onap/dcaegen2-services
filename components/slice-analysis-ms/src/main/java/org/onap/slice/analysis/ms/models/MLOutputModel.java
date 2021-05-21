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
 * Model class for ML output object
 */
public class MLOutputModel {
	private String snssai;
	private List<CUModel> data;
	public String getSnssai() {
		return snssai;
	}
	public void setSnssai(String snssai) {
		this.snssai = snssai;
	}
	public List<CUModel> getData() {
		return data;
	}
	public void setData(List<CUModel> data) {
		this.data = data;
	}
	
	@Override
	public String toString() {
		return "MLOutputModel [snssai=" + snssai + ", data=" + data + "]";
	}
	
	/**
	 * Returns a hashcode value for the object
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((snssai == null) ? 0 : snssai.hashCode());
		return result;
	}
	
	/**
	 * Checks whether the object matches with the MLOutputModel
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MLOutputModel other = (MLOutputModel) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (snssai == null) {
			if (other.snssai != null)
				return false;
		} else if (!snssai.equals(other.snssai))
			return false;
		return true;
	}
	
	
}
