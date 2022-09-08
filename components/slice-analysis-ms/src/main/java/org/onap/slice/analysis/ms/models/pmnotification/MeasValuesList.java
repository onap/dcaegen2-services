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

package org.onap.slice.analysis.ms.models.pmnotification;

import java.util.List;

/** 
 * Model class for the MeasValuesList Object 
 */
public class MeasValuesList {

	private String measObjInstId;
	private String suspectFlag;
	private List<MeasResult> measResults = null;

	public String getMeasObjInstId() {
		return measObjInstId;
	}

	public void setMeasObjInstId(String measObjInstId) {
		this.measObjInstId = measObjInstId;
	}

	public String getSuspectFlag() {
		return suspectFlag;
	}

	public void setSuspectFlag(String suspectFlag) {
		this.suspectFlag = suspectFlag;
	}

	public List<MeasResult> getMeasResults() {
		return measResults;
	}

	public void setMeasResults(List<MeasResult> measResults) {
		this.measResults = measResults;
	}

}
