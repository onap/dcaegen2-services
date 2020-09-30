/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake DES
 * ================================================================================
 * Copyright 2020 China Mobile. All rights reserved.
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

package org.onap.datalake.des.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.des.domain.DataExposure;
import org.onap.datalake.des.domain.Db;
import org.onap.datalake.des.domain.DbType;
import org.onap.datalake.des.dto.DataExposureConfig;
import org.onap.datalake.des.dto.DbConfig;
import org.onap.datalake.des.repository.DataExposureRepository;
import org.onap.datalake.des.repository.DbRepository;
import org.springframework.context.ApplicationContext;

/**
 * Test DB exposure Service.
 *
 * @author Kai Lu
 */
@RunWith(MockitoJUnitRunner.class)
public class DataExposureServiceTest {

    @Mock
    private DbType dbType;

    @Mock
    private ApplicationContext context;

    @Mock
    private DbRepository dbRepository;

    @Mock
    private DataExposureRepository dataExposureRepository;

    @InjectMocks
    private DataExposureService dataExposureService;

    /**
     * Generate data exposure config.
     *
     * @return DataExposureConfig object
     *
     */
    public DataExposureConfig getDataExposureConfig() {
        DataExposureConfig deConfig = new DataExposureConfig();
        deConfig.setDbId(3);
        deConfig.setId("test");
        deConfig.setNote("testSql");
        deConfig.setSqlTemplate("select name from datalake");
        return deConfig;
    }

    /**
     * Generate DB config.
     *
     * @return DbConfig object
     *
     */
    public DbConfig getDbConfig() {
        DbConfig dbConfig = new DbConfig();
        dbConfig.setId(1);
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

    @Test
    public void testQueryAllDataExposure() {
        Db newdb = new Db();
        DbConfig dbConfig = getDbConfig();
        newdb.setName(dbConfig.getName());
        newdb.setHost(dbConfig.getHost());
        newdb.setPort(dbConfig.getPort());
        newdb.setEnabled(dbConfig.isEnabled());
        newdb.setLogin(dbConfig.getLogin());
        newdb.setPass(dbConfig.getPass());
        newdb.setEncrypt(dbConfig.isEncrypt());
        DataExposureConfig deConfig = getDataExposureConfig();
        DataExposure de = new DataExposure();
        de.setDb(newdb);
        de.setId(deConfig.getId());
        de.setNote(deConfig.getNote());
        de.setSqlTemplate(deConfig.getSqlTemplate());
        List<DataExposure> deList = new ArrayList<>();
        deList.add(de);
        when(dataExposureRepository.findAll()).thenReturn(deList);
        List<DataExposureConfig> deConfigList = dataExposureService.queryAllDataExposure();
        assertEquals(de.getId(), deConfigList.get(0).getId());
    }

    @Test
    public void testFillDataExposureConfiguration() {
        Db newdb = new Db();
        DbConfig dbConfig = getDbConfig();
        newdb.setName(dbConfig.getName());
        newdb.setHost(dbConfig.getHost());
        newdb.setPort(dbConfig.getPort());
        newdb.setEnabled(dbConfig.isEnabled());
        newdb.setLogin(dbConfig.getLogin());
        newdb.setPass(dbConfig.getPass());
        newdb.setEncrypt(dbConfig.isEncrypt());
        DataExposureConfig deConfig = getDataExposureConfig();
        when(dbRepository.findById(deConfig.getDbId())).thenReturn(Optional.of(newdb));
        DataExposure de = dataExposureService.fillDataExposureConfiguration(deConfig);
        assertEquals(de.getId(), deConfig.getId());
    }

    @Test
    public void testFillDataExposureConfigurationWithTwoPara() {
        Db newdb = new Db();
        DbConfig dbConfig = getDbConfig();
        newdb.setName(dbConfig.getName());
        newdb.setHost(dbConfig.getHost());
        newdb.setPort(dbConfig.getPort());
        newdb.setEnabled(dbConfig.isEnabled());
        newdb.setLogin(dbConfig.getLogin());
        newdb.setPass(dbConfig.getPass());
        newdb.setEncrypt(dbConfig.isEncrypt());
        DataExposureConfig deConfig = getDataExposureConfig();
        when(dbRepository.findById(deConfig.getDbId())).thenReturn(Optional.of(newdb));
        DataExposure de = new DataExposure();
        de.setDb(newdb);
        de.setId(deConfig.getId());
        de.setNote(deConfig.getNote());
        de.setSqlTemplate(deConfig.getSqlTemplate());
        dataExposureService.fillDataExposureConfiguration(deConfig, de);
    }

}
