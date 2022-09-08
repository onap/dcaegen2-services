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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Kafka;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Thread that pulls messages from DMaaP and save them to Big Data DBs
 *
 * @author Guobiao Mo
 *
 */

@Service
@Scope("prototype")
public class Puller implements Runnable {

	@Autowired
	private StoreService storeService;

	@Autowired
	private TopicConfigPollingService topicConfigPollingService;

	@Autowired
	private ApplicationConfiguration config;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	//KafkaConsumer is not thread-safe.
	private ThreadLocal<KafkaConsumer<String, String>> consumerLocal = new ThreadLocal<>(); //<String, String> is key-value type, in our case key is empty, value is JSON text 

	private boolean active = false;
	private boolean async;
	
	private Kafka kafka;
	
	public Puller(Kafka kafka) {
		this.kafka = kafka;
	}

	@PostConstruct
	private void init() {
		async = config.isAsync();
	}

	private Properties getConsumerConfig() {
		Properties consumerConfig = new Properties();

		consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBrokerList());
		consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, kafka.getGroup());
		consumerConfig.put(ConsumerConfig.CLIENT_ID_CONFIG, String.valueOf(Thread.currentThread().getId()));
		consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		consumerConfig.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.RoundRobinAssignor");
		consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

		if (kafka.isSecure()) {
			String jaas = "org.apache.kafka.common.security.plain.PlainLoginModule required username=" + kafka.getLogin() + " password=" + kafka.getPass() + " serviceName=kafka;";
			consumerConfig.put("sasl.jaas.config", jaas);
			consumerConfig.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, kafka.getSecurityProtocol());
			consumerConfig.put("sasl.mechanism", "PLAIN");
		}
		return consumerConfig;
	}

	/**
	 * start pulling.
	 */
	@Override
	public void run() {
		active = true;
		Properties consumerConfig = getConsumerConfig();
		log.info("Kafka: {}, ConsumerConfig: {}", kafka, consumerConfig);
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfig);
		consumerLocal.set(consumer);

		DummyRebalanceListener rebalanceListener = new DummyRebalanceListener();

		try {
			while (active) {
				if (topicConfigPollingService.isActiveTopicsChanged(kafka)) {
					Collection<String> topics = topicConfigPollingService.getActiveTopics(kafka); 
					log.info("Active Topic list is changed, subscribe to the latest topics: {}", topics);
					consumer.subscribe(topics, rebalanceListener);
				}

				pull();
			}
			storeService.flush(); // force flush all buffer
		} catch (Exception e) {
			log.error("Puller run() exception.", e);
		} finally {
			consumer.close();
			log.info("Puller exited run().");
		}
	}

	private void pull() {
		KafkaConsumer<String, String> consumer = consumerLocal.get();

		log.debug("pulling...");
		ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(kafka.getTimeout()));
		log.debug("done pulling.");

		if (records != null && records.count() > 0) {
			List<Pair<Long, String>> messages = new ArrayList<>(records.count());
			for (TopicPartition partition : records.partitions()) {
				messages.clear();
				List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
				for (ConsumerRecord<String, String> record : partitionRecords) {
					messages.add(Pair.of(record.timestamp(), record.value()));
					//log.debug("threadid={} topic={}, timestamp={} key={}, offset={}, partition={}, value={}", id, record.topic(), record.timestamp(), record.key(), record.offset(), record.partition(), record.value());
				}
				storeService.saveMessages(kafka, partition.topic(), messages);
				log.info("saved to topic={} count={}", partition.topic(), partitionRecords.size());//TODO we may record this number to DB

				if (!async) {//for reliability, sync commit offset to Kafka right after saving the data to data store, this slows down a bit
					long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
					consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
				}
			}

			if (async) {//for high Throughput, async commit offset in batch to Kafka
				consumer.commitAsync();
			}
		} else {
			log.debug("no record from this polling.");
		}
		storeService.flushStall();
	}

	public void shutdown() {
		active = false;
	}

	private class DummyRebalanceListener implements ConsumerRebalanceListener {
		@Override
		public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
			log.info("Called onPartitionsRevoked with partitions: {}", partitions);
		}

		@Override
		public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
			log.info("Called onPartitionsAssigned with partitions: {}", partitions);
		}
	}

}
