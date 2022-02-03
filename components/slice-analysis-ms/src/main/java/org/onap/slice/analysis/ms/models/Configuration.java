/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020-2022 Wipro Limited.
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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private String cpsUrl;
    private String aaiUrl;
    private Boolean configDbEnabled;
    private String cg;
    private String cid;
    private int pollingInterval;
    private int pollingTimeout;
    private String aafUsername;
    private String aafPassword;
    private Map<String, Object> streamsSubscribes;
    private Map<String, Object> streamsPublishes;
    private int samples;
    private int minPercentageChange;
    private long initialDelaySeconds;
    private String rannfnssiDetailsTemplateId;
    private String desUrl;
    private int pmDataDurationInWeeks;

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

    public String getCpsUrl() {
        return cpsUrl;
    }

    public void setCpsUrl(String cpsUrl) {
        this.cpsUrl = cpsUrl;
    }

    public String getAaiUrl() {
        return aaiUrl;
    }

    public void setAaiUrl(String aaiUrl) {
        this.aaiUrl = aaiUrl;
    }

    public Boolean getConfigDbEnabled() {
        return configDbEnabled;
    }

    public void setConfigDbEnabled(Boolean configDbEnabled) {
        this.configDbEnabled = configDbEnabled;
    }

    public int getSamples() {
        return samples;
    }

    public void setSamples(int samples) {
        this.samples = samples;
    }

    public int getMinPercentageChange() {
        return minPercentageChange;
    }

    public void setMinPercentageChange(int minPercentageChange) {
        this.minPercentageChange = minPercentageChange;
    }

    public long getInitialDelaySeconds() {
        return initialDelaySeconds;
    }

    public void setInitialDelaySeconds(long initialDelaySeconds) {
        this.initialDelaySeconds = initialDelaySeconds;
    }

    /**
     * Get RannfnssiDetails TemplateId from Configuration
     */
    public String getRannfnssiDetailsTemplateId() {
        return rannfnssiDetailsTemplateId;
    }

    /**
     * Set RannfnssiDetails TemplateId
     */
    public void setRannfnssiDetailsTemplateId(String rannfnssiDetailsTemplateId) {
        this.rannfnssiDetailsTemplateId = rannfnssiDetailsTemplateId;
    }

    /**
     * Get Data Extraction Service Url
     */
    public String getDesUrl() {
        return desUrl;
    }

    /**
     * Set Data Extraction Service Url
     */
    public void setDesUrl(String desUrl) {
        this.desUrl = desUrl;
    }

    /**
     * Get duration for which PM data is to be fetched from DES
     */
    public int getPmDataDurationInWeeks() {
        return pmDataDurationInWeeks;
    }

    /**
     * Set duration for which PM data is to be fetched from DES
     */
    public void setPmDataDurationInWeeks(int pmDataDurationInWeeks) {
        this.pmDataDurationInWeeks = pmDataDurationInWeeks;
    }

    @Override
    public String toString() {
        return "Configuration [pgHost=" + pgHost + ", pgPort=" + pgPort + ", pgUsername=" + pgUsername + ", pgPassword="
                + pgPassword + ", dmaapServers=" + dmaapServers + ", configDbService=" + configDbService + ", cpsUrl="
                + cpsUrl + ", aaiUrl=" + aaiUrl + ", configDbEnabled=" + configDbEnabled + ", cg=" + cg + ", cid=" + cid
                + ", pollingInterval=" + pollingInterval + ", pollingTimeout=" + pollingTimeout + ", aafUsername="
                + aafUsername + ", aafPassword=" + aafPassword + ", streamsSubscribes=" + streamsSubscribes
                + ", streamsPublishes=" + streamsPublishes + ", samples=" + samples + ", minPercentageChange="
                + minPercentageChange + ", initialDelaySeconds=" + initialDelaySeconds + ", rannfnssiDetailsTemplateId="
                + rannfnssiDetailsTemplateId + ", desUrl=" + desUrl + ", pmDataDurationInWeeks=" + pmDataDurationInWeeks
                + "]";
    }

    /**
     * updates application configuration.
     */
    public void updateConfigurationFromJsonObject(JsonObject jsonObject) {

        log.info("Updating configuration from CBS");

        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();

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
        configDbEnabled = jsonObject.get("sliceanalysisms.configDbEnabled").getAsBoolean();

        pollingTimeout = jsonObject.get("sliceanalysisms.pollingTimeout").getAsInt();
        samples = jsonObject.get("sliceanalysisms.samples").getAsInt();
        minPercentageChange = jsonObject.get("sliceanalysisms.minPercentageChange").getAsInt();
        initialDelaySeconds = jsonObject.get("sliceanalysisms.initialDelaySeconds").getAsLong();
        rannfnssiDetailsTemplateId = jsonObject.get("sliceanalysisms.rannfnssiDetailsTemplateId").getAsString();
        desUrl = jsonObject.get("sliceanalysisms.desUrl").getAsString();
        pmDataDurationInWeeks = jsonObject.get("sliceanalysisms.pmDataDurationInWeeks").getAsInt();

        if (Objects.isNull(jsonObject.get("aafUsername"))) {
            aafUsername = null;
        } else {
            aafUsername = jsonObject.get("aafUsername").getAsString();
        }
        if (Objects.isNull(jsonObject.get("aafPassword"))) {
            aafPassword = null;
        } else {
            aafPassword = jsonObject.get("aafPassword").getAsString();
        }
        if (Objects.isNull(jsonObject.get("sliceanalysisms.aai.url"))) {
            aaiUrl = null;
        } else {
            aaiUrl = jsonObject.get("sliceanalysisms.aai.url").getAsString();
        }
        if (Objects.isNull(jsonObject.get("sliceanalysisms.cps.url"))) {
            cpsUrl = null;
        } else {
            cpsUrl = jsonObject.get("sliceanalysisms.cps.url").getAsString();
        }
        log.info("configuration from CBS {}", this);
    }
}
