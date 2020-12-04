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

package org.onap.slice.analysis.ms.models.policy;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 
 * Model class for the AAI Object 
 */
public class AAI {
	@JsonProperty("vserver.is-closed-loop-disabled")
	private String vserverIsClosedLoopDisabled;
	@JsonProperty("vserver.prov-status")
	private String vserverProvStatus;
	/*
	 * @JsonProperty("vserver.vserver-name") private String vserverVserverName;
	 */
	@JsonProperty("generic-vnf.vnf-id")
	private String vServerVNFId;

	public String getVserverIsClosedLoopDisabled() {
		return vserverIsClosedLoopDisabled;
	}

	public void setVserverIsClosedLoopDisabled(String vserverIsClosedLoopDisabled) {
		this.vserverIsClosedLoopDisabled = vserverIsClosedLoopDisabled;
	}

	public String getVserverProvStatus() {
		return vserverProvStatus;
	}

	public void setVserverProvStatus(String vserverProvStatus) {
		this.vserverProvStatus = vserverProvStatus;
	}

	/*
	 * public String getVserverVserverName() { return vserverVserverName; }
	 * 
	 * public void setVserverVserverName(String vserverVserverName) {
	 * this.vserverVserverName = vserverVserverName; }
	 */
	public String getvServerVNFId() {
		return vServerVNFId;
	}

	public void setvServerVNFId(String vServerVNFId) {
		this.vServerVNFId = vServerVNFId;
	}
	
}
