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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.json.XML;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.EffectiveTopic;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.enumeration.DataFormat;
import org.onap.datalake.feeder.service.db.DbStoreService;
import org.onap.datalake.feeder.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	private DbService dbService;

	@Autowired
	private TopicConfigPollingService configPollingService;

	private ObjectMapper yamlReader;

	@PostConstruct
	private void init() {
		yamlReader = new ObjectMapper(new YAMLFactory());
	}

	public void saveMessages(Kafka kafka, String topicStr, List<Pair<Long, String>> messages) {//pair=ts+text
		if (CollectionUtils.isEmpty(messages)) {
			return;
		}

		Collection<EffectiveTopic> effectiveTopics = configPollingService.getEffectiveTopic(kafka, topicStr);
		for (EffectiveTopic effectiveTopic : effectiveTopics) {
			saveMessagesForTopic(effectiveTopic, messages);
		}
	}

	private void saveMessagesForTopic(EffectiveTopic effectiveTopic, List<Pair<Long, String>> messages) {
		if (!effectiveTopic.getTopic().isEnabled()) {
			log.error("we should not come here {}", effectiveTopic);
			return;
		}

		List<JSONObject> docs = new ArrayList<>();

		for (Pair<Long, String> pair : messages) {
			try {
				docs.add(messageToJson(effectiveTopic, pair));
			} catch (Exception e) {
				//may see org.json.JSONException.
				log.error("Error when converting this message to JSON: " + pair.getRight(), e);
			}
		}

		Set<Db> dbs = effectiveTopic.getTopic().getDbs();

		for (Db db : dbs) {
			if (db.isTool() || db.isDruid() || !db.isEnabled()) {
				continue;
			}
			DbStoreService dbStoreService = dbService.findDbStoreService(db);
			if (dbStoreService != null) {
				dbStoreService.saveJsons(effectiveTopic, docs);
			}
		}
	}

	private JSONObject messageToJson(EffectiveTopic effectiveTopic, Pair<Long, String> pair) throws IOException {

		long timestamp = pair.getLeft();
		String text = pair.getRight();

		boolean storeRaw = effectiveTopic.getTopic().isSaveRaw();

		JSONObject json = null;

		DataFormat dataFormat = effectiveTopic.getTopic().getDataFormat2();

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

		if (StringUtils.isNotBlank(effectiveTopic.getTopic().getAggregateArrayPath())) {
			String[] paths = effectiveTopic.getTopic().getAggregateArrayPath2();
			for (String path : paths) {
				JsonUtil.arrayAggregate(path, json);
			}
		}

		if (StringUtils.isNotBlank(effectiveTopic.getTopic().getFlattenArrayPath())) {
			String[] paths = effectiveTopic.getTopic().getFlattenArrayPath2();
			for (String path : paths) {
				JsonUtil.flattenArray(path, json);
			}
		}

		return json;
	}

	public void flush() { //force flush all buffer 
		//		hdfsService.flush();
	}

	public void flushStall() { //flush stall buffer
		//	hdfsService.flushStall();
	}
}
