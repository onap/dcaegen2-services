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
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.*;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.repository.DbRepository;
import org.onap.datalake.feeder.repository.TopicRepository;
import org.onap.datalake.feeder.service.DbService;
import org.onap.datalake.feeder.controller.domain.dbConfig;
import org.onap.datalake.feeder.controller.domain.postReturnBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This controller manages the big data storage settings. All the settings are
 * saved in database.
 * 
 * @author Guobiao Mo
 *
 */

@RestController
@RequestMapping(value = "/dbs", produces = { MediaType.APPLICATION_JSON_VALUE })

//@Api(value = "db", consumes = "application/json", produces = "application/json")
public class DbController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DbRepository dbRepository;

	@Autowired
	private TopicRepository topicRepository;

	@Autowired
	private DbService dbService;

	//list all dbs 
	@GetMapping("")
	@ResponseBody
	@ApiOperation(value="Get all databases' details.")
	//public Iterable<Db> list() throws IOException {
	public List<String> list() throws IOException {
		Iterable<Db> ret = dbRepository.findAll();
		List<String> retString = new ArrayList<>();
		for(Db db : ret)
		{
			log.info(db.getName());
			retString.add(db.getName());

		}
		return retString;
	}

	//Create a  DB
	@PostMapping("")
	@ResponseBody
	@ApiOperation(value="Create a new database.")
	public postReturnBody<dbConfig> createDb(@RequestBody dbConfig db, BindingResult result, HttpServletResponse response) throws IOException {
		if (result.hasErrors()) {
			sendError(response, 400, "Malformed format of Post body: " + result.toString());
			return null;
		}

		Db oldDb = dbService.getDb(db.getName());
		if (oldDb != null) {
			sendError(response, 400, "Db already exists: " + db.getName());
			return null;
		} else {
			Db newdb = new Db();
			newdb.setName(new String(db.getName()));
			newdb.setHost(new String(db.getHost()));
			newdb.setPort(db.getPort());
			newdb.setLogin(new String(db.getLogin()));
			newdb.setPass(new String(db.getPassword()));
			newdb.setEncrypt(false);

			if(db.getName() != "Elecsticsearch" || db.getName() != "Druid")
			{
				newdb.setDatabase(new String(db.getDatabase()));
			}
			dbRepository.save(newdb);
			dbConfig retMsg;
			postReturnBody<dbConfig> retBody = new postReturnBody<>();
			retMsg = new dbConfig();
			retMsg.setName(db.getName());
			retMsg.setHost(db.getHost());
			retMsg.setPort(db.getPort());
			retBody.setReturnBody(retMsg);
			retBody.setStatusCode(200);
			return retBody;
		}
	}

	//Show a db
	//the topics are missing in the return, since in we use @JsonBackReference on Db's topics 
	//need to the the following method to retrieve the topic list 
	@GetMapping("/{dbName}")
	@ResponseBody
	@ApiOperation(value="Get a database's details.")
	public Db getDb(@PathVariable("dbName") String dbName, HttpServletResponse response) throws IOException {
		/*Db db = dbService.getDb(dbName);
		if (db == null) {
			sendError(response, 404, "Db not found: " + dbName);
		}*/
		Db db = dbRepository.findByName(dbName);
		if (db == null) {
			sendError(response, 404, "Db not found: " + dbName);
		}
		return db;
	}


	//Update Db
	@PutMapping("/{dbName}")
	@ResponseBody
	@ApiOperation(value="Update a database.")
	public postReturnBody<dbConfig> updateDb(@PathVariable("dbName") String dbName, @RequestBody dbConfig db, BindingResult result, HttpServletResponse response) throws IOException {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing DB: " + result.toString());
			return null;
		}

		Db oldDb = dbService.getDb(db.getName());
		if (oldDb == null) {
			sendError(response, 404, "Db not found: " + db.getName());
			return null;
		} else {
			oldDb.setName(new String(db.getName()));
			oldDb.setHost(new String(db.getHost()));
			oldDb.setPort(db.getPort());
			oldDb.setLogin(new String(db.getLogin()));
			oldDb.setPass(new String(db.getPassword()));
			oldDb.setEncrypt(false);

			if(oldDb.getName() != "Elecsticsearch" || oldDb.getName() != "Druid")
			{
				oldDb.setDatabase(new String(db.getDatabase()));
			}
			dbRepository.save(oldDb);
			dbConfig retMsg;
			postReturnBody<dbConfig> retBody = new postReturnBody<>();
			retMsg = new dbConfig();
			retMsg.setName(db.getName());
			retMsg.setHost(db.getHost());
			retMsg.setPort(db.getPort());
			retBody.setReturnBody(retMsg);
			retBody.setStatusCode(200);
			return retBody;
		}
	}

	//Delete a db
	//the topics are missing in the return, since in we use @JsonBackReference on Db's topics
	//need to the the following method to retrieve the topic list
	@DeleteMapping("/{dbName}")
	@ResponseBody
	@ApiOperation(value="Delete a database.")
	public void deleteDb(@PathVariable("dbName") String dbName, HttpServletResponse response) throws IOException {

		Db delDb = dbRepository.findByName(dbName);
		Set<Topic> topicRelation = delDb.getTopics();
		topicRelation.clear();
		dbRepository.save(delDb);
		dbRepository.delete(delDb);
		if (delDb == null) {
			sendError(response, 404, "Db not found: " + dbName);
		}
		response.setStatus(204);
	}

	//Read topics in a DB
	@GetMapping("/{dbName}/topics")
	@ResponseBody
	@ApiOperation(value="Get a database's all topics.")
	public Set<Topic> getDbTopics(@PathVariable("dbName") String dbName, HttpServletResponse response) throws IOException {
		//Db db = dbService.getDb(dbName);
		Set<Topic> topics;
		try {
			Db db = dbRepository.findByName(dbName);
			topics = db.getTopics();
		}catch(Exception ex)
		{
			sendError(response, 404, "DB: " + dbName + " or Topics not found");
			return null;

		}
		return topics;
	}


	@PostMapping("/verify")
	@ResponseBody
	@ApiOperation(value="Get a database's all topics.")
	public postReturnBody<dbConfig> verifyDbConnection(@RequestBody dbConfig db, HttpServletResponse response) throws IOException {

		/*
			Not implemented yet.
		 */

		response.setStatus(501);
		return null;
	}

	private void sendError(HttpServletResponse response, int sc, String msg) throws IOException {
		log.info(msg);
		response.sendError(sc, msg);
	}
}
