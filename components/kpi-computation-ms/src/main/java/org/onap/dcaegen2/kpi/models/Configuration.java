/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 China Mobile.
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

package org.onap.dcaegen2.kpi.models;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Model class for the application Configuration.
 */
public class Configuration {
    private static Logger log = LoggerFactory.getLogger(Configuration.class);

    private static Configuration instance = null;
    private List<String> dmaapServers;
    private int pollingInterval;
    private int pollingTimeout;
    private int cbsPollingInterval;
    private String aafUsername;
    private String aafPassword;
    private Map<String, Object> streamsSubscribes;
    private Map<String, Object> streamsPublishes;
    private String kpiConfig;
    private String host;
    private int port;
    private String username;
    private String password;
    private String databasename;
    private boolean enablessl;
    private String cg;
    private String cid;

    public int getCbsPollingInterval() {
        return cbsPollingInterval;
    }

    public void setCbsPollingInterval(int cbsPollingInterval) {
        this.cbsPollingInterval = cbsPollingInterval;
    }

    public String getCg() {
        return cg;
    }

    public String getCid() {
        return cid;
    }

    public void setCg(String cg) {
        this.cg = cg;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public List<String> getDmaapServers() {
        return dmaapServers;
    }

    public void setDmaapServers(List<String> dmaapServers) {
        this.dmaapServers = dmaapServers;
    }

    public boolean isEnablessl() {
        return enablessl;
    }

    public void setEnablessl(boolean enablessl) {
        this.enablessl = enablessl;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabasename() {
        return databasename;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDatabasename(String databasename) {
        this.databasename = databasename;
    }

    /**
     * Check if topic is secure.
     */
    public boolean isSecured() {
        return (aafUsername != null);

    }

    public String getAafUsername() {
        return aafUsername;
    }

    public void setAafUsername(String aafUsername) {
        this.aafUsername = aafUsername;
    }

    public String getAafPassword() {
        return aafPassword;
    }

    public void setAafPassword(String aafPassword) {
        this.aafPassword = aafPassword;
    }

    public Map<String, Object> getStreamsSubscribes() {
        return streamsSubscribes;
    }

    public void setStreamsSubscribes(Map<String, Object> streamsSubscribes) {
        this.streamsSubscribes = streamsSubscribes;
    }

    public Map<String, Object> getStreamsPublishes() {
        return streamsPublishes;
    }

    public void setStreamsPublishes(Map<String, Object> streamsPublishes) {
        this.streamsPublishes = streamsPublishes;
    }

    public Configuration() {

    }

    /**
     * Get instance of class.
     */
    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public int getPollingTimeout() {
        return pollingTimeout;
    }

    public void setPollingTimeout(int pollingTimeout) {
        this.pollingTimeout = pollingTimeout;
    }

    @Override
    public String toString() {
        return "Configuration [dmaapServers=" + dmaapServers + ", pollingInterval=" + pollingInterval
                + ", pollingTimeout=" + pollingTimeout + ", aafUsername=" + aafUsername + ", aafPassword=" + aafPassword
                + ", streamsSubscribes=" + streamsSubscribes + ", streamsPublishes=" + streamsPublishes + ", kpiConfig="
                + kpiConfig + "]";
    }

    /**
     * updates application configuration.
     */
    public void updateConfigurationFromJsonObject(JsonObject jsonObject) {

        log.info("Updating configuration from CBS");

        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();

        JsonObject subscribes = jsonObject.getAsJsonObject("streams_subscribes");
        streamsSubscribes = new Gson().fromJson(subscribes, mapType);

        JsonObject publishes = jsonObject.getAsJsonObject("streams_publishes");
        streamsPublishes = new Gson().fromJson(publishes, mapType);

        pollingInterval = jsonObject.get("pollingInterval").getAsInt();
        pollingTimeout = jsonObject.get("pollingTimeout").getAsInt();
        cbsPollingInterval = jsonObject.get("cbsPollingInterval").getAsInt();
        JsonArray servers = jsonObject.getAsJsonArray("dmaap.server");
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        dmaapServers = new Gson().fromJson(servers, listType);

        if (jsonObject.get("aafUsername") == null) {
            aafUsername = null;
        } else {
            aafUsername = jsonObject.get("aafUsername").getAsString();
        }
        if (jsonObject.get("aafPassword") == null) {
            aafPassword = null;
        } else {
            aafPassword = jsonObject.get("aafPassword").getAsString();
        }

        kpiConfig = jsonObject.get("kpi.policy").getAsString();

        log.info("kpi.policy {}", kpiConfig);
        // enablessl = jsonObject.get("mongo.enablessl").getAsBoolean();
        cg = jsonObject.get("cg").getAsString();
        cid = jsonObject.get("cid").getAsString();
        log.info("configuration from CBS {}", this);

    }
    
    public void updateConfigFromPolicy(JsonObject policyconfig) {
  	    kpiConfig = policyconfig.toString();
  	    log.info("kpi config fetched from policy {}", kpiConfig);
    }

    public String getKpiConfig() {
        return kpiConfig;
    }

    public void setKpiConfig(String kpiConfig) {
        this.kpiConfig = kpiConfig;
    }

}
