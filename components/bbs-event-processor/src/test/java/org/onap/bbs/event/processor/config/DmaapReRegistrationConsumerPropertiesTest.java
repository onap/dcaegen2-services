/*
 * ============LICENSE_START=======================================================
 * BBS-RELOCATION-CPE-AUTHENTICATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.bbs.event.processor.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DmaapReRegistrationConsumerProperties.class)
@EnableConfigurationProperties
@TestPropertySource(properties = {
        "configs.dmaap.consumer.re-registration.dmaapHostName=test localhost",
        "configs.dmaap.consumer.re-registration.dmaapPortNumber=1234",
        "configs.dmaap.consumer.re-registration.dmaapTopicName=/events/unauthenticated.PNF_REREGISTRATION",
        "configs.dmaap.consumer.re-registration.dmaapProtocol=http",
        "configs.dmaap.consumer.re-registration.dmaapContentType=application/json",
        "configs.dmaap.consumer.re-registration.consumerId=c12",
        "configs.dmaap.consumer.re-registration.consumerGroup=OpenDcae-c12",
        "configs.dmaap.consumer.re-registration.timeoutMs=-1",
        "configs.dmaap.consumer.re-registration.messageLimit=1"})
class DmaapReRegistrationConsumerPropertiesTest {

    private DmaapReRegistrationConsumerProperties properties;

    @Autowired
    public DmaapReRegistrationConsumerPropertiesTest(
            DmaapReRegistrationConsumerProperties properties) {
        this.properties = properties;
    }

    @Test
    void dmaapReRegistrationProperties_SuccessFullyLoaded() {
        assertEquals("test localhost", properties.getDmaapHostName());
        assertEquals(1234, properties.getDmaapPortNumber());
        assertEquals("/events/unauthenticated.PNF_REREGISTRATION", properties.getDmaapTopicName());
        assertEquals("http", properties.getDmaapProtocol());
        assertNull(properties.getDmaapUserName());
        assertNull(properties.getDmaapUserPassword());
        assertEquals("application/json", properties.getDmaapContentType());
        assertEquals("c12", properties.getConsumerId());
        assertEquals("OpenDcae-c12", properties.getConsumerGroup());
        assertEquals(-1, properties.getTimeoutMs());
        assertEquals(1, properties.getMessageLimit());
    }
}