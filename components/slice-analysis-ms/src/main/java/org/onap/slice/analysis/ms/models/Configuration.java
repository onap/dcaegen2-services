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

package org.onap.slice.analysis.ms.models;

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
 * Model class for the application Configuration
 */
public class Configuration {
    private static Logger log = LoggerFactory.getLogger(Configuration.class);

    private static Configuration instance = null;
    private String pgHost;
    private int pgPort;
    private String pgUsername;
    private String pgPassword;
    private List<String> dmaapServers;
    private String configDbService;
    private String cg;
    private String cid;
    private int pollingInterval;
    private int pollingTimeout;
    private String aafUsername;
    private String aafPassword;
    private Map<String, Object> streamsSubscribes;
    private Map<String, Object> streamsPublishes;
    private List<String> pmNames;
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

    protected Configuration() {

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

    public String getCg() {
        return cg;
    }

    public void setCg(String cg) {
        this.cg = cg;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
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

    public String getPgHost() {
        return pgHost;
    }

    public void setPgHost(String pgHost) {
        this.pgHost = pgHost;
    }

    public int getPgPort() {
        return pgPort;
    }

    public void setPgPort(int pgPort) {
        this.pgPort = pgPort;
    }

    public String getPgUsername() {
        return pgUsername;
    }

    public void setPgUsername(String pgUsername) {
        this.pgUsername = pgUsername;
    }

    public String getPgPassword() {
        return pgPassword;
    }

    public void setPgPassword(String pgPassword) {
        this.pgPassword = pgPassword;
    }

    public List<String> getDmaapServers() {
        return dmaapServers;
    }

    public void setDmaapServers(List<String> dmaapServers) {
        this.dmaapServers = dmaapServers;
    }

    public String getConfigDbService() {
        return configDbService;
    }

    public void setConfigDbService(String configDbService) {
        this.configDbService = configDbService;
    }


    public List<String> getPmNames() {
		return pmNames;
	}

	public void setPmNames(List<String> pmNames) {
		this.pmNames = pmNames;
	}

	@Override
	public String toString() {
		return "Configuration [pgHost=" + pgHost + ", pgPort=" + pgPort + ", pgUsername=" + pgUsername + ", pgPassword="
				+ pgPassword + ", dmaapServers=" + dmaapServers + ", configDbService=" + configDbService + ", cg=" + cg
				+ ", cid=" + cid + ", pollingInterval=" + pollingInterval + ", pollingTimeout=" + pollingTimeout
				+ ", aafUsername=" + aafUsername + ", aafPassword=" + aafPassword + ", streamsSubscribes="
				+ streamsSubscribes + ", streamsPublishes=" + streamsPublishes + "]";
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

        pgPort = jsonObject.get("postgres.port").getAsInt();
        pollingInterval = jsonObject.get("sliceanalysisms.pollingInterval").getAsInt();
        pgPassword = jsonObject.get("postgres.password").getAsString();
        pgUsername = jsonObject.get("postgres.username").getAsString();
        pgHost = jsonObject.get("postgres.host").getAsString();

        JsonArray servers = jsonObject.getAsJsonArray("sliceanalysisms.dmaap.server");
        Type listType = new TypeToken<List<String>>() {}.getType();
        dmaapServers = new Gson().fromJson(servers, listType);

        cg = jsonObject.get("sliceanalysisms.cg").getAsString();
        cid = jsonObject.get("sliceanalysisms.cid").getAsString();
        configDbService = jsonObject.get("sliceanalysisms.configDb.service").getAsString();

        pollingTimeout = jsonObject.get("sliceanalysisms.pollingTimeout").getAsInt();
        JsonArray pmNameList = jsonObject.getAsJsonArray("sliceanalysisms.pmNames");
        listType = new TypeToken<List<String>>() {}.getType();
        dmaapServers = new Gson().fromJson(pmNameList, listType);

        log.info("configuration from CBS {}", this);

    }



}
