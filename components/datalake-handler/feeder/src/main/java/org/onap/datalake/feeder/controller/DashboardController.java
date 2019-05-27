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

import org.json.JSONObject;
import org.onap.datalake.feeder.domain.Dashboard;
import org.onap.datalake.feeder.repository.DashboardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * This controller manages Dashboard settings
 *
 *
 * @author guochunmeng
 */

@RestController
@RequestMapping(value = "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
public class DashboardController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DashboardRepository dashboardRepository;

    @RequestMapping(value={"/createOrUpdate"}, method = RequestMethod.POST)
    public JSONObject createOrUpdate(@RequestBody Dashboard dashboard) {

        JSONObject result = new JSONObject();
        try {

            log.info("Dashboard save or modify");
            dashboardRepository.save(dashboard);
            result.put("status", "success");
        } catch (Exception e) {
            log.debug("Save or modify failed");
            result.put("status", "fail");
            e.printStackTrace();
        }

        return result;

    }


    @RequestMapping(value = "/delete/{name}", method = RequestMethod.DELETE)
    public JSONObject delete(@PathVariable String name){

        JSONObject result = new JSONObject();
        try {

            log.info("Dashboard delete");
            dashboardRepository.deleteById(name);
            result.put("status", "success");
        } catch (Exception e) {
            log.debug("Failed--------");
            result.put("status", "fail");
            e.printStackTrace();
        }

        return result;

    }


    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public List<Dashboard> queryAll(){

        List<Dashboard> list = null;
        try {

            list = (List<Dashboard>) dashboardRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }

    @RequestMapping(value={"/getDbName"}, method = RequestMethod.GET)
    public List<String> getDbName() {

        List<String>  dbNameList = null;
        try {

            dbNameList = dashboardRepository.getDbName();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dbNameList;

    }

}


