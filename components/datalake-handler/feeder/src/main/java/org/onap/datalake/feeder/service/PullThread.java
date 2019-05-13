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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Thread that pulls messages from DMaaP and save them to Big Data DBs
 *
 * @author Guobiao Mo
 *
 */

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PullThread implements Runnable {

	@Autowired
	private DmaapService dmaapService;

	@Autowired
	private StoreService storeService;

	@Autowired
	private ApplicationConfiguration config;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private KafkaConsumer<String, String> consumer; //<String, String> is key-value type, in our case key is empty, value is JSON text
	private int id;

	private final AtomicBoolean active = new AtomicBoolean(false);
	private boolean async;

	public PullThread(int id) {
		this.id = id;
	}

	@PostConstruct
	private void init() {
		async = config.isAsync();
		Properties consumerConfig = getConsumerConfig();
		log.info("Kafka ConsumerConfig: {}", consumerConfig);
		consumer = new KafkaConsumer<>(consumerConfig);
	}

	private Properties getConsumerConfig() {
		Properties consumerConfig = new Properties();

		consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getDmaapKafkaHostPort());
		consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, config.getDmaapKafkaGroup());
		consumerConfig.put(ConsumerConfig.CLIENT_ID_CONFIG, String.valueOf(id));
		consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		consumerConfig.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.RoundRobinAssignor");
		consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

		return consumerConfig;
	}

	/**
	 * start pulling.
	 */
	@Override
	public void run() {
		active.set(true);

		DummyRebalanceListener rebalanceListener = new DummyRebalanceListener();

		try {
			List<String> topics = dmaapService.getActiveTopics(); //TODO get updated topic list within loop

			log.info("Thread {} going to subscribe to topics: {}", id, topics);

			consumer.subscribe(topics, rebalanceListener);

			while (active.get()) {

				ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(config.getDmaapKafkaTimeout()));
				if (records != null) {
					List<Pair<Long, String>> messages = new ArrayList<>(records.count());
					for (TopicPartition partition : records.partitions()) {
						messages.clear();
						List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
						for (ConsumerRecord<String, String> record : partitionRecords) {
							messages.add(Pair.of(record.timestamp(), record.value()));
							//log.debug("threadid={} topic={}, timestamp={} key={}, offset={}, partition={}, value={}", id, record.topic(), record.timestamp(), record.key(), record.offset(), record.partition(), record.value());
						}
						storeService.saveMessages(partition.topic(), messages);
						log.info("saved to topic={} count={}", partition.topic(), partitionRecords.size());//TODO we may record this number to DB

						if (!async) {//for reliability, sync commit offset to Kafka, this slows down a bit
							long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
							consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
						}
					}

					if (async) {//for high Throughput, async commit offset in batch to Kafka
						consumer.commitAsync();
					}
				}
				storeService.flushStall();
			}
		} catch (Exception e) {
			log.error("Puller {} run():   exception={}", id, e.getMessage());
			log.error("", e);
		} finally {
			consumer.close();
		}
	}

	public void shutdown() {
		active.set(false);
		consumer.wakeup();
		consumer.unsubscribe();
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
