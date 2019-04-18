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
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest; 
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.JsonObject;
import org.onap.datalake.feeder.domain.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to use Elasticsearch
 * 
 * @author Guobiao Mo
 *
 */
@Service
public class ElasticsearchService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApplicationConfiguration config;
	
	@Autowired
	private DbService dbService;

	private RestHighLevelClient client;
	ActionListener<BulkResponse> listener;


//ES Encrypted communication https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/_encrypted_communication.html#_encrypted_communication
//Basic authentication https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/_basic_authentication.html
	@PostConstruct
	private void init() {
		Db elasticsearch = dbService.getElasticsearch();
		String elasticsearchHost = elasticsearch.getHost();
		
		// Initialize the Connection
		client = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticsearchHost, 9200, "http"), new HttpHost(elasticsearchHost, 9201, "http")));

		log.info("Connect to Elasticsearch Host {}", elasticsearchHost);

		listener = new ActionListener<BulkResponse>() {
			@Override
			public void onResponse(BulkResponse bulkResponse) {

			}

			@Override
			public void onFailure(Exception e) {
				log.error(e.getMessage());
			}
		};
	}

	@PreDestroy
	public void cleanUp() throws IOException {
		client.close();
	}
	
	public void ensureTableExist(String topic) throws IOException {
		String topicLower = topic.toLowerCase();
		
		GetIndexRequest request = new GetIndexRequest(topicLower); 
		
		boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
		if(!exists){
			CreateIndexRequest createIndexRequest = new CreateIndexRequest(topicLower); 
			CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);		
			log.info("{} : created {}", createIndexResponse.index(), createIndexResponse.isAcknowledged());
		}
	}
	
	//TTL is not supported in Elasticsearch 5.0 and later, what can we do? FIXME
	public void saveJsons(Topic topic, List<JSONObject> jsons) {
		BulkRequest request = new BulkRequest();

		for (JSONObject json : jsons) {
			if(topic.isCorrelateClearedMessage()) {
				boolean found = correlateClearedMessage(json);
				if(found) {
					continue;
				}
			}
			
			String id = topic.getMessageId(json); //id can be null
			
			request.add(new IndexRequest(topic.getName().toLowerCase(), "doc", id).source(json.toString(), XContentType.JSON));
		}
		if(config.isAsync()) {
			client.bulkAsync(request, RequestOptions.DEFAULT, listener);			
		}else {
			try {
				client.bulk(request, RequestOptions.DEFAULT);
			} catch (IOException e) { 
				log.error( topic.getName() , e);
			}
		}
	}
	
	public boolean correlateClearedMessage(JSONObject json) {
		boolean found = true;
		Gson gson = new Gson();

		try {

			JsonObject jsonObject = gson.fromJson(json.toString(),JsonObject.class);

			String eventName = jsonObject.getEvent().getCommonEventHeader().getEventName();
			Long startEpochMicrosec = jsonObject.getEvent().getCommonEventHeader().getStartEpochMicrosec();
			Long lastEpochMicrosec = jsonObject.getEvent().getCommonEventHeader().getLastEpochMicrosec();

			if (eventName != null && (startEpochMicrosec.toString().length() >= 13 || lastEpochMicrosec.toString().length() >= 13)) {

				if (eventName.contains("Cleared")) {
					jsonObject.getEvent().getFaultFields().setVfStatus("close");
					//jsonObject.getEvent().getCommonEventHeader().getEventName().replace("Cleared","");
					//search
					SearchRequest request = new SearchRequest();
					SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

					String eventName2 = eventName.replace("Cleared","");
					String reportingEntityName = jsonObject.getEvent().getCommonEventHeader().getReportingEntityName();
					String specificProblem = jsonObject.getEvent().getFaultFields().getSpecificProblem();

					MatchPhraseQueryBuilder mpqb1 = QueryBuilders
							.matchPhraseQuery("event.commonEventHeader.eventName",eventName2);
					MatchPhraseQueryBuilder mpqb2 = QueryBuilders
							.matchPhraseQuery("event.commonEventHeader.reportingEntityName",reportingEntityName);
					MatchPhraseQueryBuilder mpqb3 = QueryBuilders
							.matchPhraseQuery("event.faultFields.specificProblem",specificProblem);
					QueryBuilder qb = QueryBuilders.boolQuery().must(mpqb1).must(mpqb2).must(mpqb3);
					sourceBuilder.query(qb);
					request.source(sourceBuilder);
					sourceBuilder.from(0);
					sourceBuilder.size(10);
					sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

					SearchResponse response = client.search(request, RequestOptions.DEFAULT);

					RestStatus status = response.status();
					TimeValue took = response.getTook();
					Boolean terminatedEarly = response.isTerminatedEarly();
					boolean timedOut = response.isTimedOut();

					int totalShards = response.getTotalShards();
					int successfulShards = response.getSuccessfulShards();
					int failedShards = response.getFailedShards();
					for (ShardSearchFailure failure : response.getShardFailures()) {
						// failures should be handled here
					}

					SearchHits hits = response.getHits();
					long totalHits = hits.getTotalHits();
					float maxScore = hits.getMaxScore();
					SearchHit[] searchHits = hits.getHits();
					String sourceAsString = null;
					//es doc
					String index = null;
					String type = null;
					String id = null;
					for (SearchHit hit : searchHits) {
						// do something with the SearchHit
						index = hit.getIndex();
						type = hit.getType();
						id = hit.getId();
						float score = hit.getScore();
						//get _source value
						sourceAsString = hit.getSourceAsString(); //jsonString

					}
					log.info("id:"+id+"index:"+index+"type:"+type);
					log.info(sourceAsString);
					// json -> object
					JsonObject jsonObjectSource = gson.fromJson(sourceAsString,JsonObject.class);
					log.info("object str"+jsonObjectSource);

					jsonObjectSource.getEvent().getCommonEventHeader().setStartEpochMicrosec(startEpochMicrosec);
					jsonObjectSource.getEvent().getCommonEventHeader().setLastEpochMicrosec(lastEpochMicrosec);
					jsonObjectSource.getEvent().getFaultFields().setVfStatus(jsonObject.getEvent().getFaultFields().getVfStatus());

					String jsonString = gson.toJson(jsonObjectSource);

					log.info(jsonString);
					//update
					BulkRequest bulkRequest = new BulkRequest();
					bulkRequest.add(new IndexRequest(index,type,id).source(jsonString, XContentType.JSON));

					if(config.isAsync()) {
						client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, listener);
					}else {
						try {
							client.bulk(bulkRequest, RequestOptions.DEFAULT);
						} catch (IOException e) {
							log.error(id , e);
						}
					}

					found = true;

				} else {

					//no Cleared
					found = false;
				}

			} else {
				log.info("event is null");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return found;
	}

}
