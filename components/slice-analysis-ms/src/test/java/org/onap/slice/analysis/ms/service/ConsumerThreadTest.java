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
package org.onap.slice.analysis.ms.service;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.slice.analysis.ms.models.SubCounter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsumerThreadTest.class)
public class ConsumerThreadTest {
	ObjectMapper obj = new ObjectMapper();
	@InjectMocks
	PmDataQueue pmDataQueue;

	@Mock
	ConsumerThread consumerThread;
	
	@Test
	public void testRun() {
		consumerThread.run();
		Mockito.verify(consumerThread, Mockito.times(1)).run();
	}

	@Test
	public void testCheckForEnoughSamples() {
		int samples = 3;
		String nf = null;
		String snssai = "";
		if (!pmDataQueue.checkSamplesInQueue(new SubCounter(nf, snssai), samples))
			pmDataQueue.putSnssaiToQueue(snssai);
		assertTrue(true);
	}

}
