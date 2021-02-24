/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2021 Wipro Limited.
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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.configdb.CellsModel;
import org.onap.slice.analysis.ms.models.configdb.NetworkFunctionModel;
import org.onap.slice.analysis.ms.restclients.AaiRestClient;
import org.onap.slice.analysis.ms.restclients.ConfigDbRestClient;
import org.onap.slice.analysis.ms.utils.BeanUtil;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({ AaiService.class,Configuration.class })
@SpringBootTest(classes = AaiInterfaceServiceTest.class)
public class AaiInterfaceServiceTest {
	
	Configuration configuration = Configuration.getInstance();
	
	@InjectMocks
	AaiService aaiService;

	@Mock
	AaiRestClient restClient;

	@Test
	public void fetchCurrentConfigurationOfSlice() {
		configuration.setAaiUrl("http://aai:30233/aai/v21/business/customers/customer/");
                PowerMockito.mockStatic(AaiService.class);
		PowerMockito.mockStatic(Configuration.class);
   		PowerMockito.when(Configuration.getInstance()).thenReturn(configuration);
		Map<String, Integer> responsemap = new HashMap<>();
		responsemap.put("dLThptPerSlice", 60);
		responsemap.put("uLThptPerSlice", 54);
		try {
			String serviceInstance = new String(
					Files.readAllBytes(Paths.get("src/test/resources/aaiDetailsList.json")));
			Mockito.when(restClient.sendGetRequest(Mockito.anyString(), Mockito.any()))
					.thenReturn(new ResponseEntity<Object>(serviceInstance, HttpStatus.OK));


		} catch (Exception e) {
			e.printStackTrace();

		}
		assertEquals(responsemap, aaiService.fetchCurrentConfigurationOfSlice("001-010000"));
	}

	@Test
	public void fetchServiceProfile() {
		Map<String, String> responseMap = new HashMap<String, String>();
		responseMap.put("sNSSAI", "001-00110");
		responseMap.put("ranNFNSSIId", "4b889f2b-8ee4-4ec7-881f-5b1af8a74039");
		responseMap.put("sliceProfileId", "ab9af40f13f7219099333");
		responseMap.put("globalSubscriberId", "5GCustomer");
		responseMap.put("subscriptionServiceType", "5G");

		try {
			String serviceInstance = new String(
					Files.readAllBytes(Paths.get("src/test/resources/aaiDetailsList.json")));
			Mockito.when(restClient.sendGetRequest(Mockito.anyString(), Mockito.any()))
					.thenReturn(new ResponseEntity<Object>(serviceInstance, HttpStatus.OK));

		} catch (Exception e) {
			e.printStackTrace();

		}

		assertEquals(responseMap, aaiService.fetchServiceDetails("001-00110"));
	}
}

