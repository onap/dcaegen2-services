/*
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE
 * ================================================================================
 * Copyright 2019 China Mobile
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

package org.onap.datalake.feeder.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PullServiceTest {

    @InjectMocks
    private PullService pullService;

    @Mock
    private ApplicationContext context;

    @Mock
    private ApplicationConfiguration config;

    @Mock
    private ExecutorService executorService;

    @Mock
    private List<Puller> consumers;

    @Test
    public void isRunning() {
        assertEquals(pullService.isRunning(), false);
    }

    @Test(expected = NullPointerException.class)
    public void start() {

        when(config.getKafkaConsumerCount()).thenReturn(1);

        pullService.start();
    }

    @Test
    public void shutdown() {
        pullService.shutdown();
    }
}