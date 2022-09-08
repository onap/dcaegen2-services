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

package org.onap.datalake.feeder.service.db;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.json.JSONObject;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.EffectiveTopic;
import org.onap.datalake.feeder.domain.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;

import rx.Observable;
import rx.functions.Func1;

/**
 * Service to use Couchbase
 * 
 * @author Guobiao Mo
 *
 */
@Service
@Scope("prototype")
public class CouchbaseService implements DbStoreService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ApplicationConfiguration config;
	
	private Db couchbase;
	//Bucket is thread-safe. https://docs.couchbase.com/java-sdk/current/managing-connections.html
	Bucket bucket;
	
	public CouchbaseService(Db db) {
		couchbase = db;
	}
	
	@PostConstruct
	@Override
	public void init() {
		// Initialize Couchbase Connection
		try {
			//this tunes the SDK (to customize connection timeout)
			CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder().connectTimeout(60000) // 60s, default is 5s
					.build();
			Cluster cluster = CouchbaseCluster.create(env, couchbase.getHost());
			cluster.authenticate(couchbase.getLogin(), couchbase.getPass());
			bucket = cluster.openBucket(couchbase.getDatabase());
			// Create a N1QL Primary Index (but ignore if it exists)
			bucket.bucketManager().createN1qlPrimaryIndex(true, false);

			log.info("Connected to Couchbase {} as {}", couchbase.getHost(), couchbase.getLogin());
//			isReady = true;
		} catch (Exception ex) {
			log.error("error connection to Couchbase.", ex);
	//		isReady = false;
		}
	}

	@PreDestroy
	public void cleanUp() {
		config.getShutdownLock().readLock().lock();

		try {
			log.info("bucket.close() at cleanUp.");
			bucket.close();
		} finally {
			config.getShutdownLock().readLock().unlock();
		}
	}

	@Override
	public void saveJsons(EffectiveTopic effectiveTopic, List<JSONObject> jsons) {
		List<JsonDocument> documents = new ArrayList<>(jsons.size());
		for (JSONObject json : jsons) {
			//convert to Couchbase JsonObject from org.json JSONObject
			JsonObject jsonObject = JsonObject.fromJson(json.toString());

			long timestamp = jsonObject.getLong(config.getTimestampLabel());//this is Kafka time stamp, which is added in StoreService.messageToJson()

			//setup TTL
			int expiry = (int) (timestamp / 1000L) + effectiveTopic.getTopic().getTtl() * 3600 * 24; //in second

			String id = getId(effectiveTopic.getTopic(), json);
			JsonDocument doc = JsonDocument.create(id, expiry, jsonObject);
			documents.add(doc);
		}
		try {
			saveDocuments(documents);
		} catch (DocumentAlreadyExistsException e) {
			log.error("Some or all the following ids are duplicate.");
			for(JsonDocument document : documents) {
				log.error("saveJsons() DocumentAlreadyExistsException {}", document.id());
			}
		} catch (rx.exceptions.CompositeException e) {
			List<Throwable> causes = e.getExceptions();
			for(Throwable cause : causes) {
				log.error("saveJsons() CompositeException cause {}", cause.getMessage());
			}			
		} catch (Exception e) {
			log.error("error saving to Couchbase.", e);
		}
		log.debug("saved text to topic = {}, this batch count = {} ", effectiveTopic, documents.size());
	}

	public String getId(Topic topic, JSONObject json) {
		//if this topic requires extract id from JSON
		String id = topic.getMessageId(json);
		if (id != null) {
			return id;
		}

		String topicStr = topic.getName();
		id = topicStr+":"+UUID.randomUUID();

		//https://forums.couchbase.com/t/how-to-set-an-auto-increment-id/4892/2
		//atomically get the next sequence number:
		// increment by 1, initialize at 0 if counter doc not found
		//TODO how slow is this compared with above UUID approach?
		//sometimes this gives java.util.concurrent.TimeoutException
		//JsonLongDocument nextIdNumber = bucket.counter(topicStr, 1, 0); //like 12345 
		//id = topicStr + ":" + nextIdNumber.content();

		return id;
	}

	//https://docs.couchbase.com/java-sdk/2.7/document-operations.html
	private void saveDocuments(List<JsonDocument> documents) {
		Observable.from(documents).flatMap(new Func1<JsonDocument, Observable<JsonDocument>>() {
			@Override
			public Observable<JsonDocument> call(final JsonDocument docToInsert) {
				return bucket.async().insert(docToInsert);
			}
		}).last().toBlocking().single();
	}

}
