/*
* ============LICENSE_START=======================================================
* ONAP : DATALAKE
* ================================================================================
* Copyright 2019 China Mobile
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

package org.onap.datalake.feeder.service;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service that pulls messages from DMaaP and save them to Big Data DBs
 * 
 * @author Guobiao Mo
 *
 */

@Service
public class PullService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private boolean isRunning = false;
	private ExecutorService executorService;
	private Thread topicConfigPollingThread;

	@Autowired
	private Puller puller;

	@Autowired
	private TopicConfigPollingService topicConfigPollingService;
	
	@Autowired
	private ApplicationConfiguration config;

	/**
	 * @return the isRunning
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * start pulling.
	 * 
	 * @throws IOException
	 */
	public synchronized void start() {
		if (isRunning) {
			return;
		}

		logger.info("start pulling ...");
		int numConsumers = config.getKafkaConsumerCount();
		executorService = Executors.newFixedThreadPool(numConsumers);

		for (int i = 0; i < numConsumers; i++) {
			executorService.submit(puller);
		}
		
		topicConfigPollingThread = new Thread(topicConfigPollingService);
		topicConfigPollingThread.setName("TopicConfigPolling");
		topicConfigPollingThread.start();

		isRunning = true;

		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
	}

	/**
	 * stop pulling
	 */
	public synchronized void shutdown() {
		if (!isRunning) {
			return;
		}

		logger.info("stop pulling ...");
		puller.shutdown();

		logger.info("stop TopicConfigPollingService ...");
		topicConfigPollingService.shutdown();

		try {
			topicConfigPollingThread.join();
			
			executorService.shutdown();
			executorService.awaitTermination(120L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("executor.awaitTermination", e);
			Thread.currentThread().interrupt();
		}

		isRunning = false;
	}

}
