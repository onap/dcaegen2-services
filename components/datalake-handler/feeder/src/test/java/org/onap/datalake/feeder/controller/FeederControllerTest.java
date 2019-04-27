/*-
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE
 * ================================================================================
 * Copyright (C) 2018-2019 Huawei. All rights reserved.
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
package org.onap.datalake.feeder.controller;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.service.DmaapService;
import org.onap.datalake.feeder.service.PullService;
import org.onap.datalake.feeder.service.PullThread;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


public class FeederControllerTest {

    @InjectMocks
    private PullService pullService1;

    @Mock
    private ApplicationConfiguration config;

    @Mock
    private ApplicationContext context;

    @Mock
    private DmaapService dmaapService1;

    @Mock
    private KafkaConsumer<String, String> kafkaConsumer;

    @Before
    public void setupTest() {
        MockitoAnnotations.initMocks(this);
    }

    private void setAccessPrivateFields(FeederController feederController) throws NoSuchFieldException,
            IllegalAccessException {
        Field pullService = feederController.getClass().getDeclaredField("pullService");
        pullService.setAccessible(true);
        pullService.set(feederController, pullService1);
    }

    @Test
    public void testStart() throws IOException, NoSuchFieldException, IllegalAccessException {
        FeederController feederController = new FeederController();
        setAccessPrivateFields(feederController);
        PullService pullService2 = new PullService();
        Field applicationConfig = pullService2.getClass().getDeclaredField("config");
        applicationConfig.setAccessible(true);
        applicationConfig.set(pullService2, config);
        Field applicationContext = pullService2.getClass().getDeclaredField("context");
        applicationContext.setAccessible(true);
        applicationContext.set(pullService2, context);
        when(config.getKafkaConsumerCount()).thenReturn(1);
        PullThread pullThread = new PullThread(1);
        Field dmaapService = pullThread.getClass().getDeclaredField("dmaapService");
        dmaapService.setAccessible(true);
        dmaapService.set(pullThread, dmaapService1);
        Field kafkaConsumer1 = pullThread.getClass().getDeclaredField("consumer");
        kafkaConsumer1.setAccessible(true);
        kafkaConsumer1.set(pullThread, kafkaConsumer);
        applicationConfig = pullThread.getClass().getDeclaredField("config");
        applicationConfig.setAccessible(true);
        applicationConfig.set(pullThread, config);
        when(context.getBean(PullThread.class, 0)).thenReturn(pullThread);
        ConsumerRecords<String, String> records = ConsumerRecords.empty();
        when(kafkaConsumer.poll(2)).thenReturn(records);
        String start = feederController.start();
        assertEquals("DataLake feeder is running.", start);
    }

    @Test
    public void testStop() throws NoSuchFieldException, IllegalAccessException {
        FeederController feederController = new FeederController();
        setAccessPrivateFields(feederController);
        String stop = feederController.stop();
        assertEquals("DataLake feeder is stopped.", stop);
    }

    @Test
    public void testStatus() throws NoSuchFieldException, IllegalAccessException {
        FeederController feederController = new FeederController();
        setAccessPrivateFields(feederController);
        String status = feederController.status();
        assertEquals("Feeder is running: false", status);
    }
}
