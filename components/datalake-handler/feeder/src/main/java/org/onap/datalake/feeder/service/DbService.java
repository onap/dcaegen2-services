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

import java.util.Optional;

import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.repository.DbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for Dbs
 * 
 * @author Guobiao Mo
 *
 */
@Service
public class DbService {

	@Autowired
	private DbRepository dbRepository;

	public Db getDb(String name) {
		Optional<Db> ret = dbRepository.findById(name);
		return ret.isPresent() ? ret.get() : null;
	}

	public Db getCouchbase() {
		return getDb("Couchbase");
	}

	public Db getElasticsearch() {
		return getDb("Elasticsearch");
	}

	public Db getMongoDB() {
		return getDb("MongoDB");
	}

	public Db getDruid() {
		return getDb("Druid");
	}

	public Db getHdfs() {
		return getDb("HDFS");
	}

}
