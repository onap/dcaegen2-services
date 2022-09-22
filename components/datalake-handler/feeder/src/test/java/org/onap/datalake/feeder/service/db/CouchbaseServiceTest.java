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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.After;
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
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.mock.Bucket;
import com.couchbase.mock.BucketConfiguration;
import com.couchbase.mock.CouchbaseMock;
import com.couchbase.mock.client.MockClient;

@RunWith(MockitoJUnitRunner.class)
public class CouchbaseServiceTest {
    protected final BucketConfiguration bucketConfiguration = new BucketConfiguration();
    protected MockClient mockClient;
    protected CouchbaseMock couchbaseMock;
    protected Cluster cluster;
    protected com.couchbase.client.java.Bucket bucket;
    protected int carrierPort;
    protected int httpPort;

    @InjectMocks
    private CouchbaseService couchbaseService;

    @Mock
    private ApplicationConfiguration config;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        Db db = TestUtil.newDb("Couchbasedb");
        db.setDatabase("database");
        db.setLogin("login");
        couchbaseService = new CouchbaseService(db);

        Field configField = CouchbaseService.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(couchbaseService, config);
        couchbaseService.bucket = bucket;
        couchbaseService.init();
    }

    protected void getPortInfo(String bucket) throws Exception {
        httpPort = couchbaseMock.getHttpPort();
        carrierPort = couchbaseMock.getCarrierPort(bucket);
    }

    protected void createMock(@NotNull String name, @NotNull String password) throws Exception {
        bucketConfiguration.numNodes = 1;
        bucketConfiguration.numReplicas = 1;
        bucketConfiguration.numVBuckets = 1024;
        bucketConfiguration.name = name;
        bucketConfiguration.type = Bucket.BucketType.COUCHBASE;
        bucketConfiguration.password = password;
        ArrayList < BucketConfiguration > configList = new ArrayList < BucketConfiguration > ();
        configList.add(bucketConfiguration);
        couchbaseMock = new CouchbaseMock(0, configList);
        couchbaseMock.start();
        couchbaseMock.waitForStartup();
    }

    protected void createClient() {
        cluster = CouchbaseCluster.create(DefaultCouchbaseEnvironment.builder().bootstrapCarrierDirectPort(carrierPort)
            .bootstrapHttpDirectPort(httpPort).build(), "couchbase://127.0.0.1");
        bucket = cluster.openBucket("default");
    }

    @Before
    public void setUp() throws Exception {
        createMock("default", "");
        getPortInfo("default");
        createClient();
    }

    @After
    public void tearDown() {
        if (cluster != null) {
            cluster.disconnect();
        }
        if (couchbaseMock != null) {
            couchbaseMock.stop();
        }
        if (mockClient != null) {
            mockClient.shutdown();
        }
    }

    @Test
    public void testSaveJsonsWithTopicId() {
        ApplicationConfiguration appConfig = new ApplicationConfiguration();
        appConfig.setTimestampLabel("datalake_ts_");

        String text = "{ data: { data2 : { value : 'hello'}}}";

        JSONObject json = new JSONObject(text);

        Topic topic = TestUtil.newTopic("test getMessageId");
        topic.setMessageIdPath("/data/data2/value");
        List < JSONObject > jsons = new ArrayList < > ();
        json.put(appConfig.getTimestampLabel(), 1234);
        jsons.add(json);
        CouchbaseService couchbaseService = new CouchbaseService(new Db());
        couchbaseService.bucket = bucket;
        couchbaseService.config = appConfig;

        couchbaseService.init();
        EffectiveTopic effectiveTopic = new EffectiveTopic(topic, "test");
        couchbaseService.saveJsons(effectiveTopic, jsons);

    }

    @Test
    public void testSaveJsonsWithOutTopicId() {
        ApplicationConfiguration appConfig = new ApplicationConfiguration();
        appConfig.setTimestampLabel("datalake_ts_");

        String text = "{ data: { data2 : { value : 'hello'}}}";

        JSONObject json = new JSONObject(text);

        Topic topic = TestUtil.newTopic("test getMessageId");
        List < JSONObject > jsons = new ArrayList < > ();
        json.put(appConfig.getTimestampLabel(), 1234);
        jsons.add(json);
        CouchbaseService couchbaseService = new CouchbaseService(new Db());
        couchbaseService.bucket = bucket;
        couchbaseService.config = appConfig;

        couchbaseService.init();
        EffectiveTopic effectiveTopic = new EffectiveTopic(topic, "test");
        couchbaseService.saveJsons(effectiveTopic, jsons);
    }

    @Test
    public void testCleanupBucket() {
        when(config.getShutdownLock()).thenReturn(new ReentrantReadWriteLock());
        couchbaseService.cleanUp();
    }

}
