/*-
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE DES
 * ================================================================================
 * Copyright (C) 2020 China Mobile. All rights reserved.
 * Copyright (C) 2022 Wipro Limited.
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

package org.onap.datalake.des.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.des.domain.DataExposure;
import org.onap.datalake.des.domain.Db;
import org.onap.datalake.des.domain.DbType;
import org.onap.datalake.des.dto.DataExposureConfig;
import org.onap.datalake.des.repository.DataExposureRepository;
import org.onap.datalake.des.service.DataExposureService;
import org.springframework.validation.BindingResult;

/**
 * Test Data Exposure Controller.
 *
 * @author Kai Lu
 */
@RunWith(MockitoJUnitRunner.class)
public class DataExposureControllerTest {

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private DataExposureRepository dataExposureRepository;

    @Mock
    private BindingResult mockBindingResult;

    @Mock
    private DataExposureService dataExposureService;

    @InjectMocks
    private DataExposureController dataExposureController;

    /**
     * Generate data exposure config.
     *
     * @return DataExposureConfig object
     *
     */
    public DataExposureConfig getDataExposureConfig() {
        DataExposureConfig dataExposureConfig = new DataExposureConfig();
        dataExposureConfig.setDbId(1);
        dataExposureConfig.setId("1");
        dataExposureConfig.setNote("note");
        dataExposureConfig.setSqlTemplate("sqlTemplate");
        return dataExposureConfig;
    }

    /**
     * Generate data exposure.
     *
     * @return DataExposure object
     *
     */
    public DataExposure getDataExposure() {
        DbType dbType = new DbType("ES", "Elasticsearch");
        Db db = new Db();
        db.setId(1);
        db.setDbType(dbType);
        db.setDatabase("Elasticsearch");

        DataExposure dataExposure = new DataExposure();
        dataExposure.setId("1");
        dataExposure.setNote("note");
        dataExposure.setSqlTemplate("sqlTemplate");
        dataExposure.setDb(db);
        return dataExposure;
    }

    @Test(expected = NullPointerException.class)
    public void testServeNull()
    throws IOException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException, SQLException {
        DataExposureController dataExposureController = new DataExposureController();
        String serviceId = "test";
        Map < String, String > requestMap = new HashMap < String, String > ();
        requestMap.put("name", "oteNB5309");
        HashMap < String, Object > result = dataExposureController.serve(serviceId, requestMap, mockBindingResult,
            httpServletResponse);
        assertEquals(null, result);
        when(mockBindingResult.hasErrors()).thenReturn(true);
    }

    @Test(expected = SQLException.class)
    public void testServeException()
    throws IOException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException, SQLException {
        String serviceId = "test";
        Map < String, String > requestMap = new HashMap < String, String > ();
        requestMap.put("name", "oteNB5309");

        DataExposure dataExposure = getDataExposure();
        when(dataExposureService.getDataExposure(serviceId)).thenReturn(dataExposure);
        dataExposureController.serve(serviceId, requestMap, mockBindingResult,
            httpServletResponse);
    }

    @Test
    public void testQueryAllDataExposure() {
        DataExposureConfig dataExposureConfig = getDataExposureConfig();
        List < DataExposureConfig > dataExposureList = new ArrayList < > ();
        dataExposureList.add(dataExposureConfig);
        when(dataExposureService.queryAllDataExposure()).thenReturn(dataExposureList);
        assertEquals(dataExposureList, dataExposureController.queryAllDataExposure());
    }

    @Test
    public void TestQueryAllDataExposureByIdNull() throws IOException {
        when(dataExposureService.getDataExposureById("1")).thenReturn(null);
        assertEquals(null, dataExposureController.queryAllDataExposure("1", httpServletResponse));
    }

    @Test
    public void TestQueryAllDataExposureById() throws IOException {
        DataExposure dataExposure = getDataExposure();
        when(dataExposureService.getDataExposureById("1")).thenReturn(dataExposure);
        dataExposureController.queryAllDataExposure("1", httpServletResponse);
    }

    @Test
    public void testCreateDataExposureNull() throws IOException {
        DataExposure dataExposure = getDataExposure();
        DataExposureConfig dataExposureConfig = getDataExposureConfig();
        when(dataExposureService.getDataExposureById("1")).thenReturn(dataExposure);
        assertEquals(null, dataExposureController.createDataExposure(dataExposureConfig, mockBindingResult, httpServletResponse));
    }

    @Test
    public void testCreateDataExposure() throws IOException {
        DataExposure dataExposure = getDataExposure();
        DataExposureConfig dataExposureConfig = getDataExposureConfig();
        when(dataExposureService.getDataExposureById("1")).thenReturn(null);
        when(dataExposureService.fillDataExposureConfiguration(dataExposureConfig)).thenReturn(dataExposure);
        dataExposureController.createDataExposure(dataExposureConfig, mockBindingResult, httpServletResponse);
    }

    @Test
    public void testCreateDataExposureException() throws IOException {
        DataExposureConfig dataExposureConfig = getDataExposureConfig();
        when(dataExposureService.getDataExposureById("1")).thenReturn(null);
        when(dataExposureService.fillDataExposureConfiguration(dataExposureConfig)).thenThrow(NullPointerException.class);
        assertEquals(null, dataExposureController.createDataExposure(dataExposureConfig, mockBindingResult, httpServletResponse));
    }

    @Test
    public void testCreateDataExposureError() throws IOException {
        DataExposureConfig dataExposureConfig = getDataExposureConfig();
        when(mockBindingResult.hasErrors()).thenReturn(true);
        assertEquals(null, dataExposureController.createDataExposure(dataExposureConfig, mockBindingResult, httpServletResponse));
    }

    @Test
    public void testUpdateDataExposureNull() throws IOException {
        DataExposureConfig dataExposureConfig = getDataExposureConfig();
        when(dataExposureService.getDataExposureById("1")).thenReturn(null);
        assertEquals(null, dataExposureController.updateDataExposure(dataExposureConfig, mockBindingResult, "1", httpServletResponse));
    }

    @Test
    public void testUpdateDataExposure() throws IOException {
        DataExposure dataExposure = getDataExposure();
        DataExposureConfig dataExposureConfig = getDataExposureConfig();
        when(dataExposureService.getDataExposureById("1")).thenReturn(dataExposure);
        dataExposureController.updateDataExposure(dataExposureConfig, mockBindingResult, "1", httpServletResponse);
    }

    @Test
    public void testUpdateDataExposureException() throws IOException {
        DataExposure dataExposure = getDataExposure();
        DataExposureConfig dataExposureConfig = getDataExposureConfig();
        when(dataExposureService.getDataExposureById("1")).thenReturn(dataExposure);
        doThrow(NullPointerException.class).when(dataExposureService).fillDataExposureConfiguration(dataExposureConfig, dataExposure);
        assertEquals(null, dataExposureController.updateDataExposure(dataExposureConfig, mockBindingResult, "1", httpServletResponse));
    }

    @Test
    public void testUpdateDataExposureError() throws IOException {
        DataExposureConfig dataExposureConfig = getDataExposureConfig();
        when(mockBindingResult.hasErrors()).thenReturn(true);
        assertEquals(null, dataExposureController.updateDataExposure(dataExposureConfig, mockBindingResult, "1", httpServletResponse));
    }

}
