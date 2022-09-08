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

package org.onap.slice.analysis.ms.controller;

import org.onap.slice.analysis.ms.models.SliceConfigRequest;
import org.onap.slice.analysis.ms.models.SliceConfigResponse;
import org.onap.slice.analysis.ms.service.SliceUtilization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * This Controller provides slice configuration details
 */
@RestController
public class SliceConfiguraton {

    @Autowired
    SliceUtilization sliceUtilization;

    /**
     * This method provides the slice utilization details for requested slices.
     *
     * @param sliceConfigRequest contains sliceInstanceId
     * @return sliceConfigResponse contains slice configuration details
     */
    @GetMapping(value = "/api/v1/slices-config")
    public ResponseEntity<SliceConfigResponse> sliceConfigRequest(@RequestBody SliceConfigRequest sliceConfigRequest) {
        try {
            SliceConfigResponse sliceConfigResponse = sliceUtilization.getSliceUtilizationData(sliceConfigRequest);
            return new ResponseEntity<SliceConfigResponse>(sliceConfigResponse, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
