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

package org.onap.bbs.event.processor.tasks;

import org.onap.bbs.event.processor.exceptions.AaiTaskException;
import org.onap.bbs.event.processor.model.PnfAaiObject;
import org.onap.bbs.event.processor.model.ServiceInstanceAaiObject;
import org.onap.bbs.event.processor.utilities.AaiReactiveClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import reactor.core.publisher.Mono;

@Component
public class AaiClientTaskImpl implements AaiClientTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AaiClientTaskImpl.class);
    private final AaiReactiveClient reactiveClient;

    @Autowired
    AaiClientTaskImpl(AaiReactiveClient reactiveClient) {
        this.reactiveClient = reactiveClient;
    }

    @Override
    public Mono<PnfAaiObject> executePnfRetrieval(String taskName, String url) {
        if (StringUtils.isEmpty(url)) {
            throw new AaiTaskException("Cannot invoke an A&AI client task with an invalid URL");
        }
        LOGGER.info("Executing task ({}) for retrieving PNF object", taskName);
        return reactiveClient.getPnfObjectDataFor(url);
    }

    @Override
    public Mono<ServiceInstanceAaiObject> executeServiceInstanceRetrieval(String taskName, String url) {
        if (StringUtils.isEmpty(url)) {
            throw new AaiTaskException("Cannot invoke an A&AI client task with an invalid URL");
        }
        LOGGER.info("Executing task ({}) for retrieving Service Instance object", taskName);
        return reactiveClient.getServiceInstanceObjectDataFor(url);
    }
}
