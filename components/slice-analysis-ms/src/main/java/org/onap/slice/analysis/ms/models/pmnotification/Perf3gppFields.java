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

/** 
 * Model class for the Perf3gppFields Object 
 */
public class Perf3gppFields {

	private String perf3gppFieldsVersion;
	private MeasDataCollection measDataCollection;

	public String getPerf3gppFieldsVersion() {
		return perf3gppFieldsVersion;
	}

	public void setPerf3gppFieldsVersion(String perf3gppFieldsVersion) {
		this.perf3gppFieldsVersion = perf3gppFieldsVersion;
	}

	public MeasDataCollection getMeasDataCollection() {
		return measDataCollection;
	}

	public void setMeasDataCollection(MeasDataCollection measDataCollection) {
		this.measDataCollection = measDataCollection;
	}

}