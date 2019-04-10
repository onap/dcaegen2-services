/*
* ============LICENSE_START=======================================================
* ONAP : DATALAKE
* ================================================================================
* Copyright 2018 China Mobile
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.bson.Document;

import org.json.JSONObject;

import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Service to use MongoDB
 * 
 * @author Guobiao Mo
 *
 */
@Service
public class MongodbService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DbService dbService;

	private MongoDatabase database;
	private MongoClient mongoClient;
	private Map<String, MongoCollection<Document>> mongoCollectionMap = new HashMap<>();

	@PostConstruct
	private void init() {
		Db mongodb = dbService.getMongoDB();

		mongoClient = new MongoClient(mongodb.getHost(), mongodb.getPort());
		database = mongoClient.getDatabase(mongodb.getProperty1());
	}

	@PreDestroy
	public void cleanUp() {
		mongoClient.close();
	}

	public void saveJsons(Topic topic, List<JSONObject> jsons) {
		List<Document> documents = new ArrayList<>(jsons.size());
		for (JSONObject json : jsons) {
			//convert org.json JSONObject to MongoDB Document
			Document doc = Document.parse(json.toString());

			String id = topic.getMessageId(json); //id can be null
			if (id != null) {
				doc.put("_id", id);
			}
			documents.add(doc);
		}

		String collectionName = topic.getName().replaceAll("[^a-zA-Z0-9]","");//remove - _ .
		MongoCollection<Document> collection = mongoCollectionMap.computeIfAbsent(collectionName, k -> database.getCollection(k));
		collection.insertMany(documents);

		log.debug("saved text to topic = {}, topic total count = {} ", topic, collection.countDocuments());
	}

}
