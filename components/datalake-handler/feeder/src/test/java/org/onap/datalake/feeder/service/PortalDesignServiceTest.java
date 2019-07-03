/*
 * ============LICENSE_START=======================================================
 * ONAP : DCAE
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.DesignType;
import org.onap.datalake.feeder.domain.Portal;
import org.onap.datalake.feeder.domain.PortalDesign;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PortalDesignServiceTest {

    @Mock
    private DesignType designType;

    @Mock
    private ApplicationConfiguration applicationConfiguration;

    @InjectMocks
    private PortalDesignService designService;

    @Test(expected = RuntimeException.class)
    public void testDeploy() {
        when(designType.getId()).thenReturn("KIBANA_DB","ES_MAPPING");
        PortalDesign portalDesign = new PortalDesign();
        portalDesign.setDesignType(designType);
        portalDesign.setBody("jsonString");

        Portal portal = new Portal();
        Db db = new Db();
        db.setHost("localhost");
        portal.setDb(db);
        when(designType.getPortal()).thenReturn(portal);
        when(applicationConfiguration.getKibanaDashboardImportApi()).thenReturn("/api/kibana/dashboards/import?exclude=index-pattern");
        when(applicationConfiguration.getKibanaPort()).thenReturn(5601);
        designService.deploy(portalDesign);
        System.out.println();
    }
}