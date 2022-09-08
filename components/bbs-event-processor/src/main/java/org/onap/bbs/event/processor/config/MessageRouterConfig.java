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

import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DmaapClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageRouterConfig {

    @Bean(name = "ReRegMessageRouterSubscriber")
    public MessageRouterSubscriber reRegistrationMessageRouterSubscriber(ApplicationConfiguration configuration) {
        return DmaapClientFactory
                .createMessageRouterSubscriber(configuration.getDmaapReRegistrationConsumerConfiguration());
    }

    @Bean(name = "CpeAuthMessageRouterSubscriber")
    public MessageRouterSubscriber registrationMessageRouterSubscriber(ApplicationConfiguration configuration) {
        return DmaapClientFactory
                .createMessageRouterSubscriber(configuration.getDmaapCpeAuthenticationConsumerConfiguration());
    }

    @Bean
    public MessageRouterPublisher mrPub(ApplicationConfiguration configuration) {
        return DmaapClientFactory.createMessageRouterPublisher(configuration.getDmaapPublisherConfiguration());
    }
}
