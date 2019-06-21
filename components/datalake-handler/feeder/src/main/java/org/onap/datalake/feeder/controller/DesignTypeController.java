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

import java.util.ArrayList;
import java.util.List;

import org.onap.datalake.feeder.domain.DesignType;
import org.onap.datalake.feeder.dto.DesignTypeConfig;
import org.onap.datalake.feeder.service.DesignTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiOperation;

/**
 * This controller manages designType settings
 *
 * @author guochunmeng
 */
@RestController
@RequestMapping(value = "/designTypes", produces = { MediaType.APPLICATION_JSON_VALUE })
public class DesignTypeController {
	
	@Autowired
	private DesignTypeService designTypeService;
	
	@GetMapping("")
	@ResponseBody
	@ApiOperation(value="List all designTypes")
    public List<DesignTypeConfig> getDesignType() {
		return designTypeService.getDesignTypes();
    }
    
}
