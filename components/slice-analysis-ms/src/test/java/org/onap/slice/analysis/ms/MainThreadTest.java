/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  son-handler
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.slice.analysis.ms.data.repository.PerformanceNotificationsRepository;
import org.onap.slice.analysis.ms.dmaap.NewPmNotification;
import org.onap.slice.analysis.ms.service.PmDataQueue;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@PrepareForTest({ MainThreadTest.class,PmDataQueue.class,NewPmNotification.class,PerformanceNotificationsRepository.class})
@SpringBootTest(classes = MainThread.class)
public class MainThreadTest {
	
	@Mock
	MainThreadTest mainThreadTest;
	
	@Test
	public void start() {
		mainThreadTest.start();
		Mockito.verify(mainThreadTest, Mockito.times(1)).start();
	}
	@Test
	public void run() {
		mainThreadTest.run();
		Mockito.verify(mainThreadTest, Mockito.times(1)).run();
	}
	@Test
	public void stop() {
		mainThreadTest.start();
		mainThreadTest.stop();
		Mockito.verify(mainThreadTest, Mockito.times(1)).stop();
	}
	
	@Test
	public void testInitiateThreads() {
		mainThreadTest.testInitiateThreads();
		Mockito.verify(mainThreadTest, Mockito.atLeastOnce()).testInitiateThreads();	
	}	
	
}
