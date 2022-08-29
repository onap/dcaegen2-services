/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  son-handler
 *  ================================================================================
 *   Copyright (C) 2019-2020 Wipro Limited.
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

import org.onap.slice.analysis.ms.models.ConfigPolicy;
import org.onap.slice.analysis.ms.service.ConfigThread;
import org.onap.slice.analysis.ms.service.ConsumerThread;
import org.onap.slice.analysis.ms.service.PmThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * This class starts the pm thread and consumer thread
 */
public class MainThread {

	private static Logger log = LoggerFactory.getLogger(MainThread.class);
	
	private MainThread() {
		
	}
	
	/**
	 * main thread initialization.
	 */
	public static void initiateThreads() {
		log.debug("initializing Pm thread & Consumer thread");
		Thread pmThread = new Thread(new PmThread());
		pmThread.start();
		Thread consumerThread = new Thread(new ConsumerThread());
		consumerThread.start();
		Thread configThread = new Thread(new ConfigThread());
		configThread.start();
	}

}
