/*
* ============LICENSE_START=======================================================
* ONAP : DCAE
* ================================================================================
* Copyright 2018 TechMahindra
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
package org.onap.datalake.feeder.enumeration;

import org.onap.datalake.feeder.service.db.CouchbaseService;
import org.onap.datalake.feeder.service.db.DbStoreService;
import org.onap.datalake.feeder.service.db.ElasticsearchService;
import org.onap.datalake.feeder.service.db.HdfsService;
import org.onap.datalake.feeder.service.db.MongodbService;

/**
 * Database type
 * 
 * @author Guobiao Mo
 *
 */
public enum DbTypeEnum { 
	CB("Couchbase", CouchbaseService.class)
	, DRUID("Druid", null)
	, ES("Elasticsearch", ElasticsearchService.class)
	, HDFS("HDFS", HdfsService.class)
	, MONGO("MongoDB", MongodbService.class)
	, KIBANA("Kibana", null)
	, SUPERSET("Superset", null);

	private final String name;
	private final Class<? extends DbStoreService> serviceClass;

	DbTypeEnum(String name, Class<? extends DbStoreService> serviceClass) {
		this.name = name;
		this.serviceClass = serviceClass;
	}

	public Class<? extends DbStoreService> getServiceClass(){
		return serviceClass;
	}
}
