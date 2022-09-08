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
import org.onap.datalake.feeder.domain.DbType;
import org.onap.datalake.feeder.domain.DesignType;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.repository.DbRepository;
import org.onap.datalake.feeder.dto.DbConfig;
import org.onap.datalake.feeder.controller.domain.PostReturnBody;
import org.onap.datalake.feeder.repository.DbTypeRepository;
import org.onap.datalake.feeder.repository.DesignTypeRepository;
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
	private static final String DB_NOT_FOUND = "Db not found: ";

	@Autowired
	private DbRepository dbRepository;

    @Autowired
    private DbTypeRepository dbTypeRepository;

	@Autowired
	private DesignTypeRepository designTypeRepository;

	//list all dbs
	@GetMapping("")
	@ResponseBody
	@ApiOperation(value="Get all database id")
	public List<Integer> list() {
		Iterable<Db> ret = dbRepository.findAll();
		List<Integer> retString = new ArrayList<>();
		for(Db db : ret)
		{
			retString.add(db.getId());

		}
		return retString;
	}

	@GetMapping("/list")
	@ResponseBody
	@ApiOperation(value="Get all tools or dbs")
	public List<DbConfig> dblistByTool(@RequestParam boolean isDb) {
		log.info("Search dbs by tool start......");
		Iterable<DbType> dbType  = dbTypeRepository.findByTool(!isDb);
		List<DbConfig> retDbConfig = new ArrayList<>();
		for (DbType item : dbType) {
			for (Db d : item.getDbs()) {
				retDbConfig.add(d.getDbConfig());
			}
		}
		return retDbConfig;
	}

	@GetMapping("/idAndName/{id}")
	@ResponseBody
	@ApiOperation(value="Get all databases id and name by designTypeId")
	public Map<Integer, String> listIdAndName(@PathVariable String id) {
		Optional<DesignType> designType  = designTypeRepository.findById(id);
		Map<Integer, String> map = new HashMap<>();
		if (designType.isPresent()) {
			Set<Db> dbs = designType.get().getDbType().getDbs();
			for (Db item : dbs) {
				map.put(item.getId(), item.getName());
			}
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
			newdb.setPass(dbConfig.getPass());
			newdb.setEncrypt(dbConfig.isEncrypt());
			if (dbConfig.getDbTypeId().isEmpty()) {
                sendError(response, 400, "Malformed format of Post body: " + result.toString());
            } else {
                Optional<DbType> dbType = dbTypeRepository.findById(dbConfig.getDbTypeId());
                if (dbType.isPresent()) {
                    newdb.setDbType(dbType.get());
                }
            }

			if(!dbConfig.getName().equals("Elecsticsearch") || dbConfig.getName().equals("Druid"))
			{
				newdb.setDatabase(dbConfig.getDatabase());
			}
			dbRepository.save(newdb);
            log.info("Db save ....... name: " + dbConfig.getName());
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
	@GetMapping("/{dbId}")
	@ResponseBody
	@ApiOperation(value="Get a database's details.")
	public DbConfig getDb(@PathVariable("dbId") int dbId, HttpServletResponse response) throws IOException {
  		Optional<Db> db = dbRepository.findById(dbId);
  		return db.isPresent() ? db.get().getDbConfig() : null;
 	}


	//Delete a db
	//the topics are missing in the return, since in we use @JsonBackReference on Db's topics
	//need to the the following method to retrieve the topic list
	@DeleteMapping("/{id}")
	@ResponseBody
	@ApiOperation(value="Delete a database.")
	public void deleteDb(@PathVariable("id") int id, HttpServletResponse response) throws IOException {

		Optional<Db> delDb = dbRepository.findById(id);
		if (!delDb.isPresent()) {
			sendError(response, 404, "Db not found: " + id);
			return;
		} else {
            Set<Topic> topicRelation = delDb.get().getTopics();
            topicRelation.clear();
            dbRepository.delete(delDb.get());
            response.setStatus(204);
        }
	}

	//Read topics in a DB
	@GetMapping("/{dbName}/topics")
	@ResponseBody
	@ApiOperation(value="Get a database's all topics.")
	public Set<Topic> getDbTopics(@PathVariable("dbName") String dbName, HttpServletResponse response) throws IOException {
		Set<Topic> topics;
		try {
			Db db = dbRepository.findByName(dbName);
			topics = db.getTopics();
		} catch(Exception ex) {
			sendError(response, 404, "DB: " + dbName + " or Topics not found");
			return Collections.emptySet();

		}
		return topics;
	}

	//Update Db
	@PutMapping("/{id}")
	@ResponseBody
	@ApiOperation(value="Update a database.")
	public PostReturnBody<DbConfig> updateDb(@PathVariable int id, @RequestBody DbConfig dbConfig, BindingResult result, HttpServletResponse response) throws IOException {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing DB: " + result.toString());
			return null;
		}

		Db oldDb = dbRepository.findById(id).get();
		if (oldDb == null) {
			sendError(response, 404, DB_NOT_FOUND + dbConfig.getName());
			return null;
		} else {

			oldDb.setEnabled(dbConfig.isEnabled());
            oldDb.setName(dbConfig.getName());
            oldDb.setHost(dbConfig.getHost());
            oldDb.setPort(dbConfig.getPort());
            oldDb.setLogin(dbConfig.getLogin());
            oldDb.setPass(dbConfig.getPass());
            oldDb.setEncrypt(dbConfig.isEncrypt());
            if (dbConfig.getDbTypeId().isEmpty()) {
                sendError(response, 400, "Malformed format of Post body: " + result.toString());
            } else {
                Optional<DbType> dbType = dbTypeRepository.findById(dbConfig.getDbTypeId());
                if (dbType.isPresent()) {
                    oldDb.setDbType(dbType.get());
                }
            }
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

	//get db type list
	@GetMapping("/dbtypes")
	@ResponseBody
	@ApiOperation(value="Get a list of all db types.")
	public Iterable<DbType> getDbTypes(HttpServletResponse response) throws IOException {
		log.info("Get a list of all db types ......");
		Iterable<DbType> dbTypes = dbTypeRepository.findAll(); 
		return dbTypes;
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
        dbConfigMsg.setId(db.getId());
		dbConfigMsg.setName(db.getName());
		dbConfigMsg.setHost(db.getHost());
		dbConfigMsg.setEnabled(db.isEnabled());
		dbConfigMsg.setPort(db.getPort());
		dbConfigMsg.setLogin(db.getLogin());
		dbConfigMsg.setDatabase(db.getDatabase());
        dbConfigMsg.setDbTypeId(db.getDbType().getId());
        dbConfigMsg.setPass(db.getPass());

	}

	private void sendError(HttpServletResponse response, int sc, String msg) throws IOException {
		log.info(msg);
		response.sendError(sc, msg);
	}
}
