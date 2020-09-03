/*-
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE
 * ================================================================================
 * Copyright (C) 2020 China Mobile. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.datalake.des.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.des.repository.DataExposureRepository;
import org.onap.datalake.des.service.DataExposureService;
import org.springframework.validation.BindingResult;


/**
 * Test Data Exposure Controller.
 *
 * @author Kai Lu
 */
@RunWith(MockitoJUnitRunner.class)
public class DataExposureControllerTest {

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private DataExposureRepository dataExposureRepository;

    @Mock
    private BindingResult mockBindingResult;

    @InjectMocks
    private DataExposureService dataExposureService;

    @Test(expected = NullPointerException.class)
    public void testServe() throws IOException, NoSuchFieldException,
                                   IllegalAccessException, ClassNotFoundException, SQLException {
        DataExposureController dataExposureController = new DataExposureController();
        String serviceId = "test";
        Map<String, String> requestMap = new HashMap<String,String>();
        requestMap.put("name", "oteNB5309");
        HashMap<String, Object> result = dataExposureController
            .serve(serviceId, requestMap, mockBindingResult, httpServletResponse);
        assertEquals(null, result);
        when(mockBindingResult.hasErrors()).thenReturn(true);
    }
}
