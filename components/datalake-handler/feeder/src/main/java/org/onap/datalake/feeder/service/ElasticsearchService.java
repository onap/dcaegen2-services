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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONObject;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
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

	private RestHighLevelClient client;
	ActionListener<BulkResponse> listener;

	@PostConstruct
	private void init() {
		String elasticsearchHost = config.getElasticsearchHost();

		// Initialize the Connection
		client = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticsearchHost, 9200, "http"), new HttpHost(elasticsearchHost, 9201, "http")));

		log.info("Connect to Elasticsearch Host " + elasticsearchHost);

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

		CreateIndexRequest createIndexRequest = new CreateIndexRequest(topicLower);
		try {
			CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);		
			log.info(createIndexResponse.index()+" : created "+createIndexResponse.isAcknowledged());
		}catch(ElasticsearchStatusException e) {
			log.info("{} create ES topic status: {}", topic, e.getDetailedMessage());			
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
			request.add(new IndexRequest(topic.getId().toLowerCase(), "doc").source(json.toString(), XContentType.JSON));
		}
		if(config.isAsync()) {
			client.bulkAsync(request, RequestOptions.DEFAULT, listener);			
		}else {
			try {
				client.bulk(request, RequestOptions.DEFAULT);
			} catch (IOException e) { 
				log.error( topic.getId() , e);
			}
		}
	}
	
	private boolean correlateClearedMessage(JSONObject json) {
		boolean found = false;
		
		/*TODO
		 * 1. check if this is a alarm cleared message
		 * 2. search previous alarm message
		 * 3. update previous message, if success, set found=true
		 */
		
		return found; 
	}

}
