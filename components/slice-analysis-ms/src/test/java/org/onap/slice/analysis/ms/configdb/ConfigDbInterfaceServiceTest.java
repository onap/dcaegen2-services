/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020-2021 Wipro Limited.
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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.slice.analysis.ms.models.configdb.CellsModel;
import org.onap.slice.analysis.ms.models.configdb.NetworkFunctionModel;
import org.onap.slice.analysis.ms.restclients.ConfigDbRestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class ConfigDbInterfaceServiceTest {

	@InjectMocks
	ConfigDbInterfaceService configdbservice;

	@Mock
	ConfigDbRestClient restclient;
	
	@Test
	public void fetchCurrentConfigurationOfSlice() {

		Map<String, Integer> responsemap=new HashMap<>();
		responsemap.put("dLThptPerSlice", 1);
		responsemap.put("uLThptPerSlice", 2);
		Mockito.when(restclient.sendGetRequest(Mockito.anyString(), Mockito.any())).thenReturn(new ResponseEntity<Object>(responsemap, HttpStatus.OK));
		assertEquals(responsemap, configdbservice.fetchCurrentConfigurationOfSlice("snssai"));
	}

	@Test
	public void fetchCurrentConfigurationOfRIC() {
		Map<String,Integer> map=new HashMap<>();
		Map<String, Map<String,Integer>> responsemap=new HashMap<>();
		Map<String, List<Map<String,Integer>>> result =new HashMap<String, List<Map<String,Integer>>>();
                map.put("dLThptPerSlice", 45);
		map.put("uLThptPerSlice", 60);
		map.put("nearRTRICId",1);
		responsemap.put("1", map);
                List<Map<String,Integer>> list = new ArrayList<Map<String,Integer>>();
	        list.add(map);
	        result.put("data",list);
                Mockito.when(restclient.sendGetRequest(Mockito.anyString(), Mockito.any())).thenReturn(new ResponseEntity<Object>(result, HttpStatus.OK));
                assertEquals(responsemap, configdbservice.fetchCurrentConfigurationOfRIC("snssai"));

	}
	@Test
	public void fetchRICsOfSnssai() {
		Map<String, List<CellsModel>> response=new HashMap<>();
		List<CellsModel> cellslist=new ArrayList<>();
		List<CellsModel> cellslist1=new ArrayList<>();
		CellsModel cellsmodel1=new CellsModel();
		cellsmodel1.setCellLocalId("1111");
		CellsModel cellsmodel2=new CellsModel();
		cellsmodel2.setCellLocalId("2222");
		cellslist.add(cellsmodel1);
		cellslist.add(cellsmodel2);
		response.put("1", cellslist);
		CellsModel cellsmodel3=new CellsModel();
		cellsmodel3.setCellLocalId("3333");
		CellsModel cellsmodel4=new CellsModel();
		cellsmodel4.setCellLocalId("4444");
		cellslist1.add(cellsmodel3);
		cellslist1.add(cellsmodel4);
		response.put("2", cellslist1);
		Mockito.when(restclient.sendGetRequest(Mockito.anyString(), Mockito.any())).thenReturn(new ResponseEntity<Object>(response, HttpStatus.OK));
		List<String> outputlist=new ArrayList<>();
		outputlist.add("1111");
		outputlist.add("2222");
		Map<String,List<String>> output= configdbservice.fetchRICsOfSnssai("snssai");
		assertEquals(outputlist, output.get("1"));

	}

	@Test
	public void fetchNetworkFunctionsOfSnssai() {

		List<String> responsemap=new ArrayList<>();
		List<NetworkFunctionModel> networkfunctionslist=new ArrayList<NetworkFunctionModel>();
		NetworkFunctionModel nf1=new NetworkFunctionModel();
		nf1.setgNBDUId("1111");
		NetworkFunctionModel nf2=new NetworkFunctionModel();
		nf2.setgNBDUId("2222");
		NetworkFunctionModel nf3=new NetworkFunctionModel();
		nf3.setgNBDUId("3333");
		networkfunctionslist.add(nf1);
		networkfunctionslist.add(nf2);
		networkfunctionslist.add(nf3);
		Mockito.when(restclient.sendGetRequest(Mockito.anyString(), Mockito.any())).thenReturn(new ResponseEntity<Object>(networkfunctionslist, HttpStatus.OK));
		responsemap=configdbservice.fetchNetworkFunctionsOfSnssai("snssai");
		assertEquals(3, responsemap.size());

	}
	public void fetchServiceProfile() {
		Map<String,String> responseMap=new HashMap<String, String>();
		responseMap.put("sNSSAI", "001-010");
		responseMap.put("ranNFNSSIId","1111");
		responseMap.put("sliceProfileId","2222");
		responseMap.put("globalSubscriberId","110-345");
		Mockito.when(restclient.sendGetRequest(Mockito.anyString(), Mockito.any())).thenReturn(new ResponseEntity<Object>(responseMap, HttpStatus.OK));
		assertEquals(responseMap, configdbservice.fetchServiceDetails("snssai"));
	}
}

