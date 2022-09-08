/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
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

import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.dto.KafkaConfig;
import org.onap.datalake.feeder.repository.KafkaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for kafkas
 *
 * @author guochunmeng
 */
@Service
public class KafkaService {

    @Autowired
    private KafkaRepository kafkaRepository;

    public Kafka getKafkaById(int id) {

        Optional<Kafka> ret = kafkaRepository.findById(id);
        return ret.isPresent() ? ret.get() : null;
    }

    public List<KafkaConfig> getAllKafka() {

        List<KafkaConfig> kafkaConfigList = new ArrayList<>();
        Iterable<Kafka> kafkaIterable = kafkaRepository.findAll();
        for(Kafka portal : kafkaIterable) {
            kafkaConfigList.add(portal.getKafkaConfig());
        }
        return kafkaConfigList;
    }

    public Kafka fillKafkaConfiguration(KafkaConfig kafkaConfig) {
        Kafka kafka = new Kafka();
        fillKafka(kafkaConfig, kafka);
        return kafka;
    }

    public void fillKafkaConfiguration(KafkaConfig kafkaConfig, Kafka kafka) {
        fillKafka(kafkaConfig, kafka);
    }

    private void fillKafka(KafkaConfig kafkaConfig, Kafka kafka) {

        kafka.setId(kafkaConfig.getId());
        kafka.setBrokerList(kafkaConfig.getBrokerList());
        kafka.setConsumerCount(kafkaConfig.getConsumerCount());
        kafka.setEnabled(kafkaConfig.isEnabled());
        kafka.setExcludedTopic(kafkaConfig.getExcludedTopic());
        kafka.setIncludedTopic(kafkaConfig.getIncludedTopic());
        kafka.setGroup(kafkaConfig.getGroup());
        kafka.setLogin(kafkaConfig.getLogin());
        kafka.setName(kafkaConfig.getName());
        kafka.setPass(kafkaConfig.getPass());
        kafka.setSecure(kafkaConfig.isSecure());
        kafka.setSecurityProtocol(kafkaConfig.getSecurityProtocol());
        kafka.setTimeout(kafkaConfig.getTimeout());
        kafka.setZooKeeper(kafkaConfig.getZooKeeper());

    }

}
