/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2019 China Mobile
 * Copyright (C) 2022 Wipro Limited.
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

package org.onap.datalake.feeder.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.dto.KafkaConfig;
import org.onap.datalake.feeder.repository.KafkaRepository;
import org.onap.datalake.feeder.service.KafkaService;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KafkaControllerTest {

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private BindingResult mockBindingResult;

    @Mock
    private KafkaService kafkaService;

    @Mock
    private KafkaRepository kafkaRepository;

    @Mock
    private Kafka kafka;

    @InjectMocks
    private KafkaController kafkaController;
    @Test
    public void createKafka() throws IOException {

        int id = 123;
        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.setId(id);
        kafkaConfig.setName("123");
        when(kafkaService.getKafkaById(kafkaConfig.getId())).thenReturn(null).thenReturn(kafka);
        when(kafkaRepository.save(kafka)).thenReturn(null);
        when(kafkaService.fillKafkaConfiguration(kafkaConfig)).thenReturn(kafka);
        when(mockBindingResult.hasErrors()).thenReturn(false, true, false, true);

        kafkaController.createKafka(kafkaConfig, mockBindingResult, httpServletResponse);
        kafkaController.createKafka(kafkaConfig, mockBindingResult, httpServletResponse);

        kafkaController.updateKafka(kafkaConfig, mockBindingResult, id, httpServletResponse);
        kafkaController.updateKafka(kafkaConfig, mockBindingResult, id, httpServletResponse);

        kafkaController.deleteKafka(id, httpServletResponse);

        when(kafkaService.getAllKafka()).thenReturn(null);
        kafkaController.queryAllKafka();
    }

    @Test
    public void testCreateKafkaNull() throws IOException {
        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.setId(1);
        kafkaConfig.setName("123");
        when(kafkaService.getKafkaById(kafkaConfig.getId())).thenReturn(kafka);
        assertEquals(null, kafkaController.createKafka(kafkaConfig, mockBindingResult, httpServletResponse));
    }

    @Test
    public void testCreateKafkaException() throws IOException {
        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.setId(1);
        kafkaConfig.setName("123");
        when(kafkaService.getKafkaById(kafkaConfig.getId())).thenReturn(null);
        when(kafkaService.fillKafkaConfiguration(kafkaConfig)).thenThrow(NullPointerException.class);
        assertEquals(null, kafkaController.createKafka(kafkaConfig, mockBindingResult, httpServletResponse));
    }

    @Test
    public void testUpdateKafkaNull() throws IOException {
        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.setId(1);
        kafkaConfig.setName("123");
        when(kafkaService.getKafkaById(kafkaConfig.getId())).thenReturn(null);
        assertEquals(null, kafkaController.updateKafka(kafkaConfig, mockBindingResult, 1, httpServletResponse));
    }

    @Test
    public void testUpdateKafkaException() throws IOException {
        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.setId(1);
        kafkaConfig.setName("123");
        when(kafkaService.getKafkaById(kafkaConfig.getId())).thenReturn(kafka);
        doThrow(NullPointerException.class).when(kafkaService).fillKafkaConfiguration(kafkaConfig, kafka);
        assertEquals(null, kafkaController.updateKafka(kafkaConfig, mockBindingResult, 1, httpServletResponse));
    }

    @Test
    public void testDeleteKafkaNull() throws IOException {
        when(kafkaService.getKafkaById(1)).thenReturn(null);
        kafkaController.deleteKafka(1, httpServletResponse);
    }

    @Test
    public void testGetKafkaDetailNull() throws IOException {
        when(kafkaService.getKafkaById(1)).thenReturn(null);
        kafkaController.getKafkaDetail(1, httpServletResponse);
    }

    @Test
    public void testGetKafkaDetail() throws IOException {
        when(kafkaService.getKafkaById(1)).thenReturn(kafka);
        assertEquals(null, kafkaController.getKafkaDetail(1, httpServletResponse));
    }

}
