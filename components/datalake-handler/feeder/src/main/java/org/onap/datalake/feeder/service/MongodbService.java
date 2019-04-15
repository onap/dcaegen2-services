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

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import org.json.JSONObject;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Service for using MongoDB
 * 
 * @author Guobiao Mo
 *
 */
@Service
public class MongodbService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApplicationConfiguration config;

	@Autowired
	private DbService dbService;

	private MongoDatabase database;
	private MongoClient mongoClient;
	private Map<String, MongoCollection<Document>> mongoCollectionMap = new HashMap<>();

	@PostConstruct
	private void init() {
		Db mongodb = dbService.getMongoDB();

		String host = mongodb.getHost();

		Integer port = mongodb.getPort();
		if (port == null || port == 0) {
			port = 27017; //MongoDB default
		}

		String databaseName = mongodb.getDatabase();
		String userName = mongodb.getLogin();
		String password = mongodb.getPass();

		MongoCredential credential = null;
		if (StringUtils.isNoneBlank(userName) && StringUtils.isNoneBlank(password)) {
			credential = MongoCredential.createCredential(userName, databaseName, password.toCharArray());
		}

		Builder builder = MongoClientOptions.builder();
		builder.serverSelectionTimeout(30000);//server selection timeout, in milliseconds

		//http://mongodb.github.io/mongo-java-driver/3.0/driver/reference/connecting/ssl/
		if (config.isEnableSSL()) {
			builder.sslEnabled(Boolean.TRUE.equals(mongodb.getEncrypt()));// getEncrypt() can be null
		}
		MongoClientOptions options = builder.build();

		if (credential == null) {
			mongoClient = new MongoClient(new ServerAddress(host, port), options);
		} else {
			mongoClient = new MongoClient(new ServerAddress(host, port), credential, options);
		}
		database = mongoClient.getDatabase(databaseName);
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

		String collectionName = topic.getName().replaceAll("[^a-zA-Z0-9]", "");//remove - _ .
		MongoCollection<Document> collection = mongoCollectionMap.computeIfAbsent(collectionName, k -> database.getCollection(k));
		collection.insertMany(documents);

		log.debug("saved text to topic = {}, topic total count = {} ", topic, collection.countDocuments());
	}

}
