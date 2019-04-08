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
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.repository.DbRepository;
import org.onap.datalake.feeder.service.DbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
	private DbService dbService;

	//list all dbs 
	@GetMapping("/")
	@ResponseBody
	@ApiOperation(value="Get all databases' details.")
	public Iterable<Db> list() throws IOException {
		Iterable<Db> ret = dbRepository.findAll();
		return ret;
	}

	//Read a db
	//the topics are missing in the return, since in we use @JsonBackReference on Db's topics 
	//need to the the following method to retrieve the topic list 
	@GetMapping("/{dbName}")
	@ResponseBody
	@ApiOperation(value="Get a database's details.")
	public Db getDb(@PathVariable("dbName") String dbName, HttpServletResponse response) throws IOException {
		Db db = dbService.getDb(dbName);
		if (db == null) {
			sendError(response, 404, "Db not found: " + dbName);
		}
		return db;
	}

	//Read topics in a DB 
	@GetMapping("/{dbName}/topics")
	@ResponseBody
	@ApiOperation(value="Get a database's all topics.")
	public Set<Topic> getDbTopics(@PathVariable("dbName") String dbName) throws IOException {
		Db db = dbService.getDb(dbName);
		Set<Topic> topics = db.getTopics();
		return topics;
	}

	//Update Db
	@PutMapping("/")
	@ResponseBody
	@ApiOperation(value="Update a database.")
	public Db updateDb(@RequestBody Db db, BindingResult result, HttpServletResponse response) throws IOException {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing DB: " + result.toString());
			return null;
		}

		Db oldDb = dbService.getDb(db.getName());
		if (oldDb == null) {
			sendError(response, 404, "Db not found: " + db.getName());
			return null;
		} else {
			dbRepository.save(db);
			return db;
		}
	}

	@PostMapping("/")
	@ResponseBody
	@ApiOperation(value="Create a new database.")
	public Db createDb(@RequestBody Db db, BindingResult result, HttpServletResponse response) throws IOException {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing DB: " + result.toString());
			return null;
		}

		Db oldDb = dbService.getDb(db.getName());
		if (oldDb != null) {
			sendError(response, 400, "Db already exists: " + db.getName());
			return null;
		} else {
			dbRepository.save(db);
			return db;
		}
	}

	private void sendError(HttpServletResponse response, int sc, String msg) throws IOException {
		log.info(msg);
		response.sendError(sc, msg);
	}
}
