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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {GenericProperties.class,
        GenericProperties.ReRegistrationGenericProperties.class,
        GenericProperties.CpeAuthenticationGenericProperties.class})
@EnableConfigurationProperties
@TestPropertySource(properties = {
        "configs.application.pipelinesPollingIntervalSec=30",
        "configs.application.pipelinesTimeoutSec=15",
        "configs.application.policyVersion=1.0.0",
        "configs.application.clTargetType=VM",
        "configs.application.clEventStatus=ONSET",
        "configs.application.clVersion=1.0.2",
        "configs.application.clTarget=vserver.vserver-name",
        "configs.application.clOriginator=DCAE-bbs-event-processor",
        "configs.application.re-registration.policyScope=reRegPolicyScope",
        "configs.application.re-registration.clControlName=reRegControlName",
        "configs.application.cpe-authentication.policyScope=cpeAuthPolicyScope",
        "configs.application.cpe-authentication.clControlName=cpeAuthControlName"})
class GenericPropertiesTest {

    private GenericProperties genericProperties;

    @Autowired
    public GenericPropertiesTest(GenericProperties genericProperties) {
        this.genericProperties = genericProperties;
    }

    @Test
    void genericProperties_SuccessFullyLoaded() {
        assertAll("Generic Application Properties",
            () -> assertEquals(30, genericProperties.getPipelinesPollingIntervalSec()),
            () -> assertEquals(15, genericProperties.getPipelinesTimeoutSec()),
            () -> assertEquals("1.0.0", genericProperties.getPolicyVersion()),
            () -> assertEquals("VM", genericProperties.getClTargetType()),
            () -> assertEquals("ONSET", genericProperties.getClEventStatus()),
            () -> assertEquals("1.0.2", genericProperties.getClVersion()),
            () -> assertEquals("vserver.vserver-name", genericProperties.getClTarget()),
            () -> assertEquals("DCAE-bbs-event-processor", genericProperties.getClOriginator()),
            () -> assertEquals("reRegPolicyScope", genericProperties.getReRegistration().getPolicyScope()),
            () -> assertEquals("cpeAuthPolicyScope",
                    genericProperties.getCpeAuthentication().getPolicyScope()),
            () -> assertEquals("reRegControlName", genericProperties.getReRegistration().getClControlName()),
            () -> assertEquals("cpeAuthControlName",
                    genericProperties.getCpeAuthentication().getClControlName())
        );
    }
}