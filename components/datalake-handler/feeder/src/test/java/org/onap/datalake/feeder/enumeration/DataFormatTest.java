/*
* ============LICENSE_START=======================================================
* ONAP : DCAE
* ================================================================================
* Copyright 2018 TechMahindra
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
package org.onap.datalake.feeder.enumeration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito; 

/**
 * Test Data format of DMaaP messages
 * 
 * @author Guobiao Mo
 *
 */
public class DataFormatTest {
    @Test
    public void fromString() {
        assertEquals(DataFormat.JSON, DataFormat.fromString("json"));
        assertEquals(DataFormat.XML, DataFormat.fromString("xml"));
        assertEquals(DataFormat.YAML, DataFormat.fromString("YAML"));
        assertEquals(DataFormat.TEXT, DataFormat.fromString("Text"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromStringWithException() {
    	DataFormat.fromString("test");
    }
   
}
