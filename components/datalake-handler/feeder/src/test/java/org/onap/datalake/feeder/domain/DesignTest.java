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

package org.onap.datalake.feeder.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.onap.datalake.feeder.util.TestUtil;

public class DesignTest {

    @Test
    public void testIs() {

        Design design = new Design();
        design.setId(1);
        design.setSubmitted(false);
        design.setBody("jsonString");
        design.setName("templateTest");
        design.setTopicName(new TopicName("x"));
        Topic topic = TestUtil.newTopic("_DL_DEFAULT_");
        design.setTopicName(topic.getTopicName());
        DesignType designType = new DesignType();
        designType.setName("Kibana");
        design.setDesignType(designType);
        design.setNote("test");
        design.setDbs(null);
        assertFalse("1".equals(design.getId()));
        assertTrue("templateTest".equals(design.getName()));
        assertTrue("jsonString".equals(design.getBody()));
        assertFalse("_DL_DEFAULT_".equals(design.getTopicName()));
        assertTrue("test".equals(design.getNote()));
        assertFalse("Kibana".equals(design.getDesignType()));
        assertFalse("false".equals(design.getSubmitted()));
        assertNull(design.getDbs());
    }

}
