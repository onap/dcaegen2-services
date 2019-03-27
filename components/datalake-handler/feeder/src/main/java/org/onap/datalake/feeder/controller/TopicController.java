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
import java.util.Optional;

import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.repository.TopicRepository;
import org.onap.datalake.feeder.service.DmaapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller manages all the topic settings. Topic "_DL_DEFAULT_" acts as
 * the default. For example, if a topic's enabled=null, _DL_DEFAULT_.enabled is
 * used for that topic. All the settings are saved in Couchbase. topic
 * "_DL_DEFAULT_" is populated at setup by a DB script.
 * 
 * @author Guobiao Mo
 *
 */

@RestController
@RequestMapping(value = "/topics", produces = { MediaType.APPLICATION_JSON_VALUE })
public class TopicController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DmaapService dmaapService;

	@Autowired
	private TopicRepository topicRepository;

	//list all topics in DMaaP
	@GetMapping("/dmaap/")
	@ResponseBody
	public List<String> listDmaapTopics() throws IOException {
		return dmaapService.getTopics();
	}

	//list all topics 
	@GetMapping("/")
	@ResponseBody
	public Iterable<Topic> list() throws IOException {
		Iterable<Topic> ret = topicRepository.findAll();
		return ret;
	}

	//Read a topic
	@GetMapping("/{name}")
	@ResponseBody
	public Topic getTopic(@PathVariable("name") String topicName) throws IOException {
		//Topic topic = topicRepository.findFirstById(topicName);   	
		Optional<Topic> topic = topicRepository.findById(topicName);
		if (topic.isPresent()) {
			return topic.get();
		} else {
			return null;
		}
	}

	//Update Topic
	@PutMapping("/")
	@ResponseBody
	public Topic updateTopic(Topic topic, BindingResult result) throws IOException {

		if (result.hasErrors()) {
			log.error(result.toString());
			
			return null;//TODO return binding error
		}

		Topic oldTopic = getTopic(topic.getId());
		if (oldTopic == null) {
			return null;//TODO return not found error
		} else {
			topicRepository.save(topic);
			return topic;
		}
	}

	//create a new Topic  
	@PostMapping("/")
	@ResponseBody
	public Topic createTopic(Topic topic, BindingResult result) throws IOException {

		if (result.hasErrors()) {
			log.error(result.toString());
			return null;
		}

		Topic oldTopic = getTopic(topic.getId());
		if (oldTopic != null) {
			return null;//TODO return 'already exists' error
		} else {
			topicRepository.save(topic);
			return topic;
		}
	}

}
