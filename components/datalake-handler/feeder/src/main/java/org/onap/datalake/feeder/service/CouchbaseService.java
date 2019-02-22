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
 
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
 
import org.json.JSONObject;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.document.json.JsonObject; 

import rx.Observable;
import rx.functions.Func1;

/**
 * Service to use Couchbase
 * 
 * @author Guobiao Mo
 *
 */
@Service
public class CouchbaseService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApplicationConfiguration config;

	Bucket bucket;		

	@PostConstruct
	private void init() {
        // Initialize Couchbase Connection
        Cluster cluster = CouchbaseCluster.create(config.getCouchbaseHost());
        cluster.authenticate(config.getCouchbaseUser(), config.getCouchbasePass());
        bucket = cluster.openBucket(config.getCouchbaseBucket());

		log.info("Connect to Couchbase " + config.getCouchbaseHost());
		
        // Create a N1QL Primary Index (but ignore if it exists)
        bucket.bucketManager().createN1qlPrimaryIndex(true, false);                 
	}

	@PreDestroy
	public void cleanUp() { 
		bucket.close();
	} 

	public void saveJsons(Topic topic, List<JSONObject> jsons) { 
		List<JsonDocument> documents= new ArrayList<>(jsons.size());
		for(JSONObject json : jsons) {
			//convert to Couchbase JsonObject from org.json JSONObject
			JsonObject jsonObject = JsonObject.fromJson(json.toString());	

			long timestamp = jsonObject.getLong("_ts");//this is Kafka time stamp, which is added in StoreService.messageToJson()

			//setup TTL
			int expiry = (int) (timestamp/1000L) + topic.getTtl()*3600*24; //in second
			
			String id = getId(topic.getId());
			JsonDocument doc = JsonDocument.create(id, expiry, jsonObject);
			documents.add(doc);
		}
		saveDocuments(documents);		
	}


	private String getId(String topicStr) {
		//String id = topicStr+":"+timestamp+":"+UUID.randomUUID();

		//https://forums.couchbase.com/t/how-to-set-an-auto-increment-id/4892/2
		//atomically get the next sequence number:
		// increment by 1, initialize at 0 if counter doc not found
		//TODO how slow is this compared with above UUID approach?
		JsonLongDocument nextIdNumber = bucket.counter(topicStr, 1, 0); //like 12345 
		String id = topicStr +":"+ nextIdNumber.content();
		
		return id;
	}
	 
	//https://docs.couchbase.com/java-sdk/2.7/document-operations.html
	private void saveDocuments(List<JsonDocument> documents) { 
		Observable
	    .from(documents)
	    .flatMap(new Func1<JsonDocument, Observable<JsonDocument>>() {
	        @Override
	        public Observable<JsonDocument> call(final JsonDocument docToInsert) {
	            return bucket.async().insert(docToInsert);
	        }
	    })
	    .last()
	    .toBlocking()
	    .single();		
	}

}
