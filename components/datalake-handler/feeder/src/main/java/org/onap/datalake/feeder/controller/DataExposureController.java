/*
* ============LICENSE_START=======================================================
* ONAP : DataLake
* ================================================================================
* Copyright 2019-2020 China Mobile
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
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.json.JSONObject;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.config.DataLakeFactory;
import org.onap.datalake.feeder.domain.DataExposure;
import org.onap.datalake.feeder.service.DataExposureService;
import org.onap.datalake.feeder.util.SpringJdbcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

/**
 *
 * Data Exposure WS.
 * 
 * @author Guobiao Mo
 *
 */
@RestController
@RequestMapping("/exposure")
public class DataExposureController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataExposureService dataExposureService;

    @Autowired
    ApplicationConfiguration config;

    @Autowired
    DataLakeFactory dataLakeFactory;

    /**
     * @return message that application is started
     * @throws IOException
     * @throws SQLException
     */
    @PostMapping("/{serviceId}")
    @ApiOperation(value = "Datalake Data Exposure Service.")
    public ResponseEntity<String> serve(@PathVariable String serviceId, @RequestBody Map<String, String> requestMap)
            throws IOException, SQLException {

        log.info("Start Datalake Data Exposure Service ... requestMap=" + requestMap);
        DataExposure dataExposure = dataExposureService.getDataExposure(serviceId);

        String sqlTemplate = dataExposure.getSqlTemplate();
        StringSubstitutor sub = new StringSubstitutor(requestMap);
        String query = sub.replace(sqlTemplate);
        log.info("Start Datalake Data Exposure Service ... query=" + query);

        JdbcTemplate jdbcTemplate = dataLakeFactory.getDataLake(dataExposure.getDb().getPrestoCatalog());

        JSONObject jsonObject = SpringJdbcService.getJSONObject(jdbcTemplate, query);
        return new ResponseEntity<>(jsonObject.toString(), HttpStatus.OK);

    }
}
