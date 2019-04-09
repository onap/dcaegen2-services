/*
* ============LICENSE_START=======================================================
* ONAP : DataLake
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
package org.onap.datalake.feeder.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.repository.TopicRepository;
import org.onap.datalake.feeder.service.DbService;
import org.onap.datalake.feeder.service.DmaapService;
import org.onap.datalake.feeder.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

/**
 * This controller manages topic settings. 
 * 
 * Topic "_DL_DEFAULT_" acts as the default. For example, if a topic's enabled=null, _DL_DEFAULT_.enabled is used for that topic. 
 * All the settings are saved in database. 
 * topic "_DL_DEFAULT_" is populated at setup by a DB script.
 * 
 * @author Guobiao Mo
 *
 */

@RestController
@RequestMapping(value = "/topics", produces = { MediaType.APPLICATION_JSON_VALUE })//, consumes= {MediaType.APPLICATION_JSON_UTF8_VALUE})
public class TopicController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DmaapService dmaapService;

	@Autowired
	private TopicRepository topicRepository;
	
	@Autowired
	private TopicService topicService;

	@Autowired
	private DbService dbService;
	
	@GetMapping("/dmaap/")
	@ResponseBody
	@ApiOperation(value="List all topics in DMaaP.")
	public List<String> listDmaapTopics() throws IOException {
		return dmaapService.getTopics();
	}

	@GetMapping("/")
	@ResponseBody
	@ApiOperation(value="List all topics' details.")
	public Iterable<Topic> list() throws IOException {
		Iterable<Topic> ret = topicRepository.findAll();
		return ret;
	}

	@GetMapping("/{topicName}")
	@ResponseBody
	@ApiOperation(value="Get a topic's details.")
	public Topic getTopic(@PathVariable("topicName") String topicName) throws IOException {
		Topic topic = topicService.getTopic(topicName);
		return topic;
	}

	@GetMapping("/{topicName}/dbs")
	@ResponseBody
	@ApiOperation(value="Get all DBs in a topic.")
	public Set<Db> getTopicDbs(@PathVariable("topicName") String topicName) throws IOException {
		Topic topic = topicService.getTopic(topicName);
		Set<Db> dbs = topic.getDbs();
		return dbs;
	}

	//This is not a partial update: old topic is wiped out, and new topic is created based on the input json. 
	//One exception is that old DBs are kept
	@PutMapping("/")
	@ResponseBody
	@ApiOperation(value="Update a topic.")
	public Topic updateTopic(@RequestBody Topic topic, BindingResult result, HttpServletResponse response) throws IOException {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing Topic: "+result.toString());
			return null; 
		}

		Topic oldTopic = getTopic(topic.getName());
		if (oldTopic == null) {
			sendError(response, 404, "Topic not found "+topic.getName());
			return null; 
		} else {
			if(!topic.isDefault()) {
				Topic defaultTopic = topicService.getDefaultTopic();
				topic.setDefaultTopic(defaultTopic);
			}
			
			topic.setDbs(oldTopic.getDbs());
			topicRepository.save(topic);
			return topic;
		}
	}
 
	@PostMapping("/")
	@ResponseBody
	@ApiOperation(value="Create a new topic.")
	public Topic createTopic(@RequestBody Topic topic, BindingResult result, HttpServletResponse response) throws IOException {
		
		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing Topic: "+result.toString());
			return null;
		}

		Topic oldTopic = getTopic(topic.getName());
		if (oldTopic != null) {
			sendError(response, 400, "Topic already exists "+topic.getName());
			return null;
		} else {
			if(!topic.isDefault()) {
				Topic defaultTopic = topicService.getDefaultTopic();
				topic.setDefaultTopic(defaultTopic);
			}
			
			topicRepository.save(topic);
			return topic;
		}
	}

	@DeleteMapping("/{topicName}/db/{dbName}")
	@ResponseBody
	@ApiOperation(value="Delete a DB from a topic.")
	public Set<Db> deleteDb(@PathVariable("topicName") String topicName, @PathVariable("dbName") String dbName, HttpServletResponse response) throws IOException {
		Topic topic = topicService.getTopic(topicName);
		Set<Db> dbs = topic.getDbs();
		dbs.remove(new Db(dbName));
		 
		topicRepository.save(topic);
		return topic.getDbs();		 
	}

	@PutMapping("/{topicName}/db/{dbName}")
	@ResponseBody
	@ApiOperation(value="Add a DB to a topic.")
	public Set<Db> addDb(@PathVariable("topicName") String topicName, @PathVariable("dbName") String dbName, HttpServletResponse response) throws IOException {
		Topic topic = topicService.getTopic(topicName);
		Set<Db> dbs = topic.getDbs();		

		Db db = dbService.getDb(dbName);		
		dbs.add(db);
		 
		topicRepository.save(topic);
		return topic.getDbs();		 
	}
	
	private void sendError(HttpServletResponse response, int sc, String msg) throws IOException {
		log.info(msg);
		response.sendError(sc, msg);		
	}
}
