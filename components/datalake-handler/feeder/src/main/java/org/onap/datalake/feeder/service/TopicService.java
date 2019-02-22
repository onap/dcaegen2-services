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
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.repository.TopicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for topics topic setting is stored in Couchbase, bucket 'dl', see
 * application.properties for Spring setup
 * 
 * @author Guobiao Mo
 *
 */
@Service
public class TopicService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TopicRepository topicRepository;

	@Autowired
	private ElasticsearchService elasticsearchService;
	
	@PostConstruct
	private void init() {
	}

	@PreDestroy
	public void cleanUp() {
	}

	public Topic getEffectiveTopic(String topicStr) {
		try {
			return getEffectiveTopic(topicStr, false);
		} catch (IOException e) {
			log.error(topicStr, e);
		}
		return null;
	}
		
	public Topic getEffectiveTopic(String topicStr, boolean ensureTableExist) throws IOException {
		Topic topic = getTopic(topicStr);
		if (topic == null) {
			topic = new Topic(topicStr);
		}

		topic.setDefaultTopic(getDefaultTopic());
		
		if(ensureTableExist && topic.isEnabled() && topic.isSupportElasticsearch()) { 
			elasticsearchService.ensureTableExist(topicStr); 
		}
		return topic;
	}

	public Topic getTopic(String topicStr) {
		Optional<Topic> ret = topicRepository.findById(topicStr);
		return ret.isPresent() ? ret.get() : null;
	}

	public Topic getDefaultTopic() {
		return getTopic("_DL_DEFAULT_");
	}

}
