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
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.dto.KafkaConfig;
import org.onap.datalake.feeder.repository.KafkaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KafkaServiceTest {

    @InjectMocks
    private KafkaService kafkaService;

    @Mock
    private KafkaRepository kafkaRepository;

    @Mock
    private KafkaConfig kafkaConfig;

    @Test
    public void testKafkaServer(){
        int kafkaId = 123;
        Kafka kafka = new Kafka();
        kafka.setId(kafkaId);

        List<Kafka> kafkas = new ArrayList<>();
        kafkas.add(kafka);

        when(kafkaRepository.findById(kafkaId)).thenReturn(Optional.of(kafka));
        Kafka kafkaById = kafkaService.getKafkaById(kafkaId);
        assertEquals(kafka,kafkaById);

        when(kafkaRepository.findAll()).thenReturn(kafkas);
        assertNotNull(kafkaService.getAllKafka());

        kafkaService.fillKafkaConfiguration(kafkaConfig);
    }

}