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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.EffectiveTopic;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.repository.KafkaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Service to check topic changes in Kafka and topic setting updates in DB
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
	private ApplicationContext context;

	@Autowired
	private KafkaRepository kafkaRepository;
	
	@Autowired
	private TopicNameService topicNameService;
	
	//effectiveTopic Map, 1st key is kafkaId, 2nd is topic name, the value is a list of EffectiveTopic.
	private Map<Integer, Map<String, List<EffectiveTopic>>> effectiveTopicMap = new HashMap<>();
	//private Map<String, TopicConfig> effectiveTopicConfigMap;

	//monitor Kafka topic list changes, key is kafka id, value is active Topics
	private Map<Integer, Set<String>> activeTopicMap;
	
	private ThreadLocal<Map<Integer, Integer>> activeTopicsVersionLocal =   ThreadLocal.withInitial(HashMap::new);//kafkaId:version - local 'old' version
	private Map<Integer, Integer> currentActiveTopicsVersionMap = new HashMap<>();//kafkaId:version - current/latest version
	private Map<Integer, DmaapService> dmaapServiceMap = new HashMap<>();//kafka id:DmaapService

	private boolean active = false;

	@PostConstruct
	private void init() {
		try {
			log.info("init(), calling poll()...");
			activeTopicMap = poll();
		} catch (Exception ex) {
			log.error("error connection to HDFS.", ex);
		}
	}

	public boolean isActiveTopicsChanged(Kafka kafka) {//update=true means sync local version
		int kafkaId = kafka.getId();
		int currentActiveTopicsVersion = currentActiveTopicsVersionMap.getOrDefault(kafkaId, 1);//init did one version
		int localActiveTopicsVersion = activeTopicsVersionLocal.get().getOrDefault(kafkaId, 0);
		
		boolean changed = currentActiveTopicsVersion > localActiveTopicsVersion;
		log.debug("kafkaId={} isActiveTopicsChanged={}, currentActiveTopicsVersion={} local={}", kafkaId, changed, currentActiveTopicsVersion, localActiveTopicsVersion);
		if (changed) {
			activeTopicsVersionLocal.get().put(kafkaId, currentActiveTopicsVersion);
		}

		return changed;
	}

	//get a list of topic names to monitor
	public Collection<String> getActiveTopics(Kafka kafka) {
		return activeTopicMap.get(kafka.getId());
	}

	//get the EffectiveTopics given kafka and topic name
	public Collection<EffectiveTopic> getEffectiveTopic(Kafka kafka, String topicStr) {
		Map<String, List<EffectiveTopic>> effectiveTopicMapKafka= effectiveTopicMap.get(kafka.getId());  
		return effectiveTopicMapKafka.get(topicStr);
	}

	@Override
	public void run() {
		active = true;
		log.info("TopicConfigPollingService started.");

		while (active) {
			try { //sleep first since we already called poll() in init()
				Thread.sleep(config.getCheckTopicInterval());
				if(!active) {
					break;
				}
			} catch (InterruptedException e) {
				log.error("Thread.sleep(config.getDmaapCheckNewTopicInterval())", e);
				Thread.currentThread().interrupt();
			}

			try {
				Map<Integer, Set<String>> newTopicsMap = poll();
				
				for(Map.Entry<Integer, Set<String>> entry:newTopicsMap.entrySet()) {
					Integer kafkaId = entry.getKey();
					Set<String>  newTopics = entry.getValue();
					
					Set<String> activeTopics = activeTopicMap.get(kafkaId);

					if (!CollectionUtils.isEqualCollection(activeTopics, newTopics)) {
						log.info("activeTopics list is updated, old={}", activeTopics);
						log.info("activeTopics list is updated, new={}", newTopics);

						activeTopicMap.put(kafkaId, newTopics);
						//update version
						currentActiveTopicsVersionMap.put(kafkaId, currentActiveTopicsVersionMap.getOrDefault(kafkaId, 1)+1);
					} else {
						log.debug("activeTopics list is not updated.");
					}
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

	private Map<Integer, Set<String>>  poll() throws IOException {
		Set<String> allTopicNames = new HashSet<>();
		
		Map<Integer, Set<String>> ret = new HashMap<>();
		Iterable<Kafka> kafkas = kafkaRepository.findAll();
		for (Kafka kafka : kafkas) {
			if (kafka.isEnabled()) {
				Set<String> topics = poll(kafka);
				ret.put(kafka.getId(), topics);
				allTopicNames.addAll(topics);
			}
		}
		
		topicNameService.update(allTopicNames);
		
		return ret;
	}

	private Set<String> poll(Kafka kafka) throws IOException {
		log.debug("poll(), use dmaapService to getActiveTopicConfigs...");

		DmaapService dmaapService =  dmaapServiceMap.get(kafka.getId());
		if(dmaapService==null) {
			dmaapService = context.getBean(DmaapService.class, kafka);
			dmaapServiceMap.put(kafka.getId(), dmaapService);
		}
				
		Map<String, List<EffectiveTopic>> activeEffectiveTopics = dmaapService.getActiveEffectiveTopic();
		effectiveTopicMap.put(kafka.getId(), activeEffectiveTopics);

		Set<String> ret = activeEffectiveTopics.keySet(); 

		return ret;
	}

}
