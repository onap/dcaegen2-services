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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.onap.slice.analysis.ms.models.MeasurementObject;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AverageCalculatorTest.class)
public class AverageCalculatorTest {
	
	@InjectMocks
	AverageCalculator averageCalculator;
	
	@Before
	public void setup() {
		List<String> pmNames = new ArrayList<>();
		pmNames.add("PrbUsedDl");
		pmNames.add("PrbUsedUl");
		ReflectionTestUtils.setField(averageCalculator, "pmNames", pmNames);
	}
	
	@Test
	public void findAvgTest() {
		List<MeasurementObject> result = new ArrayList<>();	
		Map<String, Integer> pmData = new HashMap<>();
		pmData.put("PrbUsedDl", 50);
		pmData.put("PrbUsedUl", 48);
		result.add(new MeasurementObject("cell11", pmData));
		pmData.put("PrbUsedDl", 40);
		pmData.put("PrbUsedUl", 38);
		result.add(new MeasurementObject("cell12", pmData));
		
		List<MeasurementObject> exp = new ArrayList<>();	
		pmData.put("PrbUsedDl", 25);
		pmData.put("PrbUsedUl", 24);
		exp.add(new MeasurementObject("cell11", pmData));
		pmData.put("PrbUsedDl", 20);
		pmData.put("PrbUsedUl", 19);
		exp.add(new MeasurementObject("cell12", pmData));

		assertEquals(exp, averageCalculator.findAvg(result, 2));
	}
	
	@Test
	public void findAvgSum() {
		Map<String, Integer> existingMap = new HashMap<>();
		existingMap.put("PrbUsedDl", 50);
		existingMap.put("PrbUsedUl", 48);
		
		Map<String, Integer> currentMap = new HashMap<>();
		currentMap.put("PrbUsedDl", 40);
		currentMap.put("PrbUsedUl", 38);

		Map<String, Integer> result = new HashMap<>();
		result.put("PrbUsedDl", 90);
		result.put("PrbUsedUl", 86);
		
		assertEquals(new MeasurementObject("cell1", result), 
				averageCalculator.findSum(new MeasurementObject("cell1", existingMap), new MeasurementObject("cell1", currentMap)));
	}
}
	
