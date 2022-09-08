/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Wipro Limited.
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

package org.onap.slice.analysis.ms.models.aai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for receiving Relationship data of a service from AAI
 */
@Data
@NoArgsConstructor
public class Relationship {

    @JsonProperty("related-to")
    private String relatedTo;
    @JsonProperty("relationship-label")
    private String relationshipLabel;
    @JsonProperty("related-link")
    private String relatedLink;
    @JsonProperty("relationship-data")
    private List<Map<String, String>> relationshipData;
    @JsonProperty("related-to-property")
    private List<Map<String, String>> relatedToProperty;

}
