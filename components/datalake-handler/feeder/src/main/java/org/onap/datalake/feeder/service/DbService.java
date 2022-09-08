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

import java.util.HashMap;
import java.util.Map;

import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.DbType;
import org.onap.datalake.feeder.enumeration.DbTypeEnum;
import org.onap.datalake.feeder.service.db.DbStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Service for Dbs
 * 
 * @author Guobiao Mo
 *
 */
@Service
public class DbService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApplicationContext context;

	private Map<Integer, DbStoreService> dbStoreServiceMap = new HashMap<>();

	public DbStoreService findDbStoreService(Db db) {
		int dbId = db.getId();
		if (dbStoreServiceMap.containsKey(dbId)) {
			return dbStoreServiceMap.get(dbId);
		}

		DbType dbType = db.getDbType();
		DbTypeEnum dbTypeEnum = DbTypeEnum.valueOf(dbType.getId());
		Class<? extends DbStoreService> serviceClass = dbTypeEnum.getServiceClass();
		
		if (serviceClass == null) {
			log.error("Should not have come here {}", db);
			dbStoreServiceMap.put(dbId, null);
			return null;
		}
		
		DbStoreService ret = context.getBean(serviceClass, db);
		dbStoreServiceMap.put(dbId, ret);

		return ret;
	}
}
