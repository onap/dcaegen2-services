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
import org.onap.datalake.feeder.domain.DesignType;
import org.onap.datalake.feeder.domain.PortalDesign;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.domain.TopicName;

import static org.junit.Assert.*;

public class PortalDesignConfigTest {

    @Test
    public void testIs() {

        PortalDesign testPortaldesign = new PortalDesign();
        testPortaldesign.setId(1);
        testPortaldesign.setTopicName(new TopicName("test"));
        DesignType testDesignType = new DesignType();
        testDesignType.setName("test");
        testPortaldesign.setDesignType(testDesignType);

        PortalDesign testPortaldesign2 = new PortalDesign();
        testPortaldesign2.setId(1);
        testPortaldesign2.setTopicName(new TopicName("test"));
        DesignType testDesignType2 = new DesignType();
        testDesignType2.setName("test");
        testPortaldesign2.setDesignType(testDesignType2);

        PortalDesignConfig testPortalDesignConfig = testPortaldesign.getPortalDesignConfig();

        assertNotEquals(testPortalDesignConfig, testPortaldesign2.getPortalDesignConfig());
        assertNotEquals(testPortalDesignConfig, null);
        assertNotEquals(testPortalDesignConfig.getId(), null);
        assertEquals(testPortalDesignConfig.getBody(), null);
        assertEquals(testPortalDesignConfig.getNote(), null);
        assertEquals(testPortalDesignConfig.getName(), null);
        assertEquals(testPortalDesignConfig.getSubmitted(), null);
    }

}