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

package org.onap.datalake.feeder.domain;

import org.junit.Test;

import static org.junit.Assert.*;

public class DesignTypeTest {

    @Test
    public void test(){
        DesignType designType = new DesignType();
        designType.setName("Kibana Dashboard");
        designType.setNote("test");
        assertEquals("Kibana Dashboard", designType.getName());
        assertEquals("test", designType.getNote());

        designType.setDbType(null);
        designType.getDbType();
        designType.setDesigns(null);
        designType.getDesigns();
        designType.getDesignTypeConfig();
    }
}