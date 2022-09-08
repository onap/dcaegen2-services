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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.domain.DesignType;
import org.onap.datalake.feeder.dto.DesignTypeConfig;
import org.onap.datalake.feeder.repository.DesignTypeRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DesignTypeServiceTest {

    @Mock
    private DesignTypeRepository designTypeRepository;

    @InjectMocks
    private DesignTypeService designTypeService;

    @Test
    public void testDesignTypeService(){
        List<DesignType> designTypeList = new ArrayList<>();
        DesignType designType = new DesignType();
        designType.setName("test");
        //DesignTypeConfig designTypeConfig = new DesignTypeConfig();
        //designTypeConfig.setDesignType("test");
        //designTypeConfig.setDisplay("test");
        designTypeList.add(designType);
        when(designTypeRepository.findAll()).thenReturn(designTypeList);
        assertNotNull(designTypeService.getDesignTypes());
    }

}