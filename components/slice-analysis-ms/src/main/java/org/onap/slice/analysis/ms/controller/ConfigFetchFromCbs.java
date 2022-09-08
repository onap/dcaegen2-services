/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020-2021 Wipro Limited.
 *   ==============================================================================
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     ============LICENSE_END=========================================================
 *
 *******************************************************************************/

package org.onap.slice.analysis.ms.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Map;

import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsRequests;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.ImmutableRequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.onap.slice.analysis.ms.models.ConfigPolicy;
import org.onap.slice.analysis.ms.models.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.Disposable;

/**
 * This class provides method to fetch application Configuration
 * from CBS
 */
public class ConfigFetchFromCbs implements Runnable {

    private static Logger log = LoggerFactory.getLogger(ConfigFetchFromCbs.class);

    private Duration interval;

    public ConfigFetchFromCbs() {

    }

    public ConfigFetchFromCbs(Duration interval) {
        this.interval = interval;
    }

    /**
     * Gets app config from CBS.
     */
    private Disposable getAppConfig() {

        // Generate RequestID and InvocationID which will be used when logging and in
        // HTTP requests
        log.info("getAppconfig start ..");
        RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();
        // Read necessary properties from the environment
        final CbsClientConfiguration cbsClientConfiguration = CbsClientConfiguration.fromEnvironment();

        log.debug("environments {}", cbsClientConfiguration);
        ConfigPolicy configPolicy = ConfigPolicy.getInstance();

        // Polling properties
        final Duration initialDelay = Duration.ofSeconds(5);
        final Duration period = interval;

        // Create the client and use it to get the configuration
        final CbsRequest request = CbsRequests.getAll(diagnosticContext);
        return CbsClientFactory.createCbsClient(cbsClientConfiguration)
                .flatMapMany(cbsClient -> cbsClient.updates(request, initialDelay, period)).subscribe(jsonObject -> {
                    log.info("configuration and policy from CBS {}", jsonObject);
                    JsonObject config = jsonObject.getAsJsonObject("config");
                    Duration newPeriod = Duration.ofSeconds(config.get("cbsPollingInterval").getAsInt());
                    if (!newPeriod.equals(period)) {
                        interval = newPeriod;
                        synchronized (this) {
                            this.notifyAll();
                        }

                    }
                    Configuration.getInstance().updateConfigurationFromJsonObject(config);

                    Type mapType = new TypeToken<Map<String, Object>>() {
                    }.getType();
                    if (jsonObject.getAsJsonObject("policies") != null) {
                        JsonObject policyJson = jsonObject.getAsJsonObject("policies").getAsJsonArray("items").get(0)
                                .getAsJsonObject().getAsJsonObject("config");
                        Map<String, Object> policy = new Gson().fromJson(policyJson, mapType);
                        configPolicy.setConfig(policy);
                        log.info("Config policy {}", configPolicy);
                    }
                }, throwable -> log.warn("Ooops", throwable));
    }



    @Override
    public void run() {
        Boolean done = false;
        while (!done) {
            try {
                Disposable disp = getAppConfig();
                synchronized (this) {
                    this.wait();
                }
                log.info("Polling interval changed");
                disp.dispose();
            } catch (Exception e) {
                done = true;
            }
        }
    }

}
