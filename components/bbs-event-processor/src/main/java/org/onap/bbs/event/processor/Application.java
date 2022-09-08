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

package org.onap.bbs.event.processor;

import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.INVOCATION_ID;
import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.REQUEST_ID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
@Configuration
public class Application {

    private Logger logger = LoggerFactory.getLogger(Application.class);

    private static final int THREADS_IN_POOL = 5;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    Map<String, String> mdcContextMap() {
        MDC.put(REQUEST_ID, "SampleRequestID");
        MDC.put(INVOCATION_ID, UUID.randomUUID().toString());
        return MDC.getCopyOfContextMap();
    }

    @Bean
    Gson gson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        return gsonBuilder.create();
    }

    @Bean
    TaskScheduler threadPoolTaskScheduler() {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(THREADS_IN_POOL);
        scheduler.setThreadNamePrefix("pipeline-thrd-");
        return scheduler;
    }

    @PostConstruct
    public void postConstructionSetup() {
        logger.info("bbs-event-processor application started");
    }
}
