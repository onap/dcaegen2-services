/*
 * ============LICENSE_START=======================================================
 * ONAP : DCAE
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
package org.onap.datalake.feeder.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.Design;
import org.onap.datalake.feeder.domain.DesignType;
import org.onap.datalake.feeder.domain.TopicName;
import org.onap.datalake.feeder.dto.DesignConfig;
import org.onap.datalake.feeder.repository.DbRepository;
import org.onap.datalake.feeder.repository.DesignRepository;
import org.onap.datalake.feeder.repository.DesignTypeRepository;
import org.onap.datalake.feeder.repository.TopicNameRepository;
import org.onap.datalake.feeder.util.TestUtil;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class DesignServiceTest {

    @Mock
    private DesignType designType;

    @Mock
    private DesignRepository designRepository;

    @Mock
    private TopicNameRepository topicNameRepository;

    @Mock
    private DbRepository dbRepository;

    @Mock
    private ApplicationConfiguration applicationConfiguration;

    @Mock
    private DesignTypeRepository designTypeRepository;

    @InjectMocks
    private DesignService designService;

    @Test(expected = RuntimeException.class)
    public void testDeployException() {
        when(designType.getId()).thenReturn("KIBANA_DB", "ES_MAPPING");
        Design design = new Design();
        design.setDesignType(designType);
        design.setBody("jsonString");
        //when(applicationConfiguration.getKibanaDashboardImportApi()).thenReturn("/api/kibana/dashboards/import?exclude=index-pattern");
        //when(applicationConfiguration.getKibanaPort()).thenReturn(5601);
        designService.deploy(design);
        System.out.println();
    }

    @Test
    public void testFillDesignConfigurationNull() {
        DesignConfig designConfig = new DesignConfig();
        designConfig.setTopicName("topic");
        designConfig.setDesignType("designType");
        TopicName topicName = new TopicName("test");
        Optional < TopicName > topicNameOptional = Optional.of(topicName);
        when(topicNameRepository.findById(designConfig.getTopicName())).thenReturn(topicNameOptional);
        when(designTypeRepository.findById(designConfig.getDesignType())).thenReturn(Optional.of(new DesignType()));
        assertNull(designService.fillDesignConfiguration(designConfig).getName());
    }

    @Test
    public void testFillDesignConfiguration() {
        DesignConfig designConfig = new DesignConfig();
        designConfig.setTopicName("topic");
        designConfig.setDesignType("designType");

        List < Integer > dbs = new ArrayList < > ();
        dbs.add(1);
        designConfig.setDbs(dbs);
        when(topicNameRepository.findById(designConfig.getTopicName())).thenReturn(Optional.of(new TopicName()));
        when(designTypeRepository.findById(designConfig.getDesignType())).thenReturn(Optional.of(new DesignType()));
        when(dbRepository.findById(designConfig.getDbs().get(0))).thenReturn(Optional.of(new Db()));
        designService.fillDesignConfiguration(designConfig).getName();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFillDesign() {
        Design design = new Design();
        design.setDesignType(designType);
        DesignConfig designConfig = new DesignConfig();
        designService.fillDesignConfiguration(designConfig, design);
    }

    @Test
    public void testGetDesignNull() {
        Optional < Design > testDesign = Optional.ofNullable(null);
        when(designRepository.findById(1)).thenReturn(testDesign);
        assertNull(designService.getDesign(1));
    }

    @Test
    public void testDeploy() {
        when(designType.getId()).thenReturn("KIBANA_DB");
        Design design = getDesign();
        assertNotNull(designService.deploy(design));
    }

    @Test
    public void testDeployESMappingCase() {
        when(designType.getId()).thenReturn("ES_MAPPING");
        Design design = getDesign();
        assertNotNull(designService.deploy(design));
    }

    @Test
    public void testDeployDefault() {
        when(designType.getId()).thenReturn("KIBANA_SEARCH");
        Design design = getDesign();
        assertNull(designService.deploy(design));
    }

    @Test(expected = NullPointerException.class)
    public void testQueryAllDesignNull() {
        when(designRepository.findAll()).thenReturn(null);
        designService.queryAllDesign();
    }

    @Test
    public void testQueryAllDesign() {
        List < Design > designList = new ArrayList < > ();
        Design design = getDesign();
        designList.add(design);
        when(designRepository.findAll()).thenReturn(designList);
        designService.queryAllDesign();
    }

    public Design getDesign() {
        Design design = new Design();
        design.setDesignType(designType);
        design.setBody("jsonString");
        design.setTopicName(new TopicName("1"));
        Set < Db > dbs = new HashSet < > ();
        Db db = TestUtil.newDb("MongoDB");
        db.setEnabled(true);
        dbs.add(db);
        design.setDbs(dbs);
        return design;
    }

}
