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

import io.swagger.annotations.ApiOperation;
import org.onap.datalake.feeder.controller.domain.PostReturnBody;
import org.onap.datalake.feeder.domain.Portal;
import org.onap.datalake.feeder.dto.PortalConfig;
import org.onap.datalake.feeder.repository.PortalRepository;
import org.onap.datalake.feeder.service.PortalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This controller manages Portal settings
 *
 *
 * @author guochunmeng
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/portals", produces = { MediaType.APPLICATION_JSON_VALUE })
public class PortalController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PortalRepository portalRepository;

    @Autowired
    private PortalService portalService;

    @PutMapping("")
    @ResponseBody
    @ApiOperation("update portal")
    public PostReturnBody<PortalConfig> updatePortal(@RequestBody PortalConfig portalConfig, BindingResult result, HttpServletResponse response) throws IOException {

        if (result.hasErrors()) {
            sendError(response, 400, "Error binding PortalConfig: "+result.toString());
            return null;
        }

        Portal portal = null;
        try {
            portal = portalRepository.findById(portalConfig.getName()).get();
            log.info("Update portal "+portalConfig);
            portalService.fillPortalConfiguration(portalConfig, portal);
            portalRepository.save(portal);
            return mkPostReturnBody(200, portal);
        } catch (Exception e) {
            log.debug("Update or delete portal failed, Portal: "+portalConfig, e.getMessage());
            sendError(response, 400, "Error update or delete portal: "+portal);
            return null;
        }
    }


    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "List all portals")
    public List<PortalConfig> getPortals() {

        List<Portal> portalList = null;
        List<PortalConfig> portalConfigList = new ArrayList<>();
        portalList = (List<Portal>)portalRepository.findAll();
        if (portalList != null && portalList.size() > 0) {
            log.info("PortalList is not null");
            for(Portal portal : portalList) {
                portalConfigList.add(portal.getPortalConfig());
            }
        }
        return portalConfigList;
    }


    private void sendError(HttpServletResponse response, int sc, String msg) throws IOException {
        log.info(msg);
        response.sendError(sc, msg);
    }


    private PostReturnBody<PortalConfig> mkPostReturnBody(int statusCode, Portal portal) {
        PostReturnBody<PortalConfig> retBody = new PostReturnBody<>();
        retBody.setStatusCode(statusCode);
        retBody.setReturnBody(portal.getPortalConfig());
        return retBody;
    }

}