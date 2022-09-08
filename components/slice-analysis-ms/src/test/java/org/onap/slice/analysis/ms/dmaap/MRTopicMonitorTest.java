/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.onap.slice.analysis.ms.models.Configuration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MRTopicMonitorTest.class)
public class MRTopicMonitorTest {

	@Spy
	@InjectMocks
	MRTopicMonitor mrTopicMonitor;

	@Mock
	AaiEventNotificationCallback aaiEventNotificationCallback;
	
	@Before
	public void before() {
		Configuration configuration = Configuration.getInstance();
		String configAllJson = readFromFile("src/test/resources/config_all.json");
		JsonObject configAll = new Gson().fromJson(configAllJson, JsonObject.class);
		JsonObject config = configAll.getAsJsonObject("config");
		configuration.updateConfigurationFromJsonObject(config);
		mrTopicMonitor = new MRTopicMonitor("aai_subscriber", aaiEventNotificationCallback);
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void start() {
		mrTopicMonitor.start();
		Mockito.verify(mrTopicMonitor, Mockito.times(1)).start();
	}

	@Test
	public void run() throws IOException {
		mrTopicMonitor.run();
		Mockito.verify(mrTopicMonitor, Mockito.times(1)).run();
	}

	@Test
	public void stop() {
		mrTopicMonitor.start();
		mrTopicMonitor.stop();
		Mockito.verify(mrTopicMonitor, Mockito.times(1)).stop();
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
			Mockito.verify(client).startClient();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
