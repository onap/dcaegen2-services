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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.repository.KafkaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
	private Set<Puller> pullers;

	@Autowired
	private KafkaRepository kafkaRepository;

	@Autowired
	private TopicConfigPollingService topicConfigPollingService;

	@Autowired
	private ApplicationConfiguration config;

	@Autowired
	private ApplicationContext context;

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

		logger.info("PullService starting ...");

		pullers = new HashSet<>();
		executorService = Executors.newCachedThreadPool();

		Iterable<Kafka> kafkas = kafkaRepository.findAll();
		for (Kafka kafka : kafkas) {
			if (kafka.isEnabled()) {
				doKafka(kafka);
			}
		}

		executorService.submit(topicConfigPollingService);
		
		isRunning = true;

		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
	}

	private void doKafka(Kafka kafka) {
		Puller puller = context.getBean(Puller.class, kafka);
		pullers.add(puller);
		for (int i = 0; i < kafka.getConsumerCount(); i++) {
			executorService.submit(puller);
		}
	}

	/**
	 * stop pulling
	 */
	public synchronized void shutdown() {
		if (!isRunning) {
			return;
		}

		config.getShutdownLock().writeLock().lock();
		try {
			logger.info("stop pulling ...");
			for (Puller puller : pullers) {
				puller.shutdown();
			}

			logger.info("stop executorService ...");
			executorService.shutdown();
			executorService.awaitTermination(120L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("shutdown(): executor.awaitTermination", e);
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			logger.error("shutdown error.", e);
		} finally {
			config.getShutdownLock().writeLock().unlock();
		}

		isRunning = false;
	}

}
