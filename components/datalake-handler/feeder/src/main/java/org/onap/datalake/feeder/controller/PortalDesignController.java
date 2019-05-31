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
	@ApiOperation(value="Create a new portalDesign.")
    public PostReturnBody<PortalDesignConfig> createOrUpdate(@RequestBody PortalDesignConfig portalDesignConfig, BindingResult result, HttpServletResponse response) throws IOException {

		if (result.hasErrors()) {
			sendError(response, 400, "Error parsing PortalDesignConfig: "+result.toString());
			return null;
		}

		PortalDesign OldPortalDesign = portalDesignRepository.findByName(portalDesignConfig.getName());

		if (OldPortalDesign != null) {
			sendError(response, 400, "PortalDesign already exists "+OldPortalDesign.getName());
			return null;
		} else {
			PortalDesign portalDesign = portalDesignService.fillPortalDesignConfiguration(portalDesignConfig);
			
			portalDesignRepository.save(portalDesign);
			return mkPostReturnBody(200, portalDesign);
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

			log.info("-------query start--------");
			portalDesignList = (List<PortalDesign>) portalDesignRepository.findAll();
			log.info("-------Successed--------");
		} catch (Exception e) {
			portalDesignList = new ArrayList<>();
			log.debug("---------Failed--------", e.getMessage());
		}

		return portalDesignList;

    }


	@GetMapping("{id}")
	@ResponseBody
	@ApiOperation(value="Get host and port")
	public String getHostAndPort(@PathVariable Integer id){

		PortalDesign portalDesign = portalDesignRepository.findById(id).get();

		String portalHost = portalDesign.getDesignType().getPortal().getHost();
		Integer portalPort = portalDesign.getDesignType().getPortal().getPort();

		String hostAndPort = portalHost+","+portalPort;
		return hostAndPort;

	}

    
	private PostReturnBody<PortalDesignConfig> mkPostReturnBody(int statusCode, PortalDesign portalDesign)
	{
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
