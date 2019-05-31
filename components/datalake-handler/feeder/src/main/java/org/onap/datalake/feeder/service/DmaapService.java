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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.dto.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service will handle all the communication with Kafka
 * 
 * @author Guobiao Mo
 *
 */
@Service
public class DmaapService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApplicationConfiguration config;

	@Autowired
	private TopicService topicService;

	private ZooKeeper zk;

	@PreDestroy
	public void cleanUp() throws InterruptedException {
		if (zk != null) {
			zk.close();
		}
	}

	@PostConstruct
	private void init() throws IOException, InterruptedException {
		zk = connect(config.getDmaapZookeeperHostPort());
	}

	//get all topic names from Zookeeper
	//This method returns empty list if nothing found.
	public List<String> getTopics() {
		try {
			if (zk == null) {
				zk = connect(config.getDmaapZookeeperHostPort());
			}
			log.info("connecting to ZooKeeper {} for a list of topics.", config.getDmaapZookeeperHostPort());
			List<String> topics = zk.getChildren("/brokers/topics", false);
			String[] excludes = config.getDmaapKafkaExclude();
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
		log.info("connecting to ZooKeeper {} ...", config.getDmaapZookeeperHostPort());
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
	public List<TopicConfig> getActiveTopicConfigs() throws IOException {
		log.debug("entering getActiveTopicConfigs()...");
		List<String> allTopics = getTopics();

		List<TopicConfig> ret = new ArrayList<>(allTopics.size());
		for (String topicStr : allTopics) {
			log.debug("get topic setting from DB: {}.", topicStr);

			TopicConfig topicConfig = topicService.getEffectiveTopic(topicStr, true);
			if (topicConfig.isEnabled()) {
				ret.add(topicConfig);
			}
		}
		return ret;
	}

}
