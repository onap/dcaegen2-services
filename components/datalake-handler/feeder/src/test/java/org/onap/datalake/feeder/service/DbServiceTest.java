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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.repository.DbRepository;

/**
 * Test Service for Dbs 
 * 
 * @author Guobiao Mo
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DbServiceTest {

	@Mock
	private DbRepository dbRepository;
	
	@InjectMocks
	private DbService dbService;

	@Test
	public void testGetDb() {
		String name = "a";
		when(dbRepository.findById(name)).thenReturn(Optional.of(new Db(name)));
		assertEquals(dbService.getDb(name), new Db(name));
	}

	@Test
	public void testGetDbNull() {
		String name = null;
		when(dbRepository.findById(name)).thenReturn(Optional.empty());
		assertNull(dbService.getDb(name));
	}

	@Test
	public void testGetCouchbase() {
		String name = "Couchbase";
		when(dbRepository.findById(name)).thenReturn(Optional.of(new Db(name)));
		assertEquals(dbService.getCouchbase(), new Db(name));
	}

	@Test
	public void testGetElasticsearch() {
		String name = "Elasticsearch";
		when(dbRepository.findById(name)).thenReturn(Optional.of(new Db(name)));
		assertEquals(dbService.getElasticsearch(), new Db(name));
	}

	@Test
	public void testGetMongoDB() {
		String name = "MongoDB";
		when(dbRepository.findById(name)).thenReturn(Optional.of(new Db(name)));
		assertEquals(dbService.getMongoDB(), new Db(name));
	}

	@Test
	public void testGetDruid() {
		String name = "Druid";
		when(dbRepository.findById(name)).thenReturn(Optional.of(new Db(name)));
		assertEquals(dbService.getDruid(), new Db(name));
	}

}
