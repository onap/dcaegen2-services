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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.service.DbService;
import org.onap.datalake.feeder.util.TestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchServiceTest {
	@Mock
	private ApplicationConfiguration config;

	@Mock
	private RestHighLevelClient client;

	@Mock
	ActionListener<BulkResponse> listener;

	@Mock
	private DbService dbService;

	private ElasticsearchService elasticsearchService;

	@Before
	public void init() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		//MockitoAnnotations.initMocks(this);

		Db db = TestUtil.newDb("Elasticsearch");
		db.setHost("host");
		elasticsearchService = new ElasticsearchService(db);

		Field configField = ElasticsearchService.class.getDeclaredField("config");
		configField.setAccessible(true);
		configField.set(elasticsearchService, config);
		
		elasticsearchService.init();
	}

	@Test
	public void testCleanUp() throws IOException {
		when(config.getShutdownLock()).thenReturn(new ReentrantReadWriteLock());
		elasticsearchService.cleanUp();
	}

	@Test(expected = IOException.class)
	public void testEnsureTableExist() throws IOException {
		elasticsearchService.ensureTableExist("test");
	}

	@Test
	public void testSaveJsons() {
		when(config.getElasticsearchType()).thenReturn("doc");

		when(config.isAsync()).thenReturn(true);
		TestUtil.testSaveJsons(config, elasticsearchService);

		when(config.isAsync()).thenReturn(false);
		TestUtil.testSaveJsons(config, elasticsearchService);
	}
}