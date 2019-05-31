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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.dto.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to check topic changes in Kafka and topic setting updates
 * 
 * @author Guobiao Mo
 *
 */
@Service
public class TopicConfigPollingService implements Runnable {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ApplicationConfiguration config;

	@Autowired
	private DmaapService dmaapService;

	//effective TopicConfig Map
	private Map<String, TopicConfig> effectiveTopicConfigMap = new HashMap<>();

	//monitor Kafka topic list changes
	private List<String> activeTopics;
	private ThreadLocal<Integer> activeTopicsVersionLocal = ThreadLocal.withInitial(() -> -1);
	private int currentActiveTopicsVersion = -1;

	private boolean active = false;

	@PostConstruct
	private void init() {
		try {
			log.info("init(), ccalling poll()...");
			activeTopics = poll();
			currentActiveTopicsVersion++;
		} catch (Exception ex) {
			log.error("error connection to HDFS.", ex);
		}
	}

	public boolean isActiveTopicsChanged(boolean update) {
		boolean changed = currentActiveTopicsVersion > activeTopicsVersionLocal.get();
		log.debug("isActiveTopicsChanged={}, currentActiveTopicsVersion={} local={}", changed, currentActiveTopicsVersion, activeTopicsVersionLocal.get());
		if (changed && update) {
			activeTopicsVersionLocal.set(currentActiveTopicsVersion);
		}

		return changed;
	}

	public List<String> getActiveTopics() {
		return activeTopics;
	}

	public TopicConfig getEffectiveTopicConfig(String topicStr) {
		return effectiveTopicConfigMap.get(topicStr);
	}

	@Override
	public void run() {
		active = true;
		log.info("TopicConfigPollingService started.");
		
		while (active) {
			try { //sleep first since we already pool in init()
				Thread.sleep(config.getDmaapCheckNewTopicInterval());
			} catch (InterruptedException e) {
				log.error("Thread.sleep(config.getDmaapCheckNewTopicInterval())", e);
				Thread.currentThread().interrupt();
			}

			try {
				List<String> newTopics = poll();
				if (!CollectionUtils.isEqualCollection(activeTopics, newTopics)) {
					log.info("activeTopics list is updated, old={}", activeTopics);
					log.info("activeTopics list is updated, new={}", newTopics);

					activeTopics = newTopics;
					currentActiveTopicsVersion++;
				} else {
					log.debug("activeTopics list is not updated.");
				}
			} catch (IOException e) {
				log.error("dmaapService.getActiveTopics()", e);
			}
		}

		log.info("exit since active is set to false");
	}

	public void shutdown() {
		active = false;
	}

	private List<String> poll() throws IOException {
		log.debug("poll(), use dmaapService to getActiveTopicConfigs...");
		List<TopicConfig> activeTopicConfigs = dmaapService.getActiveTopicConfigs();
		activeTopicConfigs.stream().forEach(topicConfig -> effectiveTopicConfigMap.put(topicConfig.getName(), topicConfig));

		List<String> ret = new ArrayList<>(activeTopicConfigs.size());
		activeTopicConfigs.stream().forEach(topicConfig -> ret.add(topicConfig.getName()));

		return ret;
	}

}
