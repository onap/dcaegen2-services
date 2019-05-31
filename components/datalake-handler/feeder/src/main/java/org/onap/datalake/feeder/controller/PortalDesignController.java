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

import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.controller.domain.PostReturnBody;
import org.onap.datalake.feeder.domain.Portal;
import org.onap.datalake.feeder.domain.PortalDesign;
import org.onap.datalake.feeder.dto.PortalDesignConfig;
import org.onap.datalake.feeder.repository.PortalDesignRepository;
import org.onap.datalake.feeder.service.PortalDesignService;
import org.onap.datalake.feeder.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiOperation;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;


/**
 * This controller manages portalDesign settings
 *
 * @author guochunmeng
 */

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
    public PostReturnBody<PortalDesignConfig> createPortalDesign(@RequestBody PortalDesignConfig portalDesignConfig, BindingResult result, HttpServletResponse response) throws Exception {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing PortalDesignConfig: "+result.toString());
			return null;
		}

		if (portalDesignConfig.getId() == null) {
			PortalDesign portalDesign = portalDesignService.fillPortalDesignConfiguration(portalDesignConfig);
			portalDesignRepository.save(portalDesign);
			return mkPostReturnBody(200, portalDesign);
		} else {
			sendError(response, 400, "PortalDesign already exists: "+portalDesignConfig.getId());
			return null;
		}

    }


	@PutMapping("{id}")
	@ResponseBody
	@ApiOperation(value="Update a portalDesign.")
	public PostReturnBody<PortalDesignConfig> updatePortalDesign(@RequestBody PortalDesignConfig portalDesignConfig, BindingResult result, @PathVariable Integer id, HttpServletResponse response) throws Exception {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing PortalDesignConfig: "+result.toString());
			return null;
		}

		PortalDesign portalDesign = portalDesignService.getPortalDesign(id);
		if (portalDesign != null) {
			portalDesignService.fillPortalDesignConfiguration(portalDesignConfig, portalDesign);
			portalDesignRepository.save(portalDesign);
			return mkPostReturnBody(200, portalDesign);
		} else {
			sendError(response, 400, "PortalDesign not found: "+id);
			return null;
		}

	}


	@DeleteMapping("/{id}")
	@ResponseBody
	@ApiOperation(value="delete a portalDesign.")
    public void delete(@PathVariable("id") Integer id, HttpServletResponse response) throws IOException{
		
		PortalDesign oldPortalDesign= portalDesignService.getPortalDesign(id);
		if (oldPortalDesign == null) {
			sendError(response, 404, "portalDesign not found "+id);
		} else {
			portalDesignRepository.delete(oldPortalDesign);
			response.setStatus(204);
		}
    }


	@GetMapping("")
	@ResponseBody
	@ApiOperation(value="List all PortalDesigns")
    public List<PortalDesign> queryAllPortalDesign(){

		List<PortalDesign> portalDesignList = null;

		try {
			portalDesignList = (List<PortalDesign>) portalDesignRepository.findAll();
		} catch (Exception e) {
			log.debug("---------Failed--------", e.getMessage());
		}

		return portalDesignList;

    }


	@PostMapping("{id}")
	@ResponseBody
	@ApiOperation(value="Template deploy")
	public PostReturnBody<PortalDesignConfig> templateDeploy(@RequestBody PortalDesignConfig portalDesignConfig, @PathVariable Integer id, BindingResult result, HttpServletResponse response) throws IOException {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing PortalDesignConfig: "+result.toString());
			return null;
		}

		String requestBody = portalDesignConfig.getBody();
		PortalDesign portalDesign = null;
		try {
			portalDesign = portalDesignRepository.findById(id).get();
			Portal portal = portalDesign.getDesignType().getPortal();
			String portalHost = portal.getHost();
			Integer portalPort = portal.getPort();
			String url = "";

			if (portalHost == null || portalPort == null) {
				String dbHost = portal.getDb().getHost();
				Integer dbPort = portal.getDb().getPort();
				url = portalDesignService.kibanaImportUrl(dbHost, dbPort);
			} else {
				url = portalDesignService.kibanaImportUrl(portalHost, portalPort);
			}

			String kibanaResponse = HttpClientUtil.sendPostToKibana(url, requestBody);
			Boolean flag = portalDesignService.isKibanaResponse(kibanaResponse);

			if (flag) {
				sendError(response, 400, "Deploy failed: "+kibanaResponse);
				return null;
			}
			return mkPostReturnBody(200, portalDesign);
		} catch (Exception e) {
			sendError(response, 400, "PortalDesign not found: "+id);
			return null;
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
