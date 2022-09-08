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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = AaiClientProperties.class)
@EnableConfigurationProperties
@TestPropertySource(properties = {
        "configs.aai.client.aaiHost=test localhost",
        "configs.aai.client.aaiPort=1234",
        "configs.aai.client.aaiProtocol=https",
        "configs.aai.client.aaiUserName=AAI",
        "configs.aai.client.aaiUserPassword=AAI",
        "configs.aai.client.aaiIgnoreSslCertificateErrors=true",
        "configs.aai.client.aaiHeaders.X-FromAppId=bbs",
        "configs.aai.client.aaiHeaders.X-TransactionId=9999",
        "configs.aai.client.aaiHeaders.Accept=application/json",
        "configs.aai.client.aaiHeaders.Real-Time=true",
        "configs.aai.client.aaiHeaders.Content-Type=application/merge-patch+json"})
class AaiClientPropertiesTest {

    private AaiClientProperties properties;

    @Autowired
    public AaiClientPropertiesTest(
            AaiClientProperties properties) {
        this.properties = properties;
    }

    @Test
    void dmaapReRegistrationProperties_SuccessFullyLoaded() {
        assertEquals("test localhost", properties.getAaiHost());
        assertEquals(1234, properties.getAaiPort());
        assertEquals("https", properties.getAaiProtocol());
        assertEquals("AAI", properties.getAaiUserName());
        assertEquals("AAI", properties.getAaiUserPassword());
        assertTrue(properties.isAaiIgnoreSslCertificateErrors());
        assertEquals("bbs", properties.getAaiHeaders().get("X-FromAppId"));
        assertEquals("9999", properties.getAaiHeaders().get("X-TransactionId"));
        assertEquals("application/json", properties.getAaiHeaders().get("Accept"));
        assertEquals("true", properties.getAaiHeaders().get("Real-Time"));
        assertEquals("application/merge-patch+json", properties.getAaiHeaders().get("Content-Type"));
    }
}