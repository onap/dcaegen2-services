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

package org.onap.slice.analysis.ms.service;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.onap.slice.analysis.ms.models.policy.AdditionalProperties;
import org.onap.slice.analysis.ms.models.policy.OnsetMessage;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PolicyServiceTest.class)
public class PolicyServiceTest {
	ObjectMapper obj = new ObjectMapper();
	
	@InjectMocks
	PolicyService policyService;
	
	@Test
	public void formPolicyOnsetMessageTest() {
		String snssai = "001-100001";
		Map<String, String> input = null;
		OnsetMessage output = null;
		String expected = "";
		String actual = "";
		Map<String, Map<String, Integer>> ricToThroughputMapping = new HashMap<>();
		Map<String, Integer> ric1 = new HashMap<>();
		Map<String, Integer> ric2 = new HashMap<>();
		ric1.put("dLThptPerSlice",50);
		ric1.put("uLThptPerSlice",40);
		ric2.put("dLThptPerSlice",50);
		ric2.put("uLThptPerSlice",30);		
		ricToThroughputMapping.put("1", ric1);
		ricToThroughputMapping.put("2", ric2);
        try { 
             input = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/serviceDetails.json"))), new TypeReference<Map<String,String>>(){});
             output = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/onsetMessage.json"))), OnsetMessage.class);
             expected = obj.writeValueAsString(output);
        } 
        catch (IOException e) { 
             e.printStackTrace(); 
        } 
        AdditionalProperties<Map<String, Map<String, Integer>>> addProps = new AdditionalProperties<>();
		addProps.setResourceConfig(ricToThroughputMapping);
        actual = new Gson().toJson(policyService.formPolicyOnsetMessage(snssai,addProps,input));
           
        assertThatJson(actual)
        .whenIgnoringPaths("requestID","payload","closedLoopAlarmStart", "AAI", "target_type", "aai", "targetType")
        .isEqualTo(expected);
	}

	@Test
	public void formPolicyOnsetMessageForCCVPNTest() {
		String cllId = "cll-instance-01";
		OnsetMessage output = null;
		String expected = "";
		String actual = "";
		try {
			output = obj.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/onsetMessage2.json"))), OnsetMessage.class);
			expected = obj.writeValueAsString(output);

			String msg = obj.writeValueAsString(
				policyService.formPolicyOnsetMessageForCCVPN(cllId, 3000)
			);
			actual = new Gson().toJson(msg);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		assertThatJson(actual)
				.whenIgnoringPaths("requestID","payload","closedLoopAlarmStart", "AAI", "target_type", "aai", "targetType")
				.isEqualTo(expected);
	}
}
