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
    public PostReturnBody<PortalConfig> update(@RequestBody PortalConfig portalConfig, BindingResult result, HttpServletResponse response) throws IOException {

        if (result.hasErrors()) {
            sendError(response, 400, "Error update or delete portal: "+result.toString());
            return null;
        }

        Portal portal = null;
        try {
            portal = portalRepository.findById(portalConfig.getName()).get();
            if (portalConfig.getEnabled() == false) {
                //delete
                portal.setPort(null);
                portal.setHost(null);
                portal.setLogin(null);
                portal.setPass(null);
                portal.setEnabled(false);
            }else {
                //update
                portalService.fillPortalConfiguration(portalConfig, portal);
            }
            portalRepository.save(portal);
            return mkPostReturnBody(200, portal);
        } catch (Exception e) {
            sendError(response, 400, "Error update or delete portal: "+result.toString());
            return null;
        }

    }


    @GetMapping("/{enabled}")
    @ResponseBody
    @ApiOperation(value = "List all portals where enabled = 1")
    public List<Portal> queryAll(@PathVariable Boolean enabled) {

        List<Portal> portalList = null;
        try {
            portalList = portalRepository.findByEnabled(enabled);
        } catch (Exception e) {
            portalList = new ArrayList<>();
            log.debug("---------Failed--------", e.getMessage());
        }
        return portalList;

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