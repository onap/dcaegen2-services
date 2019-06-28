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
import org.onap.datalake.feeder.util.TestUtil;

import static org.junit.Assert.*;

public class PortalDesignTest {

    @Test
    public void testIs() {

        PortalDesign portalDesign = new PortalDesign();
        portalDesign.setId(1);
        portalDesign.setSubmitted(false);
        portalDesign.setBody("jsonString");
        portalDesign.setName("templateTest");
        portalDesign.setTopicName(new TopicName("x"));
        Topic topic = TestUtil.newTopic("_DL_DEFAULT_");
        portalDesign.setTopicName(topic.getTopicName());
        DesignType designType = new DesignType();
        designType.setName("Kibana");
        portalDesign.setDesignType(designType);
        portalDesign.setNote("test");
        assertFalse("1".equals(portalDesign.getId()));
        assertTrue("templateTest".equals(portalDesign.getName()));
        assertTrue("jsonString".equals(portalDesign.getBody()));
        assertFalse("_DL_DEFAULT_".equals(portalDesign.getTopicName()));
        assertTrue("test".equals(portalDesign.getNote()));
        assertFalse("Kibana".equals(portalDesign.getDesignType()));
        assertFalse("false".equals(portalDesign.getSubmitted()));
    }

}