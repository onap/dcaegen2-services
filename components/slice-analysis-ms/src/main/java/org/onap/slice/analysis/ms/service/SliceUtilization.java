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

package org.onap.slice.analysis.ms.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.slice.analysis.ms.aai.AaiService;
import org.onap.slice.analysis.ms.exception.DesClientException;
import org.onap.slice.analysis.ms.models.AggregatedConfig;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.SliceConfigDetails;
import org.onap.slice.analysis.ms.models.SliceConfigRequest;
import org.onap.slice.analysis.ms.models.SliceConfigResponse;
import org.onap.slice.analysis.ms.restclients.DesRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * This Service calculates the slice utilization details
 */
@Service
public class SliceUtilization {

    private static Logger log = LoggerFactory.getLogger(SliceUtilization.class);

    @Autowired
    private AaiService aaiService;

    @Autowired
    DesRestClient desRestClient;

    /**
     * Default values set for uplink and downlink throughput calculation (in
     * accordance with 3GPP specifications)
     *
     */
    private static final Integer numOfAggregatedComponentCarries = 8;
    private static final Integer maxNumOfLayers = 4;
    private static final Integer maxModulationOrder = 2;
    private static final Integer scalingFactor = 1;
    private static final Double maxBitRate = 948.0 / 1024;
    private static final Integer nrNumerology = 2;
    private static final Double averageSymbolDuration = Math.pow(10, -3) / (14 * 2 * nrNumerology);
    private static final Double overHeadForDl = 0.18;
    private static final Double overHeadForUl = 0.10;
    private static final Integer TOTAL_PRB = 132;

    /**
     * Calculates the slice utilization details for requested slices.
     *
     * @param sliceConfigRequest contains sliceInstanceId
     * @return SliceConfigResponse contains slice utilization details
     */
    public SliceConfigResponse getSliceUtilizationData(SliceConfigRequest sliceConfigRequest) {

        log.info("getSliceUtilizationData");
        SliceConfigResponse sliceConfigResponse = new SliceConfigResponse();
        ArrayList<SliceConfigDetails> sliceConfigDetailsList = new ArrayList<>();

        try {
            sliceConfigRequest.getSliceIdentifiers().forEach(sliceInstanceId -> {
                List<String> snssaiList = null;
                snssaiList = aaiService.getSnssaiList(sliceInstanceId);
                List<JSONObject> pmDataList = new ArrayList<>();
                snssaiList.forEach(snssai -> {
                    JSONObject pmData = getPMData(snssai);
                    pmDataList.add(pmData);
                });
                AggregatedConfig aggregatedConfig = calculateSliceUtilization(pmDataList);
                SliceConfigDetails sliceConfigDetails = new SliceConfigDetails();
                sliceConfigDetails.setSliceIdentifiers(sliceInstanceId);
                sliceConfigDetails.setAggregatedConfig(aggregatedConfig);
                sliceConfigDetailsList.add(sliceConfigDetails);

            });
        } catch (Exception e) {
            log.error("Exception caught while fetching data");
        }
        sliceConfigResponse.setSliceConfigDetails(sliceConfigDetailsList);
        log.info("SliceConfigResponse: " + sliceConfigResponse.toString());
        return sliceConfigResponse;

    }

    /**
     * Fetches the PM data for requested SNSSAI from DES.
     *
     * @param snssai snssai ID
     * @return PM Data for requested SNSSAI
     */
    protected JSONObject getPMData(String snssai) {
        String desUrl = Configuration.getInstance().getDesUrl();
        Map<String, String> inputParameter = new HashMap<>();
        int duration = Configuration.getInstance().getPmDataDurationInWeeks();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.add(Calendar.WEEK_OF_MONTH, -duration);
        inputParameter.put("snssai", "SM.PrbUsedDl." + snssai);
        inputParameter.put("time", String.valueOf(calendar.getTimeInMillis()));
        ResponseEntity<String> response = null;
        JSONObject pmData = null;
        try {

            String jsonRequestBody = new ObjectMapper().writeValueAsString(inputParameter);
            log.info("fetching PM Data for : {}", snssai);
            response =
                    desRestClient.sendPostRequest(desUrl, jsonRequestBody, new ParameterizedTypeReference<String>() {});
            pmData = new JSONObject(response.getBody());
            if (response.getStatusCode().value() != 200) {
                throw new DesClientException(String.format("Response code from DES other than 200: %d\", statusCode",
                        response.getStatusCode().value()));
            }

        } catch (Exception e) {
            log.error("Error while fetching PM data from DES {} ", e);
        }

        return pmData;
    }

    /**
     * Calculates average DL and UL PRB used from the PM data and returns the
     * remaining resources(utilization details) in terms of throughput
     *
     * @param pmDataList PM data for all SNSSAIs
     * @return aggregatedConfig containing slice utilization details
     */
    protected AggregatedConfig calculateSliceUtilization(List<JSONObject> pmDataList) {

        Integer numOfPRBData = 0;
        Integer dlPRBUsed = 0;
        Integer ulPRBUsed = 0;
        Integer dlThptIndex = 0;
        Integer ulThptIndex = 1;
        try {
            for (JSONObject pmData : pmDataList) {
                JSONArray result = pmData.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject measValuesObject = result.getJSONObject(i);
                    String measValuesListStr = measValuesObject.getString("measValuesList");
                    String sMeasTypesListtStr = measValuesObject.getString("sMeasTypesList");
                    JSONArray measValuesArray = new JSONArray(measValuesListStr);
                    JSONArray sMeasTypesArray = new JSONArray(sMeasTypesListtStr);
                    if (sMeasTypesArray.getString(0).contains("PrbUsedUl")) {
                        ulThptIndex = 0;
                        dlThptIndex = 1;
                    }
                    numOfPRBData += measValuesArray.length();
                    for (int j = 0; j < measValuesArray.length(); j++) {
                        dlPRBUsed +=
                                measValuesArray.getJSONArray(j).getJSONArray(2).getJSONArray(dlThptIndex).getInt(1);
                        ulPRBUsed +=
                                measValuesArray.getJSONArray(j).getJSONArray(2).getJSONArray(ulThptIndex).getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception caught while calculating slice utilization: {}", e);
        }

        Integer averageDLPrb = dlPRBUsed / numOfPRBData;
        Integer averageULPrb = ulPRBUsed / numOfPRBData;
        Double tempdl = 0.0;
        Double tempul = 0.0;
        int j = 1;

        while (j <= numOfAggregatedComponentCarries) {
            tempdl += ((Math.pow(maxNumOfLayers, j) * Math.pow(maxModulationOrder, j) * Math.pow(scalingFactor, j)
                    * maxBitRate * (TOTAL_PRB - averageDLPrb) * 12 * (1 - Math.pow(overHeadForDl, j)))
                    / averageSymbolDuration) * Math.pow(10, -6);
            tempul += ((Math.pow(maxNumOfLayers, j) * Math.pow(maxModulationOrder, j) * Math.pow(scalingFactor, j)
                    * maxBitRate * (TOTAL_PRB - averageULPrb) * 12 * (1 - Math.pow(overHeadForUl, j)))
                    / averageSymbolDuration) * Math.pow(10, -6);
            j++;

        }

        Integer dLThptPerSliceInMbps = (int) Math.round(tempdl);
        Integer uLThptPerSliceInMbps = (int) Math.round(tempul);
        AggregatedConfig aggregatedConfig = new AggregatedConfig();
        aggregatedConfig.setDLThptPerSlice(dLThptPerSliceInMbps);
        aggregatedConfig.setULThptPerSlice(uLThptPerSliceInMbps);
        return aggregatedConfig;
    }
}
