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
import org.onap.datalake.feeder.domain.DesignType;
import org.onap.datalake.feeder.dto.DesignTypeConfig;
import org.onap.datalake.feeder.repository.DesignTypeRepository;
import org.onap.datalake.feeder.service.DesignTypeService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DesignTypeControllerTest {

    @Mock
    private DesignTypeRepository designTypeRepository;

    @Mock
    private DesignTypeService designTypeService;

    @InjectMocks
    private DesignTypeController designTypeController;

    @Before
    public void setupTest() {
        MockitoAnnotations.initMocks(this);
    }

    public DesignType fillDomain() {
        DesignType designType = new DesignType();
        designType.setName("Kibana Dashboard");
        return designType;
    }

    @Test
    public void testGetDesignType() {
        List < DesignTypeConfig > designTypeNamesList = new ArrayList < > ();
        List < DesignType > designTypeList = new ArrayList < > ();
        DesignType designType = fillDomain();
        designTypeList.add(designType);
        when(designTypeService.getDesignTypes()).thenReturn(designTypeNamesList);
        assertNotNull(designTypeController.getDesignType());
    }

}
