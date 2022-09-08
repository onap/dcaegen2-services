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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.collections.CollectionUtils;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.EffectiveTopic;
import org.onap.datalake.feeder.domain.Kafka;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * This service will handle all the communication with Kafka
 * 
 * @author Guobiao Mo
 *
 */
@Service
@Scope("prototype")
public class DmaapService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApplicationConfiguration config;

	@Autowired
	private TopicService topicService;

	private ZooKeeper zk;

	private Kafka kafka;

	public DmaapService(Kafka kafka) {
		this.kafka = kafka;
	}

	@PreDestroy
	public void cleanUp() throws InterruptedException {
		config.getShutdownLock().readLock().lock();

		try {
			if (zk != null) {
				log.info("cleanUp() called, close zk.");
				zk.close();
			}
		} finally {
			config.getShutdownLock().readLock().unlock();
		}
	}

	@PostConstruct
	private void init() throws IOException, InterruptedException {
		zk = connect(kafka.getZooKeeper());
	}

	//get all topic names from Zookeeper
	//This method returns empty list if nothing found.
	public List<String> getTopics() {
		try {
			if (zk == null) {
				zk = connect(kafka.getZooKeeper());
			}
			log.info("connecting to ZooKeeper {} for a list of topics.", kafka.getZooKeeper());
			List<String> topics = zk.getChildren("/brokers/topics", false);
			String[]  excludes = kafka.getExcludedTopic().split(",");
			topics.removeAll(Arrays.asList(excludes));
			log.info("list of topics: {}", topics);
			return topics;
		} catch (Exception e) {
			zk = null;
			log.error("Can not get topic list from Zookeeper, return empty list.", e);
			return Collections.emptyList();
		}
	}

	private ZooKeeper connect(String host) throws IOException, InterruptedException {
		log.info("connecting to ZooKeeper {} ...", kafka.getZooKeeper());
		CountDownLatch connectedSignal = new CountDownLatch(1);
		ZooKeeper ret = new ZooKeeper(host, 10000, new Watcher() {
			public void process(WatchedEvent we) {
				if (we.getState() == KeeperState.SyncConnected) {
					connectedSignal.countDown();
				}
			}
		});

		connectedSignal.await();
		return ret;
	}

	/*
		public List<String> getActiveTopics() throws IOException {
			log.debug("entering getActiveTopics()...");
	
			List<TopicConfig> configList = getActiveTopicConfigs();
	
			List<String> ret = new ArrayList<>(configList.size());
			configList.stream().forEach(topicConfig -> ret.add(topicConfig.getName()));
	
			return ret;
		}
	*/
	public Map<String, List<EffectiveTopic>> getActiveEffectiveTopic() throws IOException {
		log.debug("entering getActiveTopicConfigs()...");
		List<String> allTopics = getTopics(); //topics in Kafka cluster TODO update table topic_name with new topics

		Map<String, List<EffectiveTopic>> ret = new HashMap<>();
		for (String topicStr : allTopics) {
			log.debug("get topic setting from DB: {}.", topicStr);

			List<EffectiveTopic> effectiveTopics= topicService.getEnabledEffectiveTopic(kafka, topicStr, true);
			if(CollectionUtils.isNotEmpty(effectiveTopics )) {
				log.debug("add effectiveTopics  {}:{}.", topicStr, effectiveTopics);
				ret.put(topicStr , effectiveTopics);
			}
			
		}
		return ret;
	}

}
