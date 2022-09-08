
/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
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

package org.onap.slice.analysis.ms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Map;

import javax.sql.DataSource;

import org.onap.slice.analysis.ms.controller.ConfigFetchFromCbs;
import org.onap.slice.analysis.ms.models.ConfigPolicy;
import org.onap.slice.analysis.ms.models.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * Entry point for the slice analysis service application 
 *
 */
@EnableScheduling
@SpringBootApplication
public class Application {

    private static Logger log = LoggerFactory.getLogger(Application.class);

    /**
     * Main method where initial configuration and context is set 
     * @param args
     */
	public static void main(String[] args) {
		getConfig();
        log.info("Starting spring boot application");
		SpringApplication.run(Application.class, args);
		MainThread.initiateThreads();
	}
	
    private static void getConfig() {

        Boolean standalone = Boolean.parseBoolean(System.getenv("STANDALONE"));

        if (standalone) {
            log.info("Running in standalone mode");

            String configFile = System.getenv("CONFIG_FILE");
            String configAllJson = readFromFile(configFile);

            JsonObject configAll = new Gson().fromJson(configAllJson, JsonObject.class);

            JsonObject config = configAll.getAsJsonObject("config");

            Configuration.getInstance().updateConfigurationFromJsonObject(config);

            ConfigPolicy configPolicy = ConfigPolicy.getInstance();
            Type mapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            if (configAll.getAsJsonObject("policies") != null) {
                JsonObject policyJson = configAll.getAsJsonObject("policies").getAsJsonArray("items").get(0)
                        .getAsJsonObject().getAsJsonObject("config");
                Map<String, Object> policy = new Gson().fromJson(policyJson, mapType);
                configPolicy.setConfig(policy);
                log.info("Config policy {}", configPolicy);
            }
            return;
        }

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

    /**
     * DataSource bean.
     */
    @Bean
    public DataSource dataSource() {
        Configuration configuration = Configuration.getInstance();

        String url = "jdbc:postgresql://" + configuration.getPgHost() + ":" + configuration.getPgPort() + "/sliceanalysisms";

        return DataSourceBuilder.create().url(url).username(configuration.getPgUsername())
                .password(configuration.getPgPassword()).build();
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
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
