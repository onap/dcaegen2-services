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
import org.springframework.stereotype.Service;

/**
 * Service for portals
 *
 * @author guochunmeng
 *
 */
@Service
public class PortalService {

    public Portal fillPortalConfiguration(PortalConfig portalConfig)
    {
        Portal portal = new Portal();
        fillPortal(portalConfig, portal);
        return portal;
    }

    private void fillPortal(PortalConfig portalConfig, Portal portal) {

        portal.setName(portalConfig.getName());
        portal.setLogin(portalConfig.getLogin());
        portal.setPass(portalConfig.getPass());
        portal.setEnabled(portalConfig.getEnabled());
        portal.setHost(portalConfig.getHost());
        portal.setPort(portalConfig.getPort());
        portal.setDb(portalConfig.getDb());

    }
}
