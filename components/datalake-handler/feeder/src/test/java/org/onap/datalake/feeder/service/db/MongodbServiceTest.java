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

package org.onap.datalake.feeder.service.db;

import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.service.DbService;
import org.onap.datalake.feeder.util.TestUtil;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@RunWith(MockitoJUnitRunner.class)
public class MongodbServiceTest {

	private MongodbService mongodbService;

	@Mock
	private ApplicationConfiguration config;

	@Mock
	private DbService dbService;

	@Mock
	private MongoDatabase database;

	@Mock
	private MongoClient mongoClient;

	@Mock
	private Map<String, MongoCollection<Document>> mongoCollectionMap = new HashMap<>();

	@Before
	public void init() throws NoSuchFieldException, IllegalAccessException {
		Db db = TestUtil.newDb("Mongodb");
		db.setDatabase("database");
		db.setLogin("login");
		mongodbService = new MongodbService(db);

		Field configField = MongodbService.class.getDeclaredField("config");
		configField.setAccessible(true);
		configField.set(mongodbService, config);
 
		mongodbService.init();
	}

	@Test
	public void cleanUp() {
		when(config.getShutdownLock()).thenReturn(new ReentrantReadWriteLock());
		mongodbService.cleanUp();
	}

	@Test
	public void saveJsons() {
		TestUtil.testSaveJsons(config, mongodbService);
	}
}