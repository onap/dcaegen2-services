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
import java.util.Optional;
import java.util.Set;

import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.dto.TopicConfig;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.repository.DbRepository;
import org.onap.datalake.feeder.repository.TopicRepository;
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
	private TopicRepository topicRepository;

	@Autowired
	private ElasticsearchService elasticsearchService;


	@Autowired
	private DbRepository dbRepository;

	public TopicConfig getEffectiveTopic(String topicStr) {
		try {
			return getEffectiveTopic(topicStr, false);
		} catch (IOException e) {
			log.error(topicStr, e);
		}
		return null;
	}

	public TopicConfig getEffectiveTopic(String topicStr, boolean ensureTableExist) throws IOException {
		Topic topic = getTopic(topicStr);
		if (topic == null) {
			topic = getDefaultTopic();
		}
		TopicConfig topicConfig = topic.getTopicConfig();
		topicConfig.setName(topicStr);//need to change name if it comes from DefaultTopic
		
		if(ensureTableExist && topicConfig.isEnabled() && topicConfig.supportElasticsearch()) {
			elasticsearchService.ensureTableExist(topicStr); 
		}
		return topicConfig;
	}

	public Topic getTopic(String topicStr) {
		Optional<Topic> ret = topicRepository.findById(topicStr);
		return ret.isPresent() ? ret.get() : null;
	}

	public Topic getDefaultTopic() {
		return getTopic(config.getDefaultTopicName());
	}

	public boolean istDefaultTopic(Topic topic) {
		if (topic == null) {
			return false;
		}
		return topic.getName().equals(config.getDefaultTopicName());
	}

	public void fillTopicConfiguration(TopicConfig tConfig, Topic wTopic)
	{
		fillTopic(tConfig, wTopic);
	}

	public Topic fillTopicConfiguration(TopicConfig tConfig)
	{
		Topic topic = new Topic();
		fillTopic(tConfig, topic);
		return topic;
	}

	private void fillTopic(TopicConfig tConfig, Topic topic)
	{
		Set<Db> relateDb = new HashSet<>();
		topic.setName(tConfig.getName());
		topic.setLogin(tConfig.getLogin());
		topic.setPass(tConfig.getPassword());
		topic.setEnabled(tConfig.isEnabled());
		topic.setSaveRaw(tConfig.isSaveRaw());
		topic.setTtl(tConfig.getTtl());
		topic.setCorrelateClearedMessage(tConfig.isCorrelateClearedMessage());
		topic.setDataFormat(tConfig.getDataFormat());
		topic.setMessageIdPath(tConfig.getMessageIdPath());

		if(tConfig.getSinkdbs() != null) {
			for (String item : tConfig.getSinkdbs()) {
				Db sinkdb = dbRepository.findByName(item);
				if (sinkdb != null) {
					relateDb.add(sinkdb);
				}
			}
			if(relateDb.size() > 0)
				topic.setDbs(relateDb);
			else if(relateDb.size() == 0)
			{
				topic.getDbs().clear();
			}
		}else
		{
			topic.setDbs(relateDb);
		}

	}

}
