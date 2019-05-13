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

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
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

	//get all topic names from Zookeeper
	public List<String> getTopics() {
		try {
			Watcher watcher = new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					// TODO monitor new topics

				}
			};
			ZooKeeper zk = new ZooKeeper(config.getDmaapZookeeperHostPort(), 10000, watcher);
			List<String> topics = zk.getChildren("/brokers/topics", false);
			String[] excludes = config.getDmaapKafkaExclude();
			topics.removeAll(Arrays.asList(excludes));
			return topics;
		} catch (Exception e) {
			log.error("Can not get topic list from Zookeeper, for testing, going to use hard coded topic list.", e);
			return Collections.emptyList();
		}
	}

	public List<String> getActiveTopics() throws IOException {
		List<String> allTopics = getTopics();
		if (allTopics == null) {
			return Collections.emptyList();
		}

		List<String> ret = new ArrayList<>(allTopics.size());
		for (String topicStr : allTopics) {
			TopicConfig topicConfig = topicService.getEffectiveTopic(topicStr, true);
			if (topicConfig.isEnabled()) {
				ret.add(topicStr);
			}
		}
		return ret;
	}

}
