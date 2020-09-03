/*
* ============LICENSE_START=======================================================
* ONAP : DATALAKE
* ================================================================================
* Copyright 2020 China Mobile
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

package org.onap.datalake.des;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.onap.datalake.des.DesSwaggerConfig;

/**
 * Test Swagger integration.
 * 
 * @author Kai Lu
 *
 */
 
public class DesSwaggerConfigTest {

    @Test
    public void test() {
        try {
            DesSwaggerConfig config = new DesSwaggerConfig();
            config.desProduceApi();
        } catch (Exception e) {
            fail("failed to read configure Des Swagger.");
        }
    }

}