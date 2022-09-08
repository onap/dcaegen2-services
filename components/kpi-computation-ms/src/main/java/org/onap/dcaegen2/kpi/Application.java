/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021 China Mobile.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.kpi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;

import org.onap.dcaegen2.kpi.controller.ConfigFetchFromCbs;
import org.onap.dcaegen2.kpi.models.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Entry point for the kpi computation service application.
 *
 * @author Kai Lu
 *
 */
@EnableScheduling
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class })
public class Application {

    private static Logger log = LoggerFactory.getLogger(Application.class);

    /**
     * Main method where initial configuration and context is set.
     * 
     * @param args args
     */
    public static void main(String[] args) {
        Boolean standalone = Boolean.parseBoolean(System.getenv("STANDALONE"));

        if (standalone) {
            String configFile = System.getenv("CONFIG_FILE");
            getStandaloneConfig(configFile);

        } else {
            getConfig();
        }

        log.info("Starting spring boot application");
        SpringApplication.run(Application.class, args);
    }

    /**
     * Get Configuration from config file.
     * 
     * @param configFile : location of the config file.
     */
    public static void getStandaloneConfig(String configFile) {

        log.info("Running in standalone mode");

        String configAllJson = readFromFile(configFile);

        JsonObject configAll = new Gson().fromJson(configAllJson, JsonObject.class);

        JsonObject config = configAll.getAsJsonObject("config");

        Configuration.getInstance().updateConfigurationFromJsonObject(config);

        return;
    }

    /**
     * Get config from cbs.
     * 
     */
    public static void getConfig() {

        ConfigFetchFromCbs configFetchFromCbs = new ConfigFetchFromCbs(Duration.ofSeconds(60));
        Thread configFetchThread = new Thread(configFetchFromCbs);
        configFetchThread.start();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            log.debug("InterruptedException : {}", e);
            Thread.currentThread().interrupt();
        }
        log.info("after 10s sleep");
    }

    private static String readFromFile(String file) {
        String content = "";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            content = bufferedReader.readLine();
            String temp;
            while ((temp = bufferedReader.readLine()) != null) {
                content = content.concat(temp);
            }
            content = content.trim();
        } catch (Exception e) {
            content = null;
        }
        return content;
    }

}
