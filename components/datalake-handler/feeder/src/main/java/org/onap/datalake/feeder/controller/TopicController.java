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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.controller.domain.PostReturnBody;
import org.onap.datalake.feeder.controller.domain.TopicConfig;
import org.onap.datalake.feeder.repository.DbRepository;
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
 * Topic "_DL_DEFAULT_" acts as the default. For example, if a topic's
 * enabled=null, _DL_DEFAULT_.enabled is used for that topic. All the settings
 * are saved in database. topic "_DL_DEFAULT_" is populated at setup by a DB
 * script.
 * 
 * @author Guobiao Mo
 * @contributor Kate Hsuan @ QCT
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
	private DbRepository dbRepository;

	@Autowired
	private TopicService topicService;

	@Autowired
	private DbService dbService;

	@GetMapping("/dmaap/")
	@ResponseBody
	@ApiOperation(value = "List all topic names in DMaaP.")
	public List<String> listDmaapTopics() throws IOException {
		return dmaapService.getTopics();
	}

	@GetMapping("")
	@ResponseBody
	@ApiOperation(value="List all topics")
	public List<String> list() throws IOException {
		Iterable<Topic> ret = topicRepository.findAll();
		List<String> retString = new ArrayList<>();
		for(Topic item : ret)
		{
			if(!item.getName().equals("_DL_DEFAULT_") && !item.getName().equals("__consumer_offsets"))
				retString.add(item.getName());
		}
		return retString;
	}

	@PostMapping("")
	@ResponseBody
	@ApiOperation(value="Create a new topic.")
	public PostReturnBody<TopicConfig> createTopic(@RequestBody TopicConfig topicConfig, BindingResult result, HttpServletResponse response) throws IOException {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing Topic: "+result.toString());
			return null;
		}
		Topic oldTopic = topicService.getTopic(topicConfig.getName());
		if (oldTopic != null) {
			sendError(response, 400, "Topic already exists "+topicConfig.getName());
			return null;
		} else {
			Topic wTopic = new Topic();
			PostReturnBody<TopicConfig> retBody = new PostReturnBody<>();
			topicConfig.fillDbConfiguration(wTopic, dbRepository);
			topicRepository.save(wTopic);
			mkPostReturnBody(retBody, 200, wTopic);
			return retBody;
		}
	}

	@GetMapping("/{topicName}")
	@ResponseBody
	@ApiOperation(value="Get a topic's settings.")
	public TopicConfig getTopic(@PathVariable("topicName") String topicName, HttpServletResponse response) throws IOException {
		Topic topic = topicService.getTopic(topicName);
		if(topic == null) {
			sendError(response, 404, "Topic not found");
		}
		TopicConfig tConfig = new TopicConfig();
		mkReturnMessage(topic, tConfig);
		return tConfig;
	}

	//This is not a partial update: old topic is wiped out, and new topic is created based on the input json.
	//One exception is that old DBs are kept
	@PutMapping("/{topicName}")
	@ResponseBody
	@ApiOperation(value="Update a topic.")
	public PostReturnBody<TopicConfig> updateTopic(@PathVariable("topicName") String topicName, @RequestBody TopicConfig topicConfig, BindingResult result, HttpServletResponse response) throws IOException {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing Topic: "+result.toString());
			return null;
		}

		if(!topicName.equals(topicConfig.getName()))
		{
			sendError(response, 400, "Topic name mismatch" + topicName + topicConfig.getName());
			return null;
		}

		Topic oldTopic = topicService.getTopic(topicConfig.getName());
		if (oldTopic == null) {
			sendError(response, 404, "Topic not found "+topicConfig.getName());
			return null;
		} else {
			PostReturnBody<TopicConfig> retBody = new PostReturnBody<>();
			topicConfig.fillDbConfiguration(oldTopic, dbRepository);
			topicRepository.save(oldTopic);
			mkPostReturnBody(retBody, 200, oldTopic);
			return retBody;
		}
	}

    private void mkReturnMessage(Topic topic, TopicConfig tConfig)
	{
		tConfig.setName(topic.getName());
		tConfig.setEnable(topic.getEnabled());
		if(topic.getDataFormat() != null)
			tConfig.setData_format(topic.getDataFormat().toString());
		tConfig.setSave_raw(topic.getSaveRaw());
		tConfig.setCorrelated_clearred_message((topic.getCorrelateClearedMessage() == null) ? topic.getCorrelateClearedMessage() : false);
		tConfig.setMessage_id_path(topic.getMessageIdPath());
		Set<Db> topicDb = topic.getDbs();
		List<String> dbList = new ArrayList<>();
		for(Db item: topicDb)
		{
			dbList.add(item.getName());
		}
		tConfig.setSinkdbs(dbList);
	}

	private void mkPostReturnBody(PostReturnBody<TopicConfig> retBody, int statusCode, Topic topic)
	{
		TopicConfig retTopic = new TopicConfig();
        retBody.setStatusCode(statusCode);
        mkReturnMessage(topic, retTopic);
        retBody.setReturnBody(retTopic);
	}
	
	private void sendError(HttpServletResponse response, int sc, String msg) throws IOException {
		log.info(msg);
		response.sendError(sc, msg);		
	}
}
