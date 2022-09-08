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

import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.service.PullService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiOperation;

/**
 * This controller controls DL data feeder.
 * 
 * @author Guobiao Mo
 *
 */

@RestController
@RequestMapping(value = "/feeder", produces = { MediaType.APPLICATION_JSON_VALUE })
public class FeederController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
    @Autowired
    private PullService pullService;

    @Autowired
    ApplicationConfiguration config;
    
    /**
     * @return message that application is started
     * @throws IOException 
     */
    @PostMapping("/start")
    @ResponseBody
	@ApiOperation(value="Start pulling data.")
    public String start() throws IOException {
    	log.info("Going to start DataLake feeder ...");
    	if(pullService.isRunning() == false) {
            pullService.start();
        	log.info("DataLake feeder started.");
        }else {
        	log.info("DataLake feeder already started.");        	
        }
        return "{\"running\": true}";
    }

    /**
     * @return message that application stop process is triggered
     */
    @PostMapping("/stop")
    @ResponseBody
	@ApiOperation(value="Stop pulling data.")
    public String stop() {
    	log.info("Going to stop DataLake feeder ...");
        if(pullService.isRunning() == true)
        {
            pullService.shutdown();
        	log.info("DataLake feeder is stopped.");
        }else {
        	log.info("DataLake feeder already stopped.");
        }
    	return "{\"running\": false}";
    }
    /**
     * @return feeder status
     */
    @GetMapping("/status")
	@ApiOperation(value="Retrieve feeder status.")
    public String status() {    	
    	String status = "Feeder is running: "+pullService.isRunning();
        log.info("sending feeder status ..." + status);//TODO we can send what topics are monitored, how many messages are sent, etc.

        return "{\"version\": \""+config.getDatalakeVersion()+"\", \"running\": "+pullService.isRunning()+"}";
    }
}
