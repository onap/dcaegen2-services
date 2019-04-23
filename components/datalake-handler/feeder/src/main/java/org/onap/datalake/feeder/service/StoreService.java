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
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.tuple.Pair;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.enumeration.DataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Service to store messages to varieties of DBs
 * 
 * comment out YAML support, since AML is for config and don't see this data
 * type in DMaaP. Do we need to support XML?
 * 
 * @author Guobiao Mo
 *
 */
@Service
public class StoreService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApplicationConfiguration config;

	@Autowired
	private TopicService topicService;

	@Autowired
	private MongodbService mongodbService;

	@Autowired
	private CouchbaseService couchbaseService;

	@Autowired
	private ElasticsearchService elasticsearchService;

	private Map<String, Topic> topicMap = new HashMap<>();

	private ObjectMapper yamlReader;

	@PostConstruct
	private void init() {
		yamlReader = new ObjectMapper(new YAMLFactory());
	}

	@PreDestroy
	public void cleanUp() {
	}

	public void saveMessages(String topicStr, List<Pair<Long, String>> messages) {//pair=ts+text
		if (messages == null || messages.isEmpty()) {
			return;
		}

		Topic topic = topicMap.computeIfAbsent(topicStr, k -> { //TODO get topic updated settings from DB periodically
			return topicService.getEffectiveTopic(topicStr);
		});

		List<JSONObject> docs = new ArrayList<>();

		for (Pair<Long, String> pair : messages) {
			try {
				docs.add(messageToJson(topic, pair));
			} catch (Exception e) {
				log.error(pair.getRight(), e);
			}
		}

		saveJsons(topic, docs);
	}

	private JSONObject messageToJson(Topic topic, Pair<Long, String> pair) throws JSONException, JsonParseException, JsonMappingException, IOException {

		long timestamp = pair.getLeft();
		String text = pair.getRight();

		//for debug, to be remove
		//		String topicStr = topic.getId();
		//		if (!"TestTopic1".equals(topicStr) && !"msgrtr.apinode.metrics.dmaap".equals(topicStr) && !"AAI-EVENT".equals(topicStr) && !"unauthenticated.DCAE_CL_OUTPUT".equals(topicStr) && !"unauthenticated.SEC_FAULT_OUTPUT".equals(topicStr)) {
		//		log.debug("{} ={}", topicStr, text);
		//}

		boolean storeRaw = topic.isSaveRaw();

		JSONObject json = null;

		DataFormat dataFormat = topic.getDataFormat();

		switch (dataFormat) {
		case JSON:
			json = new JSONObject(text);
			break;
		case XML://XML and YAML can be directly inserted into ES, we may not need to convert it to JSON 
			json = XML.toJSONObject(text);
			break;
		case YAML:// Do we need to support YAML?
			Object obj = yamlReader.readValue(text, Object.class);
			ObjectMapper jsonWriter = new ObjectMapper();
			String jsonString = jsonWriter.writeValueAsString(obj);
			json = new JSONObject(jsonString);
			break;
		default:
			json = new JSONObject();
			storeRaw = true;
			break;
		}

		//FIXME for debug, to be remove
		json.remove("_id");
		json.remove("_dl_text_");
		json.remove("_dl_type_");

		json.put(config.getTimestampLabel(), timestamp);
		if (storeRaw) {
			json.put(config.getRawDataLabel(), text);
		}

		return json;
	}

	private void saveJsons(Topic topic, List<JSONObject> jsons) {
		if (topic.supportMongoDB()) {
			mongodbService.saveJsons(topic, jsons);
		}

		if (topic.supportCouchbase()) {
			couchbaseService.saveJsons(topic, jsons);
		}

		if (topic.supportElasticsearch()) {
			elasticsearchService.saveJsons(topic, jsons);
		}
	}

}
