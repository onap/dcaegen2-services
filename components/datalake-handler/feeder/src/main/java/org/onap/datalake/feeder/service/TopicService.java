/*
* ============LICENSE_START=======================================================
* ONAP : DATALAKE
* ================================================================================
* Copyright 2019-2020 China Mobile
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.dto.TopicConfig;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.EffectiveTopic;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.domain.TopicName;
import org.onap.datalake.feeder.repository.DbRepository;
import org.onap.datalake.feeder.repository.KafkaRepository;
import org.onap.datalake.feeder.repository.TopicNameRepository;
import org.onap.datalake.feeder.repository.TopicRepository;
import org.onap.datalake.feeder.service.db.ElasticsearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for topics
 * 
 * @author Guobiao Mo
 *
 */
@Service
public class TopicService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApplicationConfiguration config;

	@Autowired
	private TopicNameRepository topicNameRepository;

	@Autowired
	private TopicRepository topicRepository;

	@Autowired
	private DbRepository dbRepository;

	@Autowired
	private DbService dbService;

	@Autowired
	private KafkaRepository kafkaRepository;
	
	public List<EffectiveTopic> getEnabledEffectiveTopic(Kafka kafka, String topicStr, boolean ensureTableExist) throws IOException {

		List<Topic> topics = findTopics(kafka, topicStr);
		if (CollectionUtils.isEmpty(topics)) {
			topics = new ArrayList<>();
			topics.add(getDefaultTopic(kafka));
		}

		List<EffectiveTopic> ret = new ArrayList<>();
		for (Topic topic : topics) {
			if (!topic.isEnabled()) {
				continue;
			}
			ret.add(new EffectiveTopic(topic, topicStr));

			if (ensureTableExist) {
				for (Db db : topic.getDbs()) {
					if (db.isElasticsearch()) {
						ElasticsearchService elasticsearchService = (ElasticsearchService) dbService.findDbStoreService(db);						
						elasticsearchService.ensureTableExist(topicStr);
					}
				}
			}
		}

		return ret;
	}

	// for unique topic string, one can create multiple 'topic' in admin UI.
	// for example, one 'topic' setting correlates events, and sends data to ES, another 'topic' sends data to HDFS without such setting
	//TODO use query
	public List<Topic> findTopics(Kafka kafka, String topicStr) {
		List<Topic> ret = new ArrayList<>();
		
		Iterable<Topic> allTopics = topicRepository.findAll();
		for(Topic topic: allTopics) {
			if(topic.getKafkas().contains(kafka ) && topic.getTopicName().getId().equals(topicStr)){
				ret.add(topic);
			}
		}
		return ret;
	}

	public Topic getTopic(int topicId) {
		Optional<Topic> ret = topicRepository.findById(topicId);
		return ret.isPresent() ? ret.get() : null;
	}

	public Topic getDefaultTopicFromFeeder() {
		return topicRepository.findByTopicName_Id(config.getDefaultTopicName());
	}

	public Topic getDefaultTopic(Kafka kafka) {
		return findTopics(kafka, config.getDefaultTopicName()).get(0);
	}

	public boolean isDefaultTopic(Topic topic) {
		if (topic == null) {
			return false;
		}
		return topic.getName().equals(config.getDefaultTopicName());
	}

	public void fillTopicConfiguration(TopicConfig tConfig, Topic wTopic) {
		fillTopic(tConfig, wTopic);
	}

	public Topic fillTopicConfiguration(TopicConfig tConfig) {
		Topic topic = new Topic();
		fillTopic(tConfig, topic);
		return topic;
	}

	private void fillTopic(TopicConfig tConfig, Topic topic) {
		Set<Db> relateDb = new HashSet<>();
		topic.setId(tConfig.getId());
		Optional<TopicName> t = topicNameRepository.findById(tConfig.getName());
		if (!t.isPresent())
			throw new IllegalArgumentException("Can not find topicName in TopicName, topic name " + tConfig.getName());
		topic.setTopicName(t.get());
		topic.setLogin(tConfig.getLogin());
		topic.setPass(tConfig.getPassword());
		topic.setEnabled(tConfig.isEnabled());
		topic.setSaveRaw(tConfig.isSaveRaw());
		topic.setTtl(tConfig.getTtl());
		topic.setCorrelateClearedMessage(tConfig.isCorrelateClearedMessage());
		topic.setDataFormat(tConfig.getDataFormat());
		topic.setMessageIdPath(tConfig.getMessageIdPath());
		topic.setAggregateArrayPath(tConfig.getAggregateArrayPath());
		topic.setFlattenArrayPath(tConfig.getFlattenArrayPath());

		if (tConfig.getSinkdbs() != null) {
			for (int item : tConfig.getSinkdbs()) {
				Optional<Db> sinkdb = dbRepository.findById(item);
				if (sinkdb.isPresent()) {
					relateDb.add(sinkdb.get());
				}
			}
			if (!relateDb.isEmpty())
				topic.setDbs(relateDb);
			else {
				topic.getDbs().clear();
			}
		} else {
			topic.setDbs(relateDb);
		}

		Set<Kafka> relateKafka = new HashSet<>();
		if (tConfig.getKafkas() != null) {
			for (int item : tConfig.getKafkas()) {
				Optional<Kafka> sinkKafka = kafkaRepository.findById(item);
				if (sinkKafka.isPresent()) {
					relateKafka.add(sinkKafka.get());
				}
			}
			if (!relateKafka.isEmpty()) {
				topic.setKafkas(relateKafka);
			} else {
				topic.getKafkas().clear();
			}
		} else {
			topic.setKafkas(relateKafka);
		}
	}

}
