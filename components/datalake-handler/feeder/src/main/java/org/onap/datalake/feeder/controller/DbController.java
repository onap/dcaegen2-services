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

import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.repository.DbRepository;
import org.onap.datalake.feeder.dto.DbConfig;
import org.onap.datalake.feeder.controller.domain.PostReturnBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiOperation;

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

	//list all dbs 
	@GetMapping("")
	@ResponseBody
	@ApiOperation(value="Gat all databases name")
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

	@GetMapping("/idAndName")
	@ResponseBody
	@ApiOperation(value="Gat all databases id and name")
	public Map<Integer, String> listIdAndName() {
		Iterable<Db> ret = dbRepository.findAll();
		Map<Integer, String> map = new HashMap<>();
		for(Db db : ret)
		{
			log.info(db.getId() + "\t"+ db.getName());
			map.put(db.getId(), db.getName());
		}
		return map;
	}

	//Create a  DB
	@PostMapping("")
	@ResponseBody
	@ApiOperation(value="Create a new database.")
	public PostReturnBody<DbConfig> createDb(@RequestBody DbConfig dbConfig, BindingResult result, HttpServletResponse response) throws IOException {
		if (result.hasErrors()) {
			sendError(response, 400, "Malformed format of Post body: " + result.toString());
			return null;
		}

/*		Db oldDb = dbService.getDb(dbConfig.getName());
		if (oldDb != null) {
			sendError(response, 400, "Db already exists: " + dbConfig.getName());
			return null;
		} else {*/
			Db newdb = new Db();
			newdb.setName(dbConfig.getName());
			newdb.setHost(dbConfig.getHost());
			newdb.setPort(dbConfig.getPort());
			newdb.setEnabled(dbConfig.isEnabled());
			newdb.setLogin(dbConfig.getLogin());
			newdb.setPass(dbConfig.getPassword());
			newdb.setEncrypt(dbConfig.isEncrypt());

			if(!dbConfig.getName().equals("Elecsticsearch") || !dbConfig.getName().equals("Druid"))
			{
				newdb.setDatabase(new String(dbConfig.getDatabase()));
			}
			dbRepository.save(newdb);
			DbConfig retMsg;
			PostReturnBody<DbConfig> retBody = new PostReturnBody<>();
			retMsg = new DbConfig();
			composeRetMessagefromDbConfig(newdb, retMsg);
			retBody.setReturnBody(retMsg);
			retBody.setStatusCode(200);
			return retBody;
		//}
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


	//Delete a db
	//the topics are missing in the return, since in we use @JsonBackReference on Db's topics
	//need to the the following method to retrieve the topic list
	@DeleteMapping("/{dbName}")
	@ResponseBody
	@ApiOperation(value="Delete a database.")
	public void deleteDb(@PathVariable("dbName") String dbName, HttpServletResponse response) throws IOException {

		Db delDb = dbRepository.findByName(dbName);
		if (delDb == null) {
			sendError(response, 404, "Db not found: " + dbName);
			return;
		}
		Set<Topic> topicRelation = delDb.getTopics();
		topicRelation.clear();
		dbRepository.save(delDb);
		dbRepository.delete(delDb);
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


	//Update Db
	@PutMapping("")
	@ResponseBody
	@ApiOperation(value="Update a database.")
	public PostReturnBody<DbConfig> updateDb(@RequestBody DbConfig dbConfig, BindingResult result, HttpServletResponse response) throws IOException {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing DB: " + result.toString());
			return null;
		}

		Db oldDb = dbRepository.findById(dbConfig.getId()).get();
		if (oldDb == null) {
			sendError(response, 404, "Db not found: " + dbConfig.getName());
			return null;
		} else {
			oldDb.setHost(dbConfig.getHost());
			oldDb.setPort(dbConfig.getPort());
			oldDb.setEnabled(dbConfig.isEnabled());
			oldDb.setLogin(dbConfig.getLogin());
			oldDb.setPass(dbConfig.getPassword());
			oldDb.setEncrypt(dbConfig.isEncrypt());
			if (!oldDb.getName().equals("Elecsticsearch") || !oldDb.getName().equals("Druid")) {
				oldDb.setDatabase(dbConfig.getDatabase());
			}

			dbRepository.save(oldDb);
			DbConfig retMsg;
			PostReturnBody<DbConfig> retBody = new PostReturnBody<>();
			retMsg = new DbConfig();
			composeRetMessagefromDbConfig(oldDb, retMsg);
			retBody.setReturnBody(retMsg);
			retBody.setStatusCode(200);
			return retBody;
		}

	}


	@PostMapping("/verify")
	@ResponseBody
	@ApiOperation(value="Database connection verification")
	public PostReturnBody<DbConfig> verifyDbConnection(@RequestBody DbConfig dbConfig, HttpServletResponse response) throws IOException {

		/*
			Not implemented yet.
		 */

		response.setStatus(501);
		return null;
	}

	private void composeRetMessagefromDbConfig(Db db, DbConfig dbConfigMsg)
	{
		dbConfigMsg.setName(db.getName());
		dbConfigMsg.setHost(db.getHost());
		dbConfigMsg.setEnabled(db.isEnabled());
		dbConfigMsg.setPort(db.getPort());
		dbConfigMsg.setLogin(db.getLogin());
		dbConfigMsg.setDatabase(db.getDatabase());


	}

	private void sendError(HttpServletResponse response, int sc, String msg) throws IOException {
		log.info(msg);
		response.sendError(sc, msg);
	}
}
