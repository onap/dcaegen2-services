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

import org.onap.datalake.feeder.controller.domain.PostReturnBody;
import org.onap.datalake.feeder.domain.Design;
import org.onap.datalake.feeder.dto.DesignConfig;
import org.onap.datalake.feeder.repository.DesignRepository;
import org.onap.datalake.feeder.service.DesignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiOperation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;


/**
 * This controller manages design settings
 *
 * @author guochunmeng
 */
@RestController
@RequestMapping(value = "/designs", produces = MediaType.APPLICATION_JSON_VALUE)
public class DesignController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DesignRepository designRepository;
    
    @Autowired
    private DesignService designService;

	@PostMapping("")
	@ResponseBody
	@ApiOperation(value="Create a design.")
    public PostReturnBody<DesignConfig> createDesign(@RequestBody DesignConfig designConfig, BindingResult result, HttpServletResponse response) throws IOException {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing DesignConfig: "+result.toString());
			return null;
		}

		Design design = null;
		try {
			design = designService.fillDesignConfiguration(designConfig);
		} catch (Exception e) {
			log.debug("FillDesignConfiguration failed", e.getMessage());
			sendError(response, 400, "Error FillDesignConfiguration: "+e.getMessage());
			return null;
		}
		designRepository.save(design);
		log.info("Design save successed");
		return mkPostReturnBody(200, design);
    }


	@PutMapping("{id}")
	@ResponseBody
	@ApiOperation(value="Update a design.")
	public PostReturnBody<DesignConfig> updateDesign(@RequestBody DesignConfig designConfig, BindingResult result, @PathVariable Integer id, HttpServletResponse response) throws IOException {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing DesignConfig: "+result.toString());
			return null;
		}

		Design design = designService.getDesign(id);
		if (design != null) {
			try {
				designService.fillDesignConfiguration(designConfig, design);
			} catch (Exception e) {
				log.debug("FillDesignConfiguration failed", e.getMessage());
				sendError(response, 400, "Error FillDesignConfiguration: "+e.getMessage());
				return null;
			}
			designRepository.save(design);
			log.info("Design update successed");
			return mkPostReturnBody(200, design);
		} else {
			sendError(response, 400, "Design not found: "+id);
			return null;
		}

	}


	@DeleteMapping("/{id}")
	@ResponseBody
	@ApiOperation(value="delete a design.")
    public void deleteDesign(@PathVariable("id") Integer id, HttpServletResponse response) throws IOException{
		
		Design oldDesign = designService.getDesign(id);
		if (oldDesign == null) {
			sendError(response, 400, "design not found "+id);
		} else {
			designRepository.delete(oldDesign);
			response.setStatus(204);
		}
    }


	@GetMapping("")
	@ResponseBody
	@ApiOperation(value="List all Designs")
    public List<DesignConfig> queryAllDesign(){
		return designService.queryAllDesign();
    }


	@PostMapping("/deploy/{id}")
	@ResponseBody
	@ApiOperation(value="Design deploy")
	public Map<Integer, Boolean> deployDesign(@PathVariable Integer id, HttpServletResponse response) throws IOException {

		Optional<Design> designOptional = designRepository.findById(id);
		if (designOptional.isPresent()) {
			Design design = designOptional.get();
			return designService.deploy(design);
		} else {
			sendError(response, 400, "Design is null");
			return new HashMap<>();
		}
	}


	private PostReturnBody<DesignConfig> mkPostReturnBody(int statusCode, Design design) {
		PostReturnBody<DesignConfig> retBody = new PostReturnBody<>();
        retBody.setStatusCode(statusCode);
        retBody.setReturnBody(design.getDesignConfig());
        return retBody;
	}
    
	private void sendError(HttpServletResponse response, int sc, String msg) throws IOException {
		log.info(msg);
		response.sendError(sc, msg);		
	}
    
}