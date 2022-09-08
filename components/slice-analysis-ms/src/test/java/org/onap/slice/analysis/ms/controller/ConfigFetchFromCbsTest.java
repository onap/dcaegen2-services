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
package org.onap.slice.analysis.ms.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import ch.qos.logback.core.util.Duration;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConfigFetchFromCbs.class)
public class ConfigFetchFromCbsTest {

	@Test
	public void testConfigFetchFromCbsDuration() {
		Duration interval = new Duration(1);
		interval.getMilliseconds();
	}

	@Test
	public void testRun() {
		ConfigFetchFromCbs configFetchFromCb = new ConfigFetchFromCbs();
		configFetchFromCb.run();
	}

	@Test
	public void getAppconfig() {
		@SuppressWarnings("unused")
		ConfigFetchFromCbs config = new ConfigFetchFromCbs();
		Method getAppConfig = null;
		try {
			getAppConfig = ConfigFetchFromCbs.class.getDeclaredMethod("getAppConfig");
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		getAppConfig.setAccessible(true);
		try {
			getAppConfig.invoke(getAppConfig);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
}
	      
