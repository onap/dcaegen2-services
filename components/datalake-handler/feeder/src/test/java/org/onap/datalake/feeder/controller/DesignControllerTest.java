/*
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE
 * ================================================================================
 * Copyright 2019 China Mobile
 * Copyright (C) 2022 Wipro Limited.
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
import org.onap.datalake.feeder.domain.Design;
import org.onap.datalake.feeder.domain.DesignType;
import org.onap.datalake.feeder.domain.TopicName;
import org.onap.datalake.feeder.dto.DesignConfig;
import org.onap.datalake.feeder.repository.DesignTypeRepository;
import org.onap.datalake.feeder.repository.TopicNameRepository;
import org.onap.datalake.feeder.repository.DesignRepository;
import org.onap.datalake.feeder.service.DesignService;
import org.onap.datalake.feeder.service.TopicService;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DesignControllerTest {
  
    //static String Kibana_Dashboard_Import_Api = "/api/kibana/dashboards/import?exclude=index-pattern";

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private BindingResult mockBindingResult;

    @Mock
    private ApplicationConfiguration applicationConfiguration;

    @Mock
    private DesignRepository designRepository;

    @Mock
    private TopicService topicService;

    @Mock
    private DesignTypeRepository designTypeRepository;
    
    @Mock
    private TopicNameRepository topicNameRepository;

    @Mock
    private DesignService designService;
    
    @InjectMocks
    private DesignController testDesignController;

    @Before
    public void setupTest() {
        MockitoAnnotations.initMocks(this);
        when(mockBindingResult.hasErrors()).thenReturn(false);
    }

    @Test
    public void testCreateDesign() throws NoSuchFieldException, IllegalAccessException, IOException {

        Design testDesign = fillDomain();
        DesignConfig designConfig = new DesignConfig();
        when(designService.fillDesignConfiguration(designConfig)).thenReturn(testDesign);
        testDesignController.createDesign(designConfig, mockBindingResult, httpServletResponse);
    }
    
    @Test
    public void testCreateDesignNull() throws NoSuchFieldException, IllegalAccessException, IOException {

        DesignConfig designConfig = new DesignConfig();
        when(designService.fillDesignConfiguration(designConfig)).thenThrow(NullPointerException.class);
        testDesignController.createDesign(designConfig, mockBindingResult, httpServletResponse);
    }
    
    @Test
    public void testCreateDesignError() throws NoSuchFieldException, IllegalAccessException, IOException {
    	
        DesignConfig designConfig = new DesignConfig();
    	when(mockBindingResult.hasErrors()).thenReturn(true);
		assertEquals(null, testDesignController.createDesign(designConfig, mockBindingResult, httpServletResponse));
    }

    @Test
    public void testUpdateDesignNull() throws NoSuchFieldException, IllegalAccessException, IOException {

        Design testDesign = fillDomain();
        Integer id = 1;
        //when(topicService.getTopic(0)).thenReturn(new Topic("unauthenticated.SEC_FAULT_OUTPUT"));
 //       when(designTypeRepository.findById("Kibana Dashboard")).thenReturn(Optional.of(testDesign.getDesignType()));
        PostReturnBody<DesignConfig> postPortal = testDesignController.updateDesign(testDesign.getDesignConfig(), mockBindingResult, id, httpServletResponse);
        //assertEquals(postPortal.getStatusCode(), 200);
        assertNull(postPortal);
    }
    
    @Test
    public void testUpdateDesignError() throws NoSuchFieldException, IllegalAccessException, IOException {
        Design testDesign = fillDomain();
        Integer id = 1;
    	when(mockBindingResult.hasErrors()).thenReturn(true);
		assertEquals(null, testDesignController.updateDesign(testDesign.getDesignConfig(), mockBindingResult, id, httpServletResponse));
    }
    
    @Test
    public void testUpdateDesign() throws NoSuchFieldException, IllegalAccessException, IOException {
        Design testDesign = fillDomain();
        Integer id = 1;
        when(designService.getDesign(id)).thenReturn(testDesign);
		testDesignController.updateDesign(testDesign.getDesignConfig(), mockBindingResult, id, httpServletResponse);
    }
    
    @Test
    public void testUpdateDesignException() throws NoSuchFieldException, IllegalAccessException, IOException {
        Design testDesign = fillDomain();
        DesignConfig designConfig = new DesignConfig();
        Integer id = 1;
        when(designService.getDesign(id)).thenReturn(testDesign);
        doThrow(NullPointerException.class).when(designService).fillDesignConfiguration(designConfig, testDesign);
		testDesignController.updateDesign(designConfig, mockBindingResult, id, httpServletResponse);
    }

    @Test
    public void testDeleteDesignNull() throws NoSuchFieldException, IllegalAccessException, IOException {

        Design testDesign = fillDomain();
        Integer id = 1;
        testDesign.setId(1);
        testDesignController.deleteDesign(id, httpServletResponse);
    }
    
    @Test
    public void testDeleteDesign() throws NoSuchFieldException, IllegalAccessException, IOException {

        Design testDesign = fillDomain();
        Integer id = 1;
        testDesign.setId(1);
        when(designService.getDesign(id)).thenReturn(testDesign);
        testDesignController.deleteDesign(id, httpServletResponse);
    }

    @Test
    public void testQueryAllDesign() throws NoSuchFieldException, IllegalAccessException {

        DesignController testDesignController = new DesignController();
        setAccessPrivateFields(testDesignController);
        Design testDesign = fillDomain();
        List<Design> designList = new ArrayList<>();
        designList.add(testDesign);
        assertEquals(0, testDesignController.queryAllDesign().size());
    }

    @Test
    public void testDeployDesignNull() throws NoSuchFieldException, IllegalAccessException, IOException {

        Design testDesign = fillDomain();
        Integer id = 1;
        testDesign.setId(1);
        testDesignController.deployDesign(id, httpServletResponse);
    }
    
    @Test
    public void testDeployDesign() throws NoSuchFieldException, IllegalAccessException, IOException {

        Design testDesign = fillDomain();
        Integer id = 1;
        testDesign.setId(1);
        when(designRepository.findById(id)).thenReturn(Optional.of(new Design()));
        testDesignController.deployDesign(id, httpServletResponse);
    }

    public void setAccessPrivateFields(DesignController designController) throws NoSuchFieldException, IllegalAccessException {

        Field testPortalDesignService = designController.getClass().getDeclaredField("designService");
        testPortalDesignService.setAccessible(true);
        testPortalDesignService.set(designController, designService);
        Field testPortalDesignRepository = designController.getClass().getDeclaredField("designRepository");
        testPortalDesignRepository.setAccessible(true);
        testPortalDesignRepository.set(designController, designRepository);
    }


    public Design fillDomain(){
        Design design = new Design();
        design.setId(1);
        design.setName("Kibana");
        design.setBody("jsonString");
        design.setSubmitted(false);
        design.setNote("test");
        DesignType designType = new DesignType();
        designType.setId("1");
        designType.setName("Kibana Dashboard");
        design.setDesignType(designType);
        design.setTopicName(new TopicName("unauthenticated.SEC_FAULT_OUTPUT"));
        return design;
    }
}
