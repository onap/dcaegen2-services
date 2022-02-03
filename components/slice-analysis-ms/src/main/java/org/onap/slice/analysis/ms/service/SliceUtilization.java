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
import org.onap.slice.analysis.ms.exception.CpsClientException;
import org.onap.slice.analysis.ms.exception.DesClientException;
import org.onap.slice.analysis.ms.models.AggregatedConfig;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.SliceConfigDetails;
import org.onap.slice.analysis.ms.models.SliceConfigRequest;
import org.onap.slice.analysis.ms.models.SliceConfigResponse;
import org.onap.slice.analysis.ms.restclients.CpsRestClient;
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
    CpsRestClient cpsRestclient;

    @Autowired
    DesRestClient desRestClient;

    private static final Integer J = 8;
    private static final Integer V = 4;
    private static final Integer QM = 2;
    private static final Integer F = 1;
    private static final Double R_MAX = 948.0 / 1024;
    private static final Integer U = 2;
    private static final Double TS = Math.pow(10, -3) / (14 * 2 * U);
    private static final Double OH_Dl = 0.18;
    private static final Double OH_Ul = 0.10;
    private static final Integer TOTAL_PRB = 132;

    /**
     * Calculates the slice utilization details for requested slices.
     *
     * @param sliceConfigRequest contains rannfnssiId
     * @return SliceConfigResponse contains slice utilization details
     */
    public SliceConfigResponse getSliceUtilizationData(SliceConfigRequest sliceConfigRequest) {

        log.info("getSliceUtilizationData");
        SliceConfigResponse sliceConfigResponse = new SliceConfigResponse();
        ArrayList<SliceConfigDetails> sliceConfigDetailsList = new ArrayList<>();

        try {
            sliceConfigRequest.getSliceIdentifiers().forEach(rannfnssiid -> {
                List<String> snssaiList = null;
                snssaiList = getSnssaiList(rannfnssiid);
                List<JSONObject> pmDataList = new ArrayList<>();
                snssaiList.forEach(snssai -> {
                    JSONObject pmData = getPMData(snssai);
                    pmDataList.add(pmData);
                });
                AggregatedConfig aggregatedConfig = calculateSliceUtilization(pmDataList);
                SliceConfigDetails sliceConfigDetails = new SliceConfigDetails();
                sliceConfigDetails.setSliceIdentifiers(rannfnssiid);
                sliceConfigDetails.setAggregatedConfig(aggregatedConfig);
                sliceConfigDetailsList.add(sliceConfigDetails);

            });
        } catch (Exception e) {
            log.error("Exception caught while fetching data");
        }

        sliceConfigResponse.setSliceConfigDetails(sliceConfigDetailsList);
        return sliceConfigResponse;

    }

   /**
    * Fetches list of SNSSAIs associated with the RANNFNSSI from CPS.
    *
    * @param rannfnssiid RANNFNSSI ID
    * @return snssaiList contains list of SNSSAIs
    */
    protected List<String> getSnssaiList(String rannfnssiid) {
      List<String> snssaiList = null;
        String cpsBaseUrl = Configuration.getInstance().getCpsUrl();
        String templateId = Configuration.getInstance().getRannfnssiDetailsTemplateId();
        Map<String, String> inputParameters = new HashMap<>();
        inputParameters.put("rannfnssiid", rannfnssiid);
        Map<String, Map<String, String>> requestBody = new HashMap<>();
        requestBody.put("inputParameters", inputParameters);
        ResponseEntity<String> response = null;
        try {

            String requestUrl = cpsBaseUrl + "/" + templateId;
            String jsonRequestBody = new ObjectMapper().writeValueAsString(requestBody);
            log.info("fetching rannfnssi details from CPS for : {}", rannfnssiid);
            response = cpsRestclient.sendPostRequest(requestUrl, jsonRequestBody,
                    new ParameterizedTypeReference<String>() {});
            log.info("Response from CPS {}", response);

            if (response.getStatusCode().value() == 200) {
                snssaiList = new ArrayList<>();
                JSONObject jSONObject = new JSONObject(response.getBody());
                JSONArray sliceProfilesList = jSONObject.getJSONArray("sliceProfilesList");
                for (int i = 0; i < sliceProfilesList.length(); i++) {
                    String snssai = sliceProfilesList.getJSONObject(i).getString("sNSSAI");
                    snssaiList.add(snssai);
                }
            } else {
                throw new CpsClientException(String.format("Response code from CPS other than 200: %d\", statusCode",
                        response.getStatusCode().value()));
            }

        } catch (Exception e) {
            log.error("Error while fetching data from CPS {} ", e);
        }
        return snssaiList;

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
            response = desRestClient.sendPostRequest(desUrl, jsonRequestBody,
                    new ParameterizedTypeReference<String>() {});
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
     * @return AggregatedConfig containing slice utilization details
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

        while (j <= J) {
            tempdl += ((Math.pow(V, j) * Math.pow(QM, j) * Math.pow(F, j) * R_MAX * (TOTAL_PRB - averageDLPrb) * 12
                            * (1 - Math.pow(OH_Dl, j))) / TS) * Math.pow(10, -6);
            tempul += ((Math.pow(V, j) * Math.pow(QM, j) * Math.pow(F, j) * R_MAX * (TOTAL_PRB - averageULPrb) * 12
                            * (1 - Math.pow(OH_Ul, j))) / TS) * Math.pow(10, -6);
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
