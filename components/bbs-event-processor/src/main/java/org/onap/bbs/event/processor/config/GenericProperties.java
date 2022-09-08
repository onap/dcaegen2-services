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

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("configs.application")
@Getter
@Setter
@Validated
public class GenericProperties {

    private int pipelinesPollingIntervalSec;

    private int pipelinesTimeoutSec;

    @NotBlank
    private String policyVersion;

    @NotBlank
    private String clTargetType;

    @NotBlank
    private String clEventStatus;

    @NotBlank
    private String clVersion;

    @NotBlank
    private String clTarget;

    @NotBlank
    private String clOriginator;

    private ReRegistrationGenericProperties reRegistration;

    private CpeAuthenticationGenericProperties cpeAuthentication;

    @Getter
    @Setter
    @Validated
    static class ReRegistrationGenericProperties {

        @NotBlank
        private String policyScope;

        @NotBlank
        private String clControlName;
    }

    @Getter
    @Setter
    @Validated
    static class CpeAuthenticationGenericProperties {
        @NotBlank
        private String policyScope;

        @NotBlank
        private String clControlName;
    }
}
