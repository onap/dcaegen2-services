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

package org.onap.slice.analysis.ms.dmaap;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.att.nsa.cambria.client.CambriaConsumer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NotificationConsumerTest.class)
public class NotificationConsumerTest {
	
	@Mock
	CambriaConsumer cambriaConsumer;
	
	@Mock
	NotificationCallback notificationCallback;

	@InjectMocks
	NotificationConsumer notificationConsumer;

	@Test
	public void testNotificationConsumer() {
		try {
			List<String> notifications = new ArrayList<>();
			notifications.add("notification1");
			when(cambriaConsumer.fetch()).thenReturn(notifications);
			Mockito.doNothing().when(notificationCallback).activateCallBack(Mockito.anyString());
			notificationConsumer.run();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
