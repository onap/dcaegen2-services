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

package org.onap.datalake.feeder.service.db;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ShutdownHookManager;
import org.json.JSONObject;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.EffectiveTopic;
import org.onap.datalake.feeder.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import lombok.Getter;

/**
 * Service to write data to HDFS
 * 
 * @author Guobiao Mo
 *
 */
@Service
@Scope("prototype")
public class HdfsService implements DbStoreService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private Db hdfs;

	@Autowired
	ApplicationConfiguration config;

	FileSystem fileSystem;

	private ThreadLocal<Map<String, Buffer>> bufferLocal = ThreadLocal.withInitial(HashMap::new);
	private ThreadLocal<SimpleDateFormat> dayFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
	private ThreadLocal<SimpleDateFormat> timeFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS"));

	@Getter
	private class Buffer {
		long lastFlush;
		List<String> data;

		public Buffer() {
			lastFlush = Long.MIN_VALUE;
			data = new ArrayList<>();
		}

		public void flush(String topic) {
			try {
				if (!data.isEmpty()) {
					saveMessages(topic, data);
					data.clear();
					lastFlush = System.currentTimeMillis();
				}
			} catch (IOException e) {
				log.error("{} error saving to HDFS. {}", topic, e.getMessage());
			}
		}

		public void flushStall(String topic) {
			if (!data.isEmpty() && Util.isStall(lastFlush, config.getHdfsFlushInterval())) {
				log.debug("going to flushStall topic={}, buffer size={}", topic, data.size());
				flush(topic);
			}
		}

		/*
				public void addData(List<Pair<Long, String>> messages) {
					if (data.isEmpty()) { //reset the last flush time stamp to current if no existing data in buffer
						lastFlush = System.currentTimeMillis();
					}
		
					messages.stream().forEach(message -> data.add(message.getRight()));//note that message left is not used			
				}
		*/
		public void addData2(List<JSONObject> messages) {
			if (data.isEmpty()) { //reset the last flush time stamp to current if no existing data in buffer
				lastFlush = System.currentTimeMillis();
			}

			messages.stream().forEach(message -> data.add(message.toString()));
		}

		private void saveMessages(String topic, List<String> bufferList) throws IOException {

			long thread = Thread.currentThread().getId();
			Date date = new Date();
			String day = dayFormat.get().format(date);
			String time = timeFormat.get().format(date);

			InetAddress inetAddress = InetAddress.getLocalHost();
			String hostName = inetAddress.getHostName();

			String filePath = String.format("/datalake/%s/%s/%s-%s-%s", topic, day, time, hostName, thread);
			Path path = new Path(filePath);
			log.debug("writing {} to HDFS {}", bufferList.size(), filePath);

			// Create a new file and write data to it.
			FSDataOutputStream out = fileSystem.create(path, true, config.getHdfsBufferSize());

			bufferList.stream().forEach(message -> {
				try {
					out.writeUTF(message);
					out.write('\n');
				} catch (IOException e) {
					log.error("error writing to HDFS. {}", e.getMessage());
				}
			});

			out.close();
			log.debug("Done writing {} to HDFS {}", bufferList.size(), filePath);
		}
	}

	public HdfsService(Db db) {
		hdfs = db;
	}

	@PostConstruct
	@Override
	public void init() {
		// Initialize HDFS Connection 
		try {
			//Get configuration of Hadoop system
			Configuration hdfsConfig = new Configuration();

			int port = hdfs.getPort() == null ? 8020 : hdfs.getPort();

			String hdfsuri = String.format("hdfs://%s:%s", hdfs.getHost(), port);
			hdfsConfig.set("fs.defaultFS", hdfsuri);
			System.setProperty("HADOOP_USER_NAME", hdfs.getLogin());

			log.info("Connecting to -- {} as {}", hdfsuri, hdfs.getLogin());

			fileSystem = FileSystem.get(hdfsConfig);

			//disable Hadoop Shutdown Hook, we need the HDFS connection to flush data
			ShutdownHookManager hadoopShutdownHookManager = ShutdownHookManager.get();
			hadoopShutdownHookManager.clearShutdownHooks();

		} catch (Exception ex) {
			log.error("error connection to HDFS.", ex);
		}
	}

	@PreDestroy
	public void cleanUp() {
		config.getShutdownLock().readLock().lock();

		try {
			log.info("fileSystem.close() at cleanUp.");
			flush();
			fileSystem.close();
		} catch (IOException e) {
			log.error("fileSystem.close() at cleanUp.", e);
		} finally {
			config.getShutdownLock().readLock().unlock();
		}
	}

	public void flush() {
		log.info("Force flush ALL data, regardless of stall");
		bufferLocal.get().forEach((topic, buffer) -> buffer.flush(topic));
	}

	//if no new data comes in for a topic for a while, need to flush its buffer
	public void flushStall() {
		log.debug("Flush stall data");
		bufferLocal.get().forEach((topic, buffer) -> buffer.flushStall(topic));
	}

	/*
		//used if raw data should be saved
		public void saveMessages(EffectiveTopic topic, List<Pair<Long, String>> messages) {
			String topicStr = topic.getName();
	
			Map<String, Buffer> bufferMap = bufferLocal.get();
			final Buffer buffer = bufferMap.computeIfAbsent(topicStr, k -> new Buffer());
	
			buffer.addData(messages);
	
			if (!config.isAsync() || buffer.getData().size() >= config.getHdfsBatchSize()) {
				buffer.flush(topicStr);
			} else {
				log.debug("buffer size too small to flush {}: bufferData.size() {} < config.getHdfsBatchSize() {}", topicStr, buffer.getData().size(), config.getHdfsBatchSize());
			}
		}
	*/
	@Override
	public void saveJsons(EffectiveTopic topic, List<JSONObject> jsons) {
		String topicStr = topic.getName();

		Map<String, Buffer> bufferMap = bufferLocal.get();
		final Buffer buffer = bufferMap.computeIfAbsent(topicStr, k -> new Buffer());

		buffer.addData2(jsons);

		if (!config.isAsync() || buffer.getData().size() >= config.getHdfsBatchSize()) {
			buffer.flush(topicStr);
		} else {
			log.debug("buffer size too small to flush {}: bufferData.size() {} < config.getHdfsBatchSize() {}", topicStr, buffer.getData().size(), config.getHdfsBatchSize());
		}

	}

}
