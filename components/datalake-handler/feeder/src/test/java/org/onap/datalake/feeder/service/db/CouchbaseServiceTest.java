/*-
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE
 * ================================================================================
 * Copyright (C) 2018-2019 Huawei. All rights reserved.
 * Copyright (C) 2022 Wipro Limited.
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

package org.onap.datalake.feeder.service.db;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.EffectiveTopic;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.util.TestUtil;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;

@RunWith(MockitoJUnitRunner.class)
public class CouchbaseServiceTest {

    @InjectMocks
    private CouchbaseService couchbaseService;

    @Mock
    private ApplicationConfiguration config;

    @Mock
    private Cluster cluster;

    @Mock
    private Collection collection;

    @Before
    public void init() {
        Db db = TestUtil.newDb("Couchbasedb");
        db.setDatabase("database");
        db.setLogin("login");
        couchbaseService = new CouchbaseService(db);
        couchbaseService.config = config;
        couchbaseService.cluster = cluster;
        couchbaseService.collection = collection;
    }

    @Test
    public void testSaveJsonsWithTopicId() {
        ApplicationConfiguration appConfig = new ApplicationConfiguration();
        appConfig.setTimestampLabel("datalake_ts_");

        String text = "{ data: { data2 : { value : 'hello'}}}";
        JSONObject json = new JSONObject(text);

        Topic topic = TestUtil.newTopic("test getMessageId");
        topic.setMessageIdPath("/data/data2/value");
        List<JSONObject> jsons = new ArrayList<>();
        json.put(appConfig.getTimestampLabel(), 1234);
        jsons.add(json);
        CouchbaseService cs = new CouchbaseService(new Db());
        cs.collection = collection;
        cs.config = appConfig;

        EffectiveTopic effectiveTopic = new EffectiveTopic(topic, "test");
        cs.saveJsons(effectiveTopic, jsons);
    }

    @Test
    public void testSaveJsonsWithOutTopicId() {
        ApplicationConfiguration appConfig = new ApplicationConfiguration();
        appConfig.setTimestampLabel("datalake_ts_");

        String text = "{ data: { data2 : { value : 'hello'}}}";
        JSONObject json = new JSONObject(text);

        Topic topic = TestUtil.newTopic("test getMessageId");
        List<JSONObject> jsons = new ArrayList<>();
        json.put(appConfig.getTimestampLabel(), 1234);
        jsons.add(json);
        CouchbaseService cs = new CouchbaseService(new Db());
        cs.collection = collection;
        cs.config = appConfig;

        EffectiveTopic effectiveTopic = new EffectiveTopic(topic, "test");
        cs.saveJsons(effectiveTopic, jsons);
    }

    @Test
    public void testCleanupBucket() {
        when(config.getShutdownLock()).thenReturn(new ReentrantReadWriteLock());
        couchbaseService.cleanUp();
    }

}
