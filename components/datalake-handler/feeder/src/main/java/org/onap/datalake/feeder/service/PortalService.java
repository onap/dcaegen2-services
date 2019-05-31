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

package org.onap.datalake.feeder.service;


import org.onap.datalake.feeder.domain.Portal;
import org.onap.datalake.feeder.dto.PortalConfig;
import org.onap.datalake.feeder.repository.PortalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for portals
 *
 * @author guochunmeng
 *
 */
@Service
public class PortalService {

    @Autowired
    private PortalRepository portalRepository;

    public Portal fillPortalConfiguration(PortalConfig portalConfig)
    {
        Portal portal = new Portal();
        fillPortal(portalConfig, portal);
        return portal;
    }
    public void fillPortalConfiguration(PortalConfig portalConfig, Portal portal)
    {
        fillPortal(portalConfig, portal);
    }

    private void fillPortal(PortalConfig portalConfig, Portal portal) {

        portal.setName(portalConfig.getName());
        portal.setLogin(portalConfig.getLogin());
        portal.setPass(portalConfig.getPass());
        portal.setEnabled(portalConfig.getEnabled());
        portal.setHost(portalConfig.getHost());
        portal.setPort(portalConfig.getPort());

    }


    public List<String> listNames(boolean enabled){

        List<String> names = new ArrayList<>();
        Iterable<Portal> ret = portalRepository.findByEnabled(enabled);
        for(Portal portal:ret) {
            names.add(portal.getName());
        }

        return names;
    }

}
