/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020-2022 Wipro Limited.
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
package org.onap.slice.analysis.ms.configdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.configdb.CellsModel;
import org.onap.slice.analysis.ms.models.configdb.NetworkFunctionModel;
import org.onap.slice.analysis.ms.restclients.ConfigDbRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 *
 *  Service for config db interfaces
 *
 */
@Service
public class ConfigDbInterfaceService implements IConfigDbService {

        @Autowired
        private ConfigDbRestClient restclient;
        private String configDbBaseUrl = Configuration.getInstance().getConfigDbService();

        /**
         *  Fetches the current configuration of an S-NSSAI from config DB
         */
        public Map<String, Integer> fetchCurrentConfigurationOfSlice(String snssai){
                Map<String,Integer> responseMap = null;
                String reqUrl=configDbBaseUrl+"/api/sdnc-config-db/v4/profile-config/"+snssai;

                ResponseEntity<Map<String,Integer>> response=restclient.sendGetRequest(reqUrl,new ParameterizedTypeReference<Map<String, Integer>>() {
                });
                responseMap=response.getBody();
                return responseMap;
        }

        /**
         *  Fetches the current configuration of RIC from config DB
         */
        public Map<String, Map<String, Object>> fetchCurrentConfigurationOfRIC(String snssai) {
                String reqUrl = configDbBaseUrl + "/api/sdnc-config-db/v4/slice-config/" + snssai;
                Map<String, Map<String, Object>> responseMap = new HashMap<String, Map<String, Object>>();
                ResponseEntity<Map<String, List<Map<String, Object>>>> response = restclient.sendGetRequest(reqUrl,
                                new ParameterizedTypeReference<Map<String, List<Map<String, Object>>>>() {
                                });
                if (Objects.nonNull(response)) {
                        for (Map.Entry<String, List<Map<String, Object>>> entry : response.getBody().entrySet()) {
                                List<Map<String, Object>> list = entry.getValue();
                                if (!list.isEmpty()) {
                                        list.forEach(l -> {
                                                if (l.containsKey("nearRTRICId")) {
                                                        responseMap.put(String.valueOf(l.get("nearRTRICId")), l);
                                                }
                                        });
                                }

                        }
                }
                return responseMap;
        }

        /**
         *  Fetches all the network functions of an S-NSSAI from config DB
         */
        public List<String> fetchNetworkFunctionsOfSnssai(String snssai){
                List<String> responseList=new ArrayList<>();
                String reqUrl=configDbBaseUrl+"/api/sdnc-config-db/v4/du-list/"+snssai;
                ResponseEntity<List<NetworkFunctionModel>> response=restclient.sendGetRequest(reqUrl, new ParameterizedTypeReference<List<NetworkFunctionModel>>() {
                });
                for(NetworkFunctionModel networkFn:response.getBody()) {
                        responseList.add(networkFn.getgNBDUId());
                }
                return responseList;
        }

        /**
         *  Fetches the RICS of an S-NSSAI from config DB
         */
        public Map<String, List<String>> fetchRICsOfSnssai(String snssai){
                Map<String,List<String>> responseMap=new HashMap<>();
                String reqUrl=configDbBaseUrl+"/api/sdnc-config-db/v4/du-cell-list/"+snssai;
                ResponseEntity<Map<String,List<CellsModel>>> response = restclient.sendGetRequest(reqUrl, new ParameterizedTypeReference<Map<String,List<CellsModel>>>() {
                });

                for (Map.Entry<String, List<CellsModel>> entry : response.getBody().entrySet()) {
                        List<String> cellslist=new ArrayList<>();
                        for(CellsModel cellmodel:entry.getValue()) {
                                cellslist.add(cellmodel.getCellLocalId());
                        }
                        responseMap.put(entry.getKey(), cellslist);
                }
                return responseMap;
        }

        /**
         *  Fetches the details of a service
         */
        public Map<String,String> fetchServiceDetails(String snssai){
                String reqUrl=configDbBaseUrl+"/api/sdnc-config-db/v4/subscriber-details/"+snssai;
                ResponseEntity<Map<String,String>> response=restclient.sendGetRequest(reqUrl, new ParameterizedTypeReference<Map<String,String>>() {
                });
                return response.getBody();
        }

        /**
         *  Fetches the CUCP Cells of an S-NSSAI from config DB
         */
        public Map<String, List<String>> fetchCUCPCellsOfSnssai(String snssai){
                Map<String,List<String>> responseMap=new HashMap<>();
                String reqUrl=configDbBaseUrl+"/api/sdnc-config-db/v4/cucp-cell-list/"+snssai;
                ResponseEntity<Map<String,List<CellsModel>>> response = restclient.sendGetRequest(reqUrl, new ParameterizedTypeReference<Map<String,List<CellsModel>>>() {
                });

                for (Map.Entry<String, List<CellsModel>> entry : response.getBody().entrySet()) {
                        List<String> cellslist=new ArrayList<>();
                        for(CellsModel cellmodel:entry.getValue()) {
                                cellslist.add(cellmodel.getCellLocalId());
                        }
                        responseMap.put(entry.getKey(), cellslist);
                }
                return responseMap;
        }

}

