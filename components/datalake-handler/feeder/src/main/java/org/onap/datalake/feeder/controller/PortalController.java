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
import org.onap.datalake.feeder.domain.Portal;
import org.onap.datalake.feeder.repository.PortalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This controller manages Portal settings
 *
 *
 * @author guochunmeng
 */

@RestController
@RequestMapping(value = "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
public class PortalController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PortalRepository portalRepository;

    @RequestMapping(value={"/createOrUpdate"}, method = RequestMethod.POST)
    public JSONObject createOrUpdate(@RequestBody Portal portal) {

        Portal d = new Portal();

        JSONObject result = new JSONObject();

        try {

            d.setName(portal.getName());
            d.setEnabled(portal.getEnabled());
            d.setLogin(portal.getLogin());
            d.setPass(portal.getPass());
            d.setHost(portal.getHost());
            d.setPort(portal.getPort());
            d.setRelatedDb(portalRepository.getRelatedDb(portal.getName()));
            log.info("-------createOrUpdate start-------");
            portalRepository.save(d);
            log.info("-------Successed-------");
            result.put("status", "success");
        } catch (Exception e) {

            result.put("status", "fail");
            log.debug("------Failed----------", e.getMessage());

        }

        return result;

    }


    @RequestMapping(value = "/delete/{name}", method = RequestMethod.DELETE)
    public JSONObject delete(@PathVariable String name){

        JSONObject result = new JSONObject();
        try {

            log.info("-------delete start--------");
            portalRepository.updateEnabled(name);
            log.info("-------Successed--------");
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "fail");
            log.debug("---------Failed--------", e.getMessage());

        }

        return result;

    }


    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public List<Portal> queryAll(){

        List<Portal> list = null;
        try {

            log.info("-------query start--------");
            list = (List<Portal>) portalRepository.queryAll();
            log.info("-------Successed--------");
        } catch (Exception e) {
            list = new ArrayList<>();
            log.debug("---------Failed--------", e.getMessage());
        }

        return list;

    }


    @RequestMapping(value={"/getName"}, method = RequestMethod.GET)
    public List<String> getName() {

        List<String>  dashboardNameList = null;
        try {

            log.info("-------getName start--------");
            dashboardNameList = portalRepository.getName();
            log.info("-------Successed--------");
        } catch (Exception e) {
            dashboardNameList = new ArrayList<>();
            log.debug("---------Failed--------", e.getMessage());

        }

        return dashboardNameList;

    }

}


