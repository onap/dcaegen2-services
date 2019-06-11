/*
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.controller.domain.PostReturnBody;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Portal;
import org.onap.datalake.feeder.dto.PortalConfig;
import org.onap.datalake.feeder.repository.PortalRepository;
import org.onap.datalake.feeder.service.PortalService;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PortalControllerTest {

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private BindingResult mockBindingResult;

    @Mock
    private PortalRepository portalRepository;

    @InjectMocks
    private PortalService portalService;

    @Before
    public void setupTest() {
        MockitoAnnotations.initMocks(this);
        when(mockBindingResult.hasErrors()).thenReturn(false);
    }


    @Test
    public void testUpdatePortal() throws NoSuchFieldException, IllegalAccessException, IOException {

        PortalController testPortalController = new PortalController();
        setAccessPrivateFields(testPortalController);
        Portal testPortal = fillDomain();
        when(portalRepository.findById("Kibana")).thenReturn(Optional.of(testPortal));
        PostReturnBody<PortalConfig> postPortal = testPortalController.updatePortal(testPortal.getPortalConfig(), mockBindingResult, httpServletResponse);
        assertEquals(postPortal.getStatusCode(), 200);
        //when(mockBindingResult.hasErrors()).thenReturn(true);

    }


    @Test
    public void testGetPortals() throws NoSuchFieldException, IllegalAccessException {

        PortalController testPortalController = new PortalController();
        setAccessPrivateFields(testPortalController);
        Portal testPortal = fillDomain();
        List<Portal> portalList = new ArrayList<>();
        portalList.add(testPortal);
        when(portalRepository.findAll()).thenReturn(portalList);
        assertEquals(1, testPortalController.getPortals().size());

    }


    public void setAccessPrivateFields(PortalController portalController) throws NoSuchFieldException, IllegalAccessException {

        Field testPortalService = portalController.getClass().getDeclaredField("portalService");
        testPortalService.setAccessible(true);
        testPortalService.set(portalController, portalService);
        Field testPortalRepository = portalController.getClass().getDeclaredField("portalRepository");
        testPortalRepository.setAccessible(true);
        testPortalRepository.set(portalController, portalRepository);
    }


    public Portal fillDomain(){
        Portal portal = new Portal();
        portal.setName("Kibana");
        portal.setEnabled(true);
        portal.setHost("127.0.0.1");
        portal.setPort(5601);
        portal.setLogin("admin");
        portal.setPass("password");
        portal.setDb(new Db("Elasticsearch"));
        return  portal;
    }
}