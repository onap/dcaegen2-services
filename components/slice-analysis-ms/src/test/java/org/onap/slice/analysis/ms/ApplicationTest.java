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

package org.onap.slice.analysis.ms;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.slice.analysis.ms.dmaap.DmaapClient;
import org.onap.slice.analysis.ms.models.ConfigPolicy;
import org.onap.slice.analysis.ms.models.Configuration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationTest.class)
public class ApplicationTest {
	@Mock
	MainThread mainThread;

	@Test
	public void getConfig() {
		@SuppressWarnings("unused")
		Application application = new Application();
		String configFile = System.getenv("CONFIG_FILE");
		@SuppressWarnings("unused")
		String configAllJson = readFromFile(configFile);
		Method getConfig = null;
		try {
			getConfig = Application.class.getDeclaredMethod("getConfig");
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		getConfig.setAccessible(true);
		try {
			getConfig.invoke(getConfig);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		ConfigPolicy configPolicy = ConfigPolicy.getInstance();
		Map<String, Object> policy = new HashMap<>();
		policy.put("policyName", "pcims_policy");
		configPolicy.setConfig(policy);
		assertEquals(policy, configPolicy.getConfig());
	}

	@Test
	public void datasource() {
		Application application = new Application();
		application.dataSource();
	}

	@Test
	public void restTemplate() {
		Application application = new Application();
		application.restTemplate();
	}

	private static String readFromFile(String file) {
		String content = "";
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
			content = bufferedReader.readLine();
			String temp;
			while ((temp = bufferedReader.readLine()) != null) {
				content = content.concat(temp);
			}
			content = content.trim();
		} catch (Exception e) {
			content = null;
		}
		return content;
	}

	@Test
	public void startClientTest() {
		try {
			Configuration configuration = Configuration.getInstance();
			String configAllJson = readFromFile("src/test/resources/config_all.json");
			JsonObject configAll = new Gson().fromJson(configAllJson, JsonObject.class);
			JsonObject config = configAll.getAsJsonObject("config");
			System.out.println(configuration);
			configuration.updateConfigurationFromJsonObject(config);
			DmaapClient client = new DmaapClient();
			client.initClient();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
