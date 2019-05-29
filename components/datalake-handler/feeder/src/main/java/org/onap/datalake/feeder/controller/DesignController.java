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

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.onap.datalake.feeder.domain.PortalDesign;
import org.onap.datalake.feeder.repository.DesignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This controller manages Design settings
 *
 * @author guochunmeng
 */

@RestController
@RequestMapping(value = "/template", produces = MediaType.APPLICATION_JSON_VALUE)
public class DesignController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DesignRepository designRepository;


    @RequestMapping(value={"/createOrUpdate"}, method = RequestMethod.POST)
    public JSONObject createOrUpdate(@RequestBody PortalDesign portalDesign) {

        JSONObject result = new JSONObject();
        try {
            designRepository.save(portalDesign);
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "fail");
            log.debug("------Failed----------", e.getMessage());
        }

        return result;

    }


    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public JSONObject delete(@PathVariable Integer id){

        JSONObject result = new JSONObject();
        try {
            designRepository.deleteById(id);
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "fail");
            log.debug("------Failed----------", e.getMessage());
        }

        return result;

    }


    @RequestMapping(value = "/queryAll", method = RequestMethod.GET)
    public List<PortalDesign> queryAll(){

        List<PortalDesign> list = null;
        try {
            list = (List<PortalDesign>) designRepository.findAll();
        } catch (Exception e) {
            list = new ArrayList<>();
            log.debug("------Failed----------", e.getMessage());
        }

        return list;

    }


    @RequestMapping(value={"/deploy/{id}"}, method = RequestMethod.GET)
    public JSONObject deploy(@PathVariable Integer id) {

        JSONObject result = new JSONObject();
        String  hostAndPort = null; //eg: localhost,5601
        try {

            hostAndPort = designRepository.deployById(id);
            Map map = new HashMap<>();
            if (StringUtils.isNotBlank(hostAndPort)) {

                String[] paths = hostAndPort.split(",");
                map.put("host", paths[0]);
                map.put("port", paths[1]);
            }
            result.put("source", map);
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "fail");
            log.debug("------Failed----------", e.getMessage());
        }

        return result;

    }


    @RequestMapping(value={"/getTopicName"}, method = RequestMethod.GET)
    public List<String> getTopicName() {

        List<String>  topicNameList = null;
        try {

            topicNameList = designRepository.getTopicName();
        } catch (Exception e) {
            topicNameList = new ArrayList<>();
            log.debug("------Failed----------", e.getMessage());
        }

        return topicNameList;

    }


    @RequestMapping(value={"/getTemplateTypeName"}, method = RequestMethod.GET)
    public List<String> getTemplateTypeName() {

        List<String>  typeNameList = null;
        try {

            typeNameList = designRepository.getDesignType();
        } catch (Exception e) {
            typeNameList = new ArrayList<>();
            log.debug("------Failed----------", e.getMessage());
        }

        return typeNameList;

    }

}
