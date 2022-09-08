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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.domain.DesignType;
import org.onap.datalake.feeder.service.DesignTypeService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DesignTypeControllerTest {

    @InjectMocks
    private DesignTypeService designTypeService;

    @Before
    public void setupTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = NullPointerException.class)
    public void getTemplateTypeName() throws NoSuchFieldException, IllegalAccessException {

        DesignTypeController testDesignTypeController = new DesignTypeController();
        setAccessPrivateFields(testDesignTypeController);
        DesignType testDesignType = fillDomain();
        List<String> designTypeNamesList = new ArrayList<>();
        designTypeNamesList.add(testDesignType.getName());
        assertEquals(1, testDesignTypeController.getDesignType().size());
    }

    public void setAccessPrivateFields(DesignTypeController designTypeController) throws NoSuchFieldException, IllegalAccessException {

        Field testDesignTypeService = designTypeController.getClass().getDeclaredField("designTypeService");
        testDesignTypeService.setAccessible(true);
        testDesignTypeService.set(designTypeController, designTypeService);
    }


    public DesignType fillDomain(){
        DesignType designType = new DesignType();
        designType.setName("Kibana Dashboard");
        return designType;
    }
}