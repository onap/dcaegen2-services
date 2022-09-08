/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2021-2022 Wipro Limited.
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

package org.onap.slice.analysis.ms.cps;

import java.util.List;
import java.util.Map;

/**
 *
 * Interface for CPS
 *
 */
public interface CpsInterface {

    public Map<String, List<String>> fetchRICsOfSnssai(String snssai);

    public List<String> fetchNetworkFunctionsOfSnssai(String snssai);

    public Map<String, Map<String, Object>> fetchCurrentConfigurationOfRIC(String snssai);

    public Map<String, List<String>> fetchnrCellCUsOfSnssai(String snssai);

}
