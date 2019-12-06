/*-
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE
 * ================================================================================
 * Copyright (C) 2018-2019 Huawei. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.datalake.feeder.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.controller.domain.PostReturnBody;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.dto.DbConfig;
import org.onap.datalake.feeder.repository.DbRepository;
import org.onap.datalake.feeder.service.DbService;
import org.onap.datalake.feeder.util.TestUtil;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DbControllerTest {

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private DbRepository dbRepository;

    @Mock
    private BindingResult mockBindingResult;

    @InjectMocks
    private DbService dbService1;
    
    public DbConfig getDbConfig() {
        DbConfig dbConfig = new DbConfig();
        dbConfig.setName("Elecsticsearch");
        dbConfig.setHost("localhost");
        dbConfig.setLogin("root");
        dbConfig.setPass("root123");
        dbConfig.setDatabase("Elecsticsearch");
        dbConfig.setPort(123);
        dbConfig.setPoperties("driver");
      	dbConfig.setDbTypeId("ES");
        return dbConfig;
    }

    public void setAccessPrivateFields(DbController dbController) throws NoSuchFieldException,
            IllegalAccessException {
        Field dbRepository1 = dbController.getClass().getDeclaredField("dbRepository");
        dbRepository1.setAccessible(true);
        dbRepository1.set(dbController, dbRepository);
    }

    @Before
    public void setupTest() {
        MockitoAnnotations.initMocks(this);
        // While the default boolean return value for a mock is 'false',
        // it's good to be explicit anyway:
        when(mockBindingResult.hasErrors()).thenReturn(false);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateDb() throws IOException, NoSuchFieldException, IllegalAccessException {
        DbController dbController = new DbController();
        DbConfig dbConfig = getDbConfig();
        setAccessPrivateFields(dbController);
        PostReturnBody<DbConfig> db = dbController.createDb(dbConfig, mockBindingResult, httpServletResponse);
        assertEquals(200, db.getStatusCode());
        when(mockBindingResult.hasErrors()).thenReturn(true);
        db = dbController.createDb(dbConfig, mockBindingResult, httpServletResponse);
        assertEquals(null, db);
    }

    @Test
    public void testUpdateDb() throws IOException, NoSuchFieldException, IllegalAccessException {
        DbController dbController = new DbController();
        DbConfig dbConfig = getDbConfig();
        when(mockBindingResult.hasErrors()).thenReturn(true);
        PostReturnBody<DbConfig> db = dbController.updateDb(dbConfig, mockBindingResult,
                                                            httpServletResponse);
        assertEquals(null, db);
        //when(mockBindingResult.hasErrors()).thenReturn(false);
        setAccessPrivateFields(dbController);
        //db = dbController.updateDb(dbConfig, mockBindingResult, httpServletResponse);
        assertEquals(null, db);
        //when(mockBindingResult.hasErrors()).thenReturn(false);
        // String name = "Elecsticsearch";
        int id = 1234;
        //when(dbRepository.findByName(name)).thenReturn(TestUtil.newDb(name));
        when(dbRepository.findById(id)).thenReturn(TestUtil.newDb(id));
        //db = dbController.updateDb(dbConfig, mockBindingResult, httpServletResponse);
        //assertEquals(200, db.getStatusCode());
        Db elecsticsearch = dbController.getDb("Elecsticsearch", httpServletResponse);
        Db elecsticsearch = dbController.getDb(1234, httpServletResponse);
        assertNotNull(elecsticsearch);
    }

    @Test
    public void testGetAllDbs() throws IOException, IllegalAccessException, NoSuchFieldException {
        DbController dbController = new DbController();
        // String name = "Elecsticsearch";
        int id = 1234;
        List<Db> dbs = new ArrayList<>();
        // dbs.add(TestUtil.newDb(name));
        dbs.add(TestUtil.newDb(id));
        setAccessPrivateFields(dbController);
        when(dbRepository.findAll()).thenReturn(dbs);
        // List<String> list = dbController.list();
        List<int> list = dbController.list();
        // for (String dbName : list) {
        //     assertEquals("Elecsticsearch", dbName);
        // }
        for (int dbId : list) {
            assertEquals("1234", dbId);
        }
        //dbController.deleteDb("Elecsticsearch", httpServletResponse);
    }


    @Test
    public void testDeleteDb() throws IOException, IllegalAccessException, NoSuchFieldException {
        DbController dbController = new DbController();
        String dbName = "Elecsticsearch";
        String topicName = "a";
        Topic topic = TestUtil.newTopic(topicName);
        topic.setEnabled(true);
        topic.setId(1);
        Set<Topic> topics = new HashSet<>();
        topics.add(topic);
        Db db1 = TestUtil.newDb(dbName);
        db1.setTopics(topics);
        setAccessPrivateFields(dbController);
        Set<Topic> elecsticsearch = dbController.getDbTopics(dbName, httpServletResponse);
        assertEquals(Collections.emptySet(), elecsticsearch);
        when(dbRepository.findByName(dbName)).thenReturn(db1);
        elecsticsearch = dbController.getDbTopics(dbName, httpServletResponse);
        for (Topic anElecsticsearch : elecsticsearch) {
        	Topic tmp = TestUtil.newTopic(topicName);
        	tmp.setId(2);
            assertNotEquals(tmp, anElecsticsearch);
        }
        //dbController.deleteDb(dbName, httpServletResponse);
    }

    @Test(expected = NullPointerException.class)
    public void testPostReturnBody() throws IOException, NoSuchFieldException, IllegalAccessException {
        DbController dbController = new DbController();
        DbConfig dbConfig = getDbConfig();
        setAccessPrivateFields(dbController);
        PostReturnBody<DbConfig> db = dbController.createDb(dbConfig, mockBindingResult, httpServletResponse);
        assertNotNull(db);
    }

    @Test
    public void testVerifyConnection() throws IOException {
        DbController dbController = new DbController();
        DbConfig dbConfig = getDbConfig();
        PostReturnBody<DbConfig> dbConfigPostReturnBody = dbController.verifyDbConnection(dbConfig, httpServletResponse);
        assertEquals(null, dbConfigPostReturnBody);
    }

}
