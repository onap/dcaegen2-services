/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
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
package org.onap.datalake.feeder.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.text.StringSubstitutor;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.controller.domain.PostReturnBody;
import org.onap.datalake.feeder.domain.DataExposure;
import org.onap.datalake.feeder.dto.DataExposureConfig;
import org.onap.datalake.feeder.repository.DataExposureRepository;
import org.onap.datalake.feeder.service.DataExposureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiOperation;

/**
 * Data Exposure WS.
 *
 * @author Guobiao Mo
 *
 */

@RestController
@RequestMapping(value = "/exposure", produces = { MediaType.APPLICATION_JSON_VALUE })
public class DataExposureController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataExposureService dataExposureService;

    @Autowired
    private DataExposureRepository dataExposureRepository;

    @Autowired
    ApplicationConfiguration config;

    /**
     * @return message that application is started
     * @throws IOException
     * @throws SQLException
     */
    @PostMapping("/{serviceId}")
    @ResponseBody
    @ApiOperation(value = "Datalake Data Exposure Service.")
    public HashMap<String, Object> serve(@PathVariable String serviceId, @RequestBody Map<String, String> requestMap, BindingResult bindingResult, HttpServletResponse response) throws IOException, SQLException {
        log.info("Going to start Datalake Data Exposure Service ... requestMap=" + requestMap);

        HashMap<String, Object> ret = new HashMap<>();
        ret.put("request", requestMap);

        DataExposure dataExposure = dataExposureService.getDataExposure(serviceId);

        String sqlTemplate = dataExposure.getSqlTemplate();

        StringSubstitutor sub = new StringSubstitutor(requestMap);

        String query = sub.replace(sqlTemplate);

        log.info("Going to start Datalake Data Exposure Service ... query=" + query);

        //https://prestodb.io/docs/current/installation/jdbc.html

        String url = String.format("jdbc:presto://dl-presto:8080/%s/%s", dataExposure.getDb().getPrestoCatalog(), dataExposure.getDb().getDatabase());
        Properties properties = new Properties();
        properties.setProperty("user", "test");
        //properties.setProperty("password", "secret");
        //properties.setProperty("SSL", "true");
        Connection connection = DriverManager.getConnection(url, properties);

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        ArrayList<HashMap<String, Object>> result = new ArrayList<>();

        int count =0;
        while (rs.next()) {
            HashMap<String, Object> entry = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String label = meta.getColumnLabel(i);
                Object value = rs.getObject(i);

                entry.put(label, value);
                log.info(label + "\t" + value);
            }

            result.add(entry);
            count++;
        }
        ret.put("result", result);
        ret.put("result_count", count);

        return ret;
    }

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "Datalake Data Exposure list")
    public List<DataExposureConfig> queryAllDataExposure() {
        return dataExposureService.queryAllDataExposure();
    }

    @GetMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "Get Detail of DataExposure")
    public DataExposureConfig queryAllDataExposure(@PathVariable String id, HttpServletResponse response) throws IOException {

        log.info("Get Detail of DataExposure Starting.....");
        DataExposure oldDataExposure = dataExposureService.getDataExposureById(id);
        if (oldDataExposure == null) {
            sendError(response, 400, "DataExposure not found, ID: "+id);
            return null;
        } else {
            log.info("ResponseBody......" + oldDataExposure.getDataExposureConfig());
            return oldDataExposure.getDataExposureConfig();
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @ApiOperation(value="delete a dataExposure.")
    public void deleteKafka(@PathVariable String id, HttpServletResponse response) throws IOException{

        DataExposure oldDataExposure = dataExposureService.getDataExposureById(id);
        if (oldDataExposure == null) {
            sendError(response, 400, "DataExposure not found, ID: "+id);
        } else {
            dataExposureRepository.delete(oldDataExposure);
            response.setStatus(204);
        }
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value="Create a DataExposure.")
    public PostReturnBody<DataExposureConfig> createDataExposure(@RequestBody DataExposureConfig dataExposureConfig, BindingResult result, HttpServletResponse response) throws IOException {

        if (result.hasErrors()) {
            sendError(response, 400, "Error parsing DataExposureConfig : "+result.toString());
            return null;
        }

        DataExposure oldDataExposure = dataExposureService.getDataExposureById(dataExposureConfig.getId());

        if (oldDataExposure != null) {
            sendError(response, 400, "DataExposure is exist "+dataExposureConfig.getId());
            return null;
        } else {
            DataExposure dataExposure = null;
            try {
                dataExposure = dataExposureService.fillDataExposureConfiguration(dataExposureConfig);
            } catch (Exception e) {
                log.debug("FillDataExposureConfiguration failed", e.getMessage());
                sendError(response, 400, "Error FillDataExposureConfiguration: "+e.getMessage());
                return null;
            }
            dataExposureRepository.save(dataExposure);
            log.info("Kafka save successed");
            return mkPostReturnBody(200, dataExposure);
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    @ApiOperation(value="Update a DataExposure.")
    public PostReturnBody<DataExposureConfig> updateDataExposure(@RequestBody DataExposureConfig dataExposureConfig, BindingResult result, @PathVariable String id, HttpServletResponse response) throws IOException {

        if (result.hasErrors()) {
            sendError(response, 400, "Error parsing DataExposureConfig : "+result.toString());
            return null;
        }

        DataExposure oldDataExposure = dataExposureService.getDataExposureById(id);

        if (oldDataExposure == null) {
            sendError(response, 400, "DataExposure not found: "+id);
            return null;
        } else {
            try {
                dataExposureService.fillDataExposureConfiguration(dataExposureConfig, oldDataExposure);
            } catch (Exception e) {
                log.debug("FillDataExposureConfiguration failed", e.getMessage());
                sendError(response, 400, "Error FillDataExposureConfiguration: "+e.getMessage());
                return null;
            }
            dataExposureRepository.save(oldDataExposure);
            log.info("DataExposure update successed");
            return mkPostReturnBody(200, oldDataExposure);
        }
    }

    private PostReturnBody<DataExposureConfig> mkPostReturnBody(int statusCode, DataExposure dataExposure) {
        PostReturnBody<DataExposureConfig> retBody = new PostReturnBody<>();
        retBody.setStatusCode(statusCode);
        retBody.setReturnBody(dataExposure.getDataExposureConfig());
        return retBody;
    }

    private void sendError(HttpServletResponse response, int sc, String msg) throws IOException {
        log.info(msg);
        response.sendError(sc, msg);
    }
}
