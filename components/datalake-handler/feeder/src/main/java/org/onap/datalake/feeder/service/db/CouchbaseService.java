/*
* ============LICENSE_START=======================================================
* ONAP : DATALAKE
* ================================================================================
* Copyright 2019 China Mobile
* Copyright 2026 Deutsche Telekom AG. All rights reserved.
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

package org.onap.datalake.feeder.service.db;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.json.JSONObject;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.EffectiveTopic;
import org.onap.datalake.feeder.domain.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.couchbase.client.core.env.TimeoutConfig;
import com.couchbase.client.core.error.DocumentExistsException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.InsertOptions;
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;

/**
 * Service to use Couchbase
 *
 * @author Guobiao Mo
 *
 */
@Service
@Scope("prototype")
public class CouchbaseService implements DbStoreService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ApplicationConfiguration config;

    private Db couchbase;
    Cluster cluster;
    // Collection is thread-safe
    Collection collection;

    public CouchbaseService(Db db) {
        couchbase = db;
    }

    @PostConstruct
    @Override
    public void init() {
        // Initialize Couchbase Connection
        try {
            ClusterEnvironment env = ClusterEnvironment.builder()
                    .timeoutConfig(TimeoutConfig.connectTimeout(Duration.ofSeconds(60)))
                    .build();
            cluster = Cluster.connect(
                    couchbase.getHost(),
                    ClusterOptions.clusterOptions(couchbase.getLogin(), couchbase.getPass()).environment(env));
            collection = cluster.bucket(couchbase.getDatabase()).defaultCollection();
            cluster.queryIndexes().createPrimaryIndex(
                    couchbase.getDatabase(),
                    CreatePrimaryQueryIndexOptions.createPrimaryQueryIndexOptions().ignoreIfExists(true));

            log.info("Connected to Couchbase {} as {}", couchbase.getHost(), couchbase.getLogin());
        } catch (Exception ex) {
            log.error("error connection to Couchbase.", ex);
        }
    }

    @PreDestroy
    public void cleanUp() {
        config.getShutdownLock().readLock().lock();

        try {
            log.info("cluster.disconnect() at cleanUp.");
            cluster.disconnect();
        } finally {
            config.getShutdownLock().readLock().unlock();
        }
    }

    @Override
    public void saveJsons(EffectiveTopic effectiveTopic, List<JSONObject> jsons) {
        for (JSONObject json : jsons) {
            JsonObject jsonObject = JsonObject.fromJson(json.toString());
            long timestamp = jsonObject.getLong(config.getTimestampLabel());
            long expiryEpochSec = (timestamp / 1000L) + (long) effectiveTopic.getTopic().getTtl() * 3600 * 24;
            String id = getId(effectiveTopic.getTopic(), json);
            try {
                collection.insert(id, jsonObject,
                        InsertOptions.insertOptions().expiry(Instant.ofEpochSecond(expiryEpochSec)));
            } catch (DocumentExistsException e) {
                log.error("saveJsons() DocumentExistsException for id {}", id);
            } catch (Exception e) {
                log.error("error saving to Couchbase.", e);
            }
        }
        log.debug("saved text to topic = {}, this batch count = {} ", effectiveTopic, jsons.size());
    }

    public String getId(Topic topic, JSONObject json) {
        String id = topic.getMessageId(json);
        if (id != null) {
            return id;
        }
        return topic.getName() + ":" + UUID.randomUUID();
    }

}
