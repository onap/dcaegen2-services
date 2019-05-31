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
import org.onap.datalake.feeder.domain.PortalDesign;
import org.onap.datalake.feeder.dto.PortalDesignConfig;
import org.onap.datalake.feeder.repository.PortalDesignRepository;
import org.onap.datalake.feeder.service.PortalDesignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiOperation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;


/**
 * This controller manages portalDesign settings
 *
 * @author guochunmeng
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/portalDesigns", produces = MediaType.APPLICATION_JSON_VALUE)
public class PortalDesignController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PortalDesignRepository portalDesignRepository;
    
    @Autowired
    private PortalDesignService portalDesignService;

	@PostMapping("")
	@ResponseBody
	@ApiOperation(value="Create a portalDesign.")
    public PostReturnBody<PortalDesignConfig> createPortalDesign(@RequestBody PortalDesignConfig portalDesignConfig, BindingResult result, HttpServletResponse response) throws IOException {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing PortalDesignConfig: "+result.toString());
			return null;
		}

		PortalDesign portalDesign = null;
		try {
			portalDesign = portalDesignService.fillPortalDesignConfiguration(portalDesignConfig);
		} catch (Exception e) {
			log.debug("FillPortalDesignConfiguration failed", e.getMessage());
			sendError(response, 400, "Error FillPortalDesignConfiguration: "+e.getMessage());
			return null;
		}
		portalDesignRepository.save(portalDesign);
		log.info("PortalDesign save successed");
		return mkPostReturnBody(200, portalDesign);
    }


	@PutMapping("{id}")
	@ResponseBody
	@ApiOperation(value="Update a portalDesign.")
	public PostReturnBody<PortalDesignConfig> updatePortalDesign(@RequestBody PortalDesignConfig portalDesignConfig, BindingResult result, @PathVariable Integer id, HttpServletResponse response) throws IOException {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing PortalDesignConfig: "+result.toString());
			return null;
		}

		PortalDesign portalDesign = portalDesignService.getPortalDesign(id);
		if (portalDesign != null) {
			try {
				portalDesignService.fillPortalDesignConfiguration(portalDesignConfig, portalDesign);
			} catch (Exception e) {
				log.debug("FillPortalDesignConfiguration failed", e.getMessage());
				sendError(response, 400, "Error FillPortalDesignConfiguration: "+e.getMessage());
				return null;
			}
			portalDesignRepository.save(portalDesign);
			log.info("PortalDesign update successed");
			return mkPostReturnBody(200, portalDesign);
		} else {
			sendError(response, 400, "PortalDesign not found: "+id);
			return null;
		}

	}


	@DeleteMapping("/{id}")
	@ResponseBody
	@ApiOperation(value="delete a portalDesign.")
    public void deletePortalDesign(@PathVariable("id") Integer id, HttpServletResponse response) throws IOException{
		
		PortalDesign oldPortalDesign= portalDesignService.getPortalDesign(id);
		if (oldPortalDesign == null) {
			sendError(response, 400, "portalDesign not found "+id);
		} else {
			portalDesignRepository.delete(oldPortalDesign);
			response.setStatus(204);
		}
    }


	@GetMapping("")
	@ResponseBody
	@ApiOperation(value="List all PortalDesigns")
    public List<PortalDesignConfig> queryAllPortalDesign(){

		List<PortalDesign> portalDesignList = null;
		List<PortalDesignConfig> portalDesignConfigList = new ArrayList<>();
		portalDesignList = (List<PortalDesign>) portalDesignRepository.findAll();
		if (portalDesignList != null && portalDesignList.size() > 0) {
			log.info("PortalDesignList is not null");
			for (PortalDesign portalDesign : portalDesignList) {
				portalDesignConfigList.add(portalDesign.getPortalDesignConfig());
			}
		}
		return portalDesignConfigList;
    }


	@PostMapping("/deploy/{id}")
	@ResponseBody
	@ApiOperation(value="PortalDesign deploy")
	public void deployPortalDesign(@PathVariable Integer id, HttpServletResponse response) throws IOException {

		PortalDesign portalDesign = null;
		try {
			portalDesign = portalDesignRepository.findById(id).get();
			if (portalDesign.getDesignType() != null && portalDesign.getDesignType().getName().startsWith("Kibana")) {
				boolean flag = portalDesignService.deployKibanaImport(portalDesign);
				if (flag) {
					sendError(response, 400, "DeployPortalDesign failed, id: "+id);
				}
			} else if (portalDesign.getDesignType() != null && portalDesign.getDesignType().getName().startsWith("Elasticsearch")) {
				//TODO Elasticsearch template import
				sendError(response, 400, "DeployPortalDesign failed, id: "+id);
			} else {
				//TODO Druid import
				sendError(response, 400, "DeployPortalDesign failed, id: "+id);
			}
			portalDesign.setSubmitted(true);
			portalDesignRepository.save(portalDesign);
			response.setStatus(204);
		} catch (Exception e) {
			log.debug("PortalDesign is null", e.getMessage());
			sendError(response, 400, "PortalDesign not found, id: "+id);
		}

	}


	private PostReturnBody<PortalDesignConfig> mkPostReturnBody(int statusCode, PortalDesign portalDesign) {
		PostReturnBody<PortalDesignConfig> retBody = new PostReturnBody<>();
        retBody.setStatusCode(statusCode);
        retBody.setReturnBody(portalDesign.getPortalDesignConfig());
        return retBody;
	}
    
	private void sendError(HttpServletResponse response, int sc, String msg) throws IOException {
		log.info(msg);
		response.sendError(sc, msg);		
	}
    
}
