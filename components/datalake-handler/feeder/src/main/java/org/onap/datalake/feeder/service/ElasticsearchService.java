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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.json.JSONObject;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.PortalDesign;
import org.onap.datalake.feeder.dto.TopicConfig;
import org.onap.datalake.feeder.repository.PortalDesignRepository;
import org.onap.datalake.feeder.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Elasticsearch Service for table creation, data submission, as well as data pre-processing. 
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
	
    @Autowired
    private PortalDesignRepository portalDesignRepository;
    
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

		log.info("Connected to Elasticsearch Host {}", elasticsearchHost);

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
		if (!exists) {
			//TODO submit mapping template
			//PortalDesign portalDesign = portalDesignRepository.findByName(topicLower);
			
			//if (portalDesign!=null){
			//	String body = portalDesign.getBody();
				
			//	HttpClientUtil.sendPost(dbService.getElasticsearch().getHost()+":9200/_template/my_logs", body);
			//}
			CreateIndexRequest createIndexRequest = new CreateIndexRequest(topicLower);
			CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
			log.info("{} : created {}", createIndexResponse.index(), createIndexResponse.isAcknowledged());
		}
	}

	//TTL is not supported in Elasticsearch 5.0 and later, what can we do? FIXME
	public void saveJsons(TopicConfig topic, List<JSONObject> jsons) {
		BulkRequest request = new BulkRequest();

		for (JSONObject json : jsons) {
			if (topic.isCorrelateClearedMessage()) {
				boolean found = correlateClearedMessage(topic, json);
				if (found) {
					continue;
				}
			}

			String id = topic.getMessageId(json); //id can be null

			request.add(new IndexRequest(topic.getName().toLowerCase(), config.getElasticsearchType(), id).source(json.toString(), XContentType.JSON));
		}

		log.debug("saving text to topic = {}, batch count = {} ", topic, jsons.size());

		if (config.isAsync()) {
			client.bulkAsync(request, RequestOptions.DEFAULT, listener);
		} else {
			try {
				client.bulk(request, RequestOptions.DEFAULT);
			} catch (IOException e) {
				log.error(topic.getName(), e);
			}
		}
	}

	/**
	 *
	 * @param topic
	 * @param json
	 * @return boolean
	 *
	 *         Because of query by id, The search API cannot be used for query. The
	 *         search API can only query all data or based on the fields in the
	 *         source. So use the get API, three parameters: index, type, document
	 *         id
	 */
	private boolean correlateClearedMessage(TopicConfig topic, JSONObject json) {
		boolean found = false;
		String eName = null;

		try {
			eName = json.query("/event/commonEventHeader/eventName").toString();

			if (StringUtils.isNotBlank(eName) && eName.endsWith("Cleared")) {

				String name = eName.substring(0, eName.length() - 7);
				String reportingEntityName = json.query("/event/commonEventHeader/reportingEntityName").toString();
				String specificProblem = json.query("/event/faultFields/specificProblem").toString();

				String id = String.join("^", name, reportingEntityName, specificProblem);//example: id = "aaaa^cccc^bbbbb"
				String index = topic.getName().toLowerCase();

				//get
				GetRequest getRequest = new GetRequest(index, config.getElasticsearchType(), id);

				GetResponse getResponse = null;
				try {
					getResponse = client.get(getRequest, RequestOptions.DEFAULT);
					if (getResponse != null) {

						if (getResponse.isExists()) {
							String sourceAsString = getResponse.getSourceAsString();
							JSONObject jsonObject = new JSONObject(sourceAsString);
							jsonObject.getJSONObject("event").getJSONObject("faultFields").put("vfStatus", "closed");
							String jsonString = jsonObject.toString();

							//update
							IndexRequest request = new IndexRequest(index, config.getElasticsearchType(), id);
							request.source(jsonString, XContentType.JSON);
							IndexResponse indexResponse = null;
							try {
								indexResponse = client.index(request, RequestOptions.DEFAULT);
								found = true;
							} catch (IOException e) {
								log.error("save failure");
							}
						} else {
							log.error("The getResponse was not exists");
						}

					} else {
						log.error("The document for this id was not found");
					}

				} catch (ElasticsearchException e) {
					if (e.status() == RestStatus.NOT_FOUND) {
						log.error("The document for this id was not found");
					}
					if (e.status() == RestStatus.CONFLICT) {
						log.error("Version conflict");
					}
					log.error("Get document exception", e);
				} catch (IOException e) {
					log.error(topic.getName(), e);
				}

			}

		} catch (Exception e) {
			log.error("error", e);
		}

		return found;
	}

}
