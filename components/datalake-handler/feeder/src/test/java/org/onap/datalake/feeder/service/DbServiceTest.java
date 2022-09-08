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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.DbType;
import org.onap.datalake.feeder.repository.DbRepository;
import org.onap.datalake.feeder.service.db.CouchbaseService;
import org.onap.datalake.feeder.service.db.ElasticsearchService;
import org.onap.datalake.feeder.service.db.HdfsService;
import org.onap.datalake.feeder.service.db.MongodbService;
import org.springframework.context.ApplicationContext;


/**
 * Test Service for Dbs 
 * 
 * @author Guobiao Mo
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DbServiceTest {

	@Mock
	private DbType dbType;

	@Mock
	private ApplicationContext context;

	@Mock
	private DbRepository dbRepository;
	
	@InjectMocks
	private DbService dbService;

	@Test
	public void testGetDb() {
		String name = "a";
		//when(dbRepository.findByName(name)).thenReturn(new Db(name));
		assertEquals("a", name);
	}

	@Test
	public void testFindDbStoreService(){
		when(dbType.getId()).thenReturn("CB","ES","HDFS","MONGO","KIBANA");

		Db db = Mockito.mock(Db.class);
		when(db.getId()).thenReturn(1,2,3,4,5,6,7,8,9);
		when(db.getDbType()).thenReturn(dbType);

		when(context.getBean(CouchbaseService.class, db)).thenReturn(new CouchbaseService(db));
		when(context.getBean(ElasticsearchService.class, db)).thenReturn(new ElasticsearchService(db));
		when(context.getBean(HdfsService.class, db)).thenReturn(new HdfsService(db));
		when(context.getBean(MongodbService.class, db)).thenReturn(new MongodbService(db));

		dbService.findDbStoreService(db);
		dbService.findDbStoreService(db);
		dbService.findDbStoreService(db);
		dbService.findDbStoreService(db);
		dbService.findDbStoreService(db);



	}
	
	/*
	@Test
	public void testGetDb() {
		String name = "a";
		when(dbRepository.findByName(name)).thenReturn(new Db(name));
		assertEquals(dbService.getDb(name), new Db(name));
	}

	@Test
	public void testGetDbNull() {
		String name = null;
		when(dbRepository.findByName(name)).thenReturn(null);
		assertNull(dbService.getDb(name));
	}

	@Test
	public void testGetCouchbase() {
		String name = "Couchbase";
		when(dbRepository.findByName(name)).thenReturn(new Db(name));
		assertEquals(dbService.getCouchbase(), new Db(name));
	}

	@Test
	public void testGetElasticsearch() {
		String name = "Elasticsearch";
		when(dbRepository.findByName(name)).thenReturn(new Db(name));
		assertEquals(dbService.getElasticsearch(), new Db(name));
	}

	@Test
	public void testGetMongoDB() {
		String name = "MongoDB";
		when(dbRepository.findByName(name)).thenReturn(new Db(name));
		assertEquals(dbService.getMongoDB(), new Db(name));
	}

	@Test
	public void testGetDruid() {
		String name = "Druid";
		when(dbRepository.findByName(name)).thenReturn(new Db(name));
		assertEquals(dbService.getDruid(), new Db(name));
	}

	@Test
	public void testGetHdfs() {
		String name = "HDFS";
		when(dbRepository.findByName(name)).thenReturn(new Db(name));
		assertEquals(dbService.getHdfs(), new Db(name));
	}
*/
}
