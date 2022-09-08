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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("configs.dmaap.consumer.cpe-authentication")
@Getter
@Setter
@Validated
public class DmaapCpeAuthenticationConsumerProperties {

    @NotBlank
    private String dmaapHostName;

    @Min(1025)
    @Max(65536)
    private int dmaapPortNumber;

    @NotBlank
    private String dmaapTopicName;

    @NotBlank
    @Pattern(regexp = "(http|https)")
    private String dmaapProtocol;

    private String dmaapUserName;

    private String dmaapUserPassword;

    @NotBlank
    private String dmaapContentType;

    @NotBlank
    private String consumerId;

    @NotBlank
    private String consumerGroup;

    private int timeoutMs;
    private int messageLimit;
}
