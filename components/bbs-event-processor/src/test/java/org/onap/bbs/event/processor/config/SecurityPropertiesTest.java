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

@SpringBootTest(classes = SecurityProperties.class)
@EnableConfigurationProperties
@TestPropertySource(properties = {
        "configs.security.trustStorePath=test trust store path",
        "configs.security.trustStorePasswordPath=test trust store password path",
        "configs.security.keyStorePath=test key store path",
        "configs.security.keyStorePasswordPath=test key store password path",
        "configs.security.enableDmaapCertAuth=true",
        "configs.security.enableAaiCertAuth=true"})
class SecurityPropertiesTest {

    private SecurityProperties securityProperties;

    @Autowired
    public SecurityPropertiesTest(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Test
    void securityProperties_SuccessFullyLoaded() {
        assertEquals("test key store path",
                securityProperties.getKeyStorePath());
        assertEquals("test key store password path",
                securityProperties.getKeyStorePasswordPath());
        assertEquals("test trust store path",
                securityProperties.getTrustStorePath());
        assertEquals("test trust store password path",
                securityProperties.getTrustStorePasswordPath());
        assertTrue(securityProperties.isEnableDmaapCertAuth());
        assertTrue(securityProperties.isEnableAaiCertAuth());
    }
}