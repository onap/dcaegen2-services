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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.restclients.CpsRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * 
 * Service for CPS interfaces
 *
 */
@Service
public class CpsService implements CpsInterface {

    private static Logger log = LoggerFactory.getLogger(CpsService.class);

    @Autowired
    CpsRestClient restclient;
    private static String cpsBaseUrl = Configuration.getInstance().getCpsUrl();

    /**
     * Fetches the current configuration of RIC from CPS
     */
    public Map<String, Map<String, Object>> fetchCurrentConfigurationOfRIC(String snssai) {
        Map<String, Map<String, Object>> responseMap = new HashMap<String, Map<String, Object>>();
        String reqUrl = cpsBaseUrl + "/get-nearrtric-config";
        log.info("fetching current configuration of RIC from Cps: {s-NSSAI: " + snssai + "}");
        String requestBody = "{\"inputParameters\": {\"sNssai\":" + JSONObject.quote(snssai) + "}}";
        try {
            String response = restclient
                    .sendPostRequest(reqUrl, requestBody, new ParameterizedTypeReference<String>() {}).getBody();
            JSONArray sliceArray = new JSONArray(response);
            for (int i = 0; i < sliceArray.length(); i++) {
                String nearRTTICid = sliceArray.getJSONObject(i).optString("idNearRTRIC");
                JSONArray pLMNInfoList =
                        sliceArray.getJSONObject(i).getJSONObject("attributes").getJSONArray("pLMNInfoList");
                for (int j = 0; j < pLMNInfoList.length(); j++) {
                    JSONArray sNSSAIList = pLMNInfoList.getJSONObject(j).getJSONArray("sNSSAIList");
                    for (int k = 0; k < sNSSAIList.length(); k++) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        JSONArray configDataArray = sNSSAIList.getJSONObject(k).getJSONArray("configData");
                        for (int l = 0; l < configDataArray.length(); l++) {
                            JSONObject configData = configDataArray.getJSONObject(l);
                            map.put((String) configData.get("configParameter"), configData.get("configValue"));
                        }
                        responseMap.put(nearRTTICid, map);
                    }
                }
            }
        } catch (Exception e) {
            log.info("CPS fetches current configuration of RIC: " + e);
        }
        return responseMap;
    }

    /**
     * Fetches all the network functions of an S-NSSAI from CPS
     */
    public List<String> fetchNetworkFunctionsOfSnssai(String snssai) {
        List<String> responseList = new ArrayList<>();
        String reqUrl = cpsBaseUrl + "/get-gnbdufunction-by-snssai";
        log.info("fetching network functions of snssai from Cps: {s-NSSAI: " + snssai + "}");
        String requestBody = "{\"inputParameters\": {\"sNssai\":" + JSONObject.quote(snssai) + "}}";
        try {
            String response = restclient
                    .sendPostRequest(reqUrl, requestBody, new ParameterizedTypeReference<String>() {}).getBody();
            JSONArray networkFunctionJsonArry = new JSONArray(response);
            for (int i = 0; i < networkFunctionJsonArry.length(); i++) {
                JSONObject networkFunctionJson = networkFunctionJsonArry.getJSONObject(i);
                responseList.add(networkFunctionJson.getJSONObject("attributes").optString("gNBDUId"));
            }
        } catch (Exception e) {
            log.info("Fetch network functions of S-NSSAI from CPS" + e);
        }
        return responseList;
    }

    /**
     * Fetches the RICS of an S-NSSAI from CPS
     */
    public Map<String, List<String>> fetchRICsOfSnssai(String snssai) {
        Map<String, List<String>> responseMap = new HashMap<>();
        String reqUrl = cpsBaseUrl + "/get-nrcelldu-by-snssai";
        log.info("fetching RIC of s-NSSAI from Cps: {s-NSSAI: " + snssai + "}");
        String requestBody = "{\"inputParameters\": {\"sNssai\":" + JSONObject.quote(snssai) + "}}";
        try {
            String response = restclient
                    .sendPostRequest(reqUrl, requestBody, new ParameterizedTypeReference<String>() {}).getBody();
            JSONArray sliceArray = new JSONArray(response);
            for (int i = 0; i < sliceArray.length(); i++) {
                String nearRTTICid = sliceArray.getJSONObject(i).optString("idNearRTRIC");
                JSONArray GNBDUFunctionArray = sliceArray.getJSONObject(i).getJSONArray("GNBDUFunction");
                for (int j = 0; j < GNBDUFunctionArray.length(); j++) {
                    JSONArray NRCellDUArray = GNBDUFunctionArray.getJSONObject(j).getJSONArray("NRCellDU");
                    List<String> cellslist = new ArrayList<>();
                    for (int k = 0; k < NRCellDUArray.length(); k++) {
                        cellslist.add(
                                NRCellDUArray.getJSONObject(k).getJSONObject("attributes").optString("cellLocalId"));
                    }
                    responseMap.put(nearRTTICid, cellslist);
                }
            }
        } catch (Exception e) {
            log.info("Fetch RICS of S-NSSAI from CPS" + e);
        }
        return responseMap;
    }
}
