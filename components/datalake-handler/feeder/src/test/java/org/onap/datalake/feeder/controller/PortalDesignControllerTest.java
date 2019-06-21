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
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.controller.domain.PostReturnBody;
import org.onap.datalake.feeder.domain.DesignType;
import org.onap.datalake.feeder.domain.Portal;
import org.onap.datalake.feeder.domain.PortalDesign;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.dto.PortalDesignConfig;
import org.onap.datalake.feeder.repository.DesignTypeRepository;
import org.onap.datalake.feeder.repository.PortalDesignRepository;
import org.onap.datalake.feeder.service.PortalDesignService;
import org.onap.datalake.feeder.service.TopicService;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PortalDesignControllerTest {

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private BindingResult mockBindingResult;

    @Mock
    private ApplicationConfiguration applicationConfiguration;

    @Mock
    private PortalDesignRepository portalDesignRepository;

    @Mock
    private TopicService topicService;

    @Mock
    private DesignTypeRepository designTypeRepository;

    @InjectMocks
    private PortalDesignService portalDesignService;


    @Before
    public void setupTest() {
        MockitoAnnotations.initMocks(this);
        when(mockBindingResult.hasErrors()).thenReturn(false);
    }

    @Test
    public void testCreatePortalDesign() throws NoSuchFieldException, IllegalAccessException, IOException {

        PortalDesignController testPortalDesignController = new PortalDesignController();
        setAccessPrivateFields(testPortalDesignController);
        PortalDesign testPortalDesign = fillDomain();
        when(topicService.getTopic("unauthenticated.SEC_FAULT_OUTPUT")).thenReturn(new Topic("unauthenticated.SEC_FAULT_OUTPUT"));
        when(designTypeRepository.findById("Kibana Dashboard")).thenReturn(Optional.of(testPortalDesign.getDesignType()));
        PostReturnBody<PortalDesignConfig> postPortal = testPortalDesignController.createPortalDesign(testPortalDesign.getPortalDesignConfig(), mockBindingResult, httpServletResponse);
        assertEquals(postPortal.getStatusCode(), 200);
    }

    @Test
    public void testUpdatePortalDesign() throws NoSuchFieldException, IllegalAccessException, IOException {

        PortalDesignController testPortalDesignController = new PortalDesignController();
        setAccessPrivateFields(testPortalDesignController);
        PortalDesign testPortalDesign = fillDomain();
        Integer id = 1;
        when(portalDesignRepository.findById(id)).thenReturn((Optional.of(testPortalDesign)));
        when(topicService.getTopic("unauthenticated.SEC_FAULT_OUTPUT")).thenReturn(new Topic("unauthenticated.SEC_FAULT_OUTPUT"));
        when(designTypeRepository.findById("Kibana Dashboard")).thenReturn(Optional.of(testPortalDesign.getDesignType()));
        PostReturnBody<PortalDesignConfig> postPortal = testPortalDesignController.updatePortalDesign(testPortalDesign.getPortalDesignConfig(), mockBindingResult, id, httpServletResponse);
        assertEquals(postPortal.getStatusCode(), 200);
    }

    @Test
    public void testDeletePortalDesign() throws NoSuchFieldException, IllegalAccessException, IOException {

        PortalDesignController testPortalDesignController = new PortalDesignController();
        setAccessPrivateFields(testPortalDesignController);
        PortalDesign testPortalDesign = fillDomain();
        Integer id = 1;
        testPortalDesign.setId(1);
        when(portalDesignRepository.findById(id)).thenReturn((Optional.of(testPortalDesign)));
        testPortalDesignController.deletePortalDesign(id, httpServletResponse);
    }

    @Test
    public void testQueryAllPortalDesign() throws NoSuchFieldException, IllegalAccessException {

        PortalDesignController testPortalDesignController = new PortalDesignController();
        setAccessPrivateFields(testPortalDesignController);
        PortalDesign testPortalDesign = fillDomain();
        List<PortalDesign> portalDesignList = new ArrayList<>();
        portalDesignList.add(testPortalDesign);
        when(portalDesignRepository.findAll()).thenReturn(portalDesignList);
        assertEquals(1, testPortalDesignController.queryAllPortalDesign().size());
    }

    /*@Test
    public void testDeployPortalDesign() throws NoSuchFieldException, IllegalAccessException, IOException {

        PortalDesignController testPortalDesignController = new PortalDesignController();
        setAccessPrivateFields(testPortalDesignController);
        PortalDesign testPortalDesign = fillDomain();
        Integer id = 1;
        testPortalDesign.setId(1);
        when(portalDesignRepository.findById(id)).thenReturn((Optional.of(testPortalDesign)));
        when(applicationConfiguration.getKibanaDashboardImportApi()).thenReturn("/api/kibana/dashboards/import?exclude=index-pattern");
        testPortalDesignController.deployPortalDesign(id, httpServletResponse);
    }*/

    public void setAccessPrivateFields(PortalDesignController portalDesignController) throws NoSuchFieldException, IllegalAccessException {

        Field testPortalDesignService = portalDesignController.getClass().getDeclaredField("portalDesignService");
        testPortalDesignService.setAccessible(true);
        testPortalDesignService.set(portalDesignController, portalDesignService);
        Field testPortalDesignRepository = portalDesignController.getClass().getDeclaredField("portalDesignRepository");
        testPortalDesignRepository.setAccessible(true);
        testPortalDesignRepository.set(portalDesignController, portalDesignRepository);
    }


    public PortalDesign fillDomain(){
        PortalDesign portalDesign = new PortalDesign();
        portalDesign.setName("Kibana");
        portalDesign.setBody("jsonString");
        portalDesign.setSubmitted(false);
        portalDesign.setNote("test");
        DesignType designType = new DesignType();
        designType.setName("Kibana Dashboard");
        Portal portal = new Portal();
        portal.setName("Kibana");
        portal.setHost("127.0.0.1");
        portal.setPort(5601);
        designType.setPortal(portal);
        portalDesign.setDesignType(designType);
        portalDesign.setTopic(new Topic("unauthenticated.SEC_FAULT_OUTPUT"));
        return  portalDesign;
    }
}