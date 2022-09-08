/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
 *   Copyright (C) 2022 Huawei Canada Limited.
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
package org.onap.slice.analysis.ms.dmaap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.slice.analysis.ms.service.PolicyService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = IntelligentSlicingCallbackTest.class)
public class IntelligentSlicingCallbackTest {

	ObjectMapper obj = new ObjectMapper();

	@Spy
	@InjectMocks
	IntelligentSlicingCallback intelligentSlicingCallback;
	
	@Mock
    private PolicyService policyService;
	
	@Test
	public void testActivateCallBack() {
		String output = null;
		try {
			output = new String(Files.readAllBytes(Paths.get("src/test/resources/vesCCVPNNotiModel.json")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		intelligentSlicingCallback.activateCallBack(output);
		Mockito.verify(intelligentSlicingCallback, Mockito.atLeastOnce()).activateCallBack(Mockito.anyString());
	}
	
}
	
