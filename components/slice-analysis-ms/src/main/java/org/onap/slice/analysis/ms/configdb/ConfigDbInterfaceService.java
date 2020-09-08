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
package org.onap.slice.analysis.ms.configdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onap.slice.analysis.ms.models.configdb.CellsModel;
import org.onap.slice.analysis.ms.models.configdb.NetworkFunctionModel;
import org.onap.slice.analysis.ms.restclients.ConfigDbRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ConfigDbInterfaceService implements IConfigDbService {

	@Autowired
	ConfigDbRestClient restclient;
	
	public Map<String, Integer> fetchCurrentConfigurationOfSlice(String snssai){
		Map<String,Integer> responseMap = new HashMap<>();
		String reqUrl="http://localhost:8080/api/sdnc-config-db/v4/profile-config/"+snssai;

		ResponseEntity<Map<String,Integer>> response=restclient.sendGetRequest(reqUrl,new ParameterizedTypeReference<Map<String, Integer>>() {
		});
		responseMap=response.getBody();
		return responseMap;
				
	}
	
	public Map<String,Map<String,Integer>> fetchCurrentConfigurationOfRIC(String snssai){
		String reqUrl="http://localhost:8080/api/sdnc-config-db/v4/slice-config/"+snssai;
		ResponseEntity<Map<String,Map<String,Integer>>> response=restclient.sendGetRequest(reqUrl, new ParameterizedTypeReference<Map<String,Map<String,Integer>>>() {
		});
		return response.getBody();
	}
	
	public List<String> fetchNetworkFunctionsOfSnssai(String snssai){
		List<String> responseList=new ArrayList<>();
		String reqUrl="http://localhost:8080/api/sdnc-config-db/v4/du-list/"+snssai;
		ResponseEntity<List<NetworkFunctionModel>> response=restclient.sendGetRequest(reqUrl, new ParameterizedTypeReference<List<NetworkFunctionModel>>() {
		});
		for(NetworkFunctionModel networkFn:response.getBody()) {
			responseList.add(networkFn.getgNBDUId());	
		}
		return responseList;
	}
	
	public Map<String, List<String>> fetchRICsOfSnssai(String snssai){
		
		Map<String,List<String>> responseMap=new HashMap<>();
		
		String reqUrl="http://localhost:8080/api/sdnc-config-db/v4/du-cell-list/"+snssai;
		
		ResponseEntity<Map<String,List<CellsModel>>> response=restclient.sendGetRequest(reqUrl, new ParameterizedTypeReference<Map<String,List<CellsModel>>>() {
		});

		
		for (Map.Entry<String, List<CellsModel>> entry : response.getBody().entrySet()) {
			List<String> cellslist=new ArrayList<String>();
			for(CellsModel cellmodel:entry.getValue()) {
				
				cellslist.add(cellmodel.getCellLocalId());
			}
			responseMap.put(entry.getKey(), cellslist);
		}
		
		return responseMap;
	}
	public Map<String,String> fetchServiceDetails(String snssai){
		String reqUrl="http://localhost:8080/api/sdnc-config-db/v4/subscriber-details/"+snssai;
		ResponseEntity<Map<String,String>> response=restclient.sendGetRequest(reqUrl, new ParameterizedTypeReference<Map<String,String>>() {
		});
		return response.getBody();
	}	
	
}
