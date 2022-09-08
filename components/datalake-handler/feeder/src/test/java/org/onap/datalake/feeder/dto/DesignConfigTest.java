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

package org.onap.datalake.feeder.dto;

import org.junit.Test;
import org.onap.datalake.feeder.domain.Design;
import org.onap.datalake.feeder.domain.DesignType;
import org.onap.datalake.feeder.domain.TopicName;

import static org.junit.Assert.*;

public class DesignConfigTest {

    @Test
    public void testIs() {

        Design testPortaldesign = new Design();
        testPortaldesign.setId(1);
        testPortaldesign.setTopicName(new TopicName("test"));
        DesignType testDesignType = new DesignType();
        testDesignType.setName("test");
        testPortaldesign.setDesignType(testDesignType);

        Design testPortaldesign2 = new Design();
        testPortaldesign2.setId(1);
        testPortaldesign2.setTopicName(new TopicName("test"));
        DesignType testDesignType2 = new DesignType();
        testDesignType2.setName("test");
        testPortaldesign2.setDesignType(testDesignType2);

        DesignConfig testDesignConfig = testPortaldesign.getDesignConfig();

        assertNotEquals(testDesignConfig, testPortaldesign2.getDesignConfig());
        assertNotEquals(testDesignConfig, null);
        assertNotEquals(testDesignConfig.getId(), null);
        assertEquals(testDesignConfig.getBody(), null);
        assertEquals(testDesignConfig.getNote(), null);
        assertEquals(testDesignConfig.getName(), null);
        assertEquals(testDesignConfig.getSubmitted(), null);
        assertEquals(testDesignConfig.getDesignType(), null);
    }

}