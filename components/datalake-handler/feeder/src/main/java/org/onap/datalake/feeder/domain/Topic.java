/*
* ============LICENSE_START=======================================================
* ONAP : DataLake
* ================================================================================
* Copyright 2019-2020 China Mobile
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
package org.onap.datalake.feeder.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.GenerationType;
import javax.persistence.GeneratedValue;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.onap.datalake.feeder.dto.TopicConfig;
import org.onap.datalake.feeder.enumeration.DataFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Getter;
import lombok.Setter;

/**
 * Domain class representing topic
 * 
 * @author Guobiao Mo
 *
 */
@Setter
@Getter
@Entity
@Table(name = "topic")
public class Topic {
	@Id
    @Column(name = "`id`")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_name_id", nullable = false)
	private TopicName topicName;//topic name 
	
	//for protected Kafka topics
	@Column(name = "`login`")
	private String login;

	@Column(name = "`pass`")
	private String pass;

	//@ManyToMany(mappedBy = "topics", cascade=CascadeType.ALL)
	@JsonBackReference
	//@JsonManagedReference
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "map_db_topic", joinColumns = { @JoinColumn(name = "topic_id") }, inverseJoinColumns = { @JoinColumn(name = "db_id") })
	protected Set<Db> dbs=new HashSet<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "map_kafka_topic", joinColumns = { @JoinColumn(name = "topic_id") }, inverseJoinColumns = { @JoinColumn(name = "kafka_id") })
	protected Set<Kafka> kafkas=new HashSet<>();

	/**
	 * indicate if we should monitor this topic
	 */
	@Column(name = "`enabled`", nullable = false)
	private boolean enabled;

	/**
	 * save raw message text
	 */
	@Column(name = "`save_raw`", nullable = false, columnDefinition = " bit(1) DEFAULT 0")
	private boolean saveRaw;

	/**
	 * need to explicitly tell feeder the data format of the message. support JSON,
	 * XML, YAML, TEXT
	 */
	@Column(name = "`data_format`")
	protected String dataFormat;

	/**
	 * TTL in day
	 */
	@Column(name = "`ttl_day`")
	private Integer ttl;

	//if this flag is true, need to correlate alarm cleared message to previous alarm 
	@Column(name = "`correlate_cleared_message`", nullable = false, columnDefinition = " bit(1) DEFAULT 0")
	private boolean correlateClearedMessage;

	//paths to the values in the JSON that are used to composite DB id, comma separated, example: "/event-header/id,/event-header/entity-type,/entity/product-name"
	@Column(name = "`message_id_path`")
	protected String messageIdPath;

	//paths to the array that need aggregation, comma separated, example: "/event/measurementsForVfScalingFields/diskUsageArray,/event/measurementsForVfScalingFields/cpuUsageArray,/event/measurementsForVfScalingFields/vNicPerformanceArray"
	@Column(name = "`aggregate_array_path`")
	protected String aggregateArrayPath;

	//paths to the element in array that need flatten, this element is used as label, comma separated, 
	//example: "/event/measurementsForVfScalingFields/astriMeasurement/astriDPMeasurementArray/astriInterface,..."
	@Column(name = "`flatten_array_path`")
	protected String flattenArrayPath;
	
	public String getName() {
		return topicName.getId();
	}
	
	public int getTtl() {
		if (ttl != null) {
			return ttl;
		} else {
			return 3650;//default to 10 years for safe
		}
	}

	public DataFormat getDataFormat2() {
		if (dataFormat != null) {
			return DataFormat.fromString(dataFormat);
		} else {
			return null;
		}
	}

	public String[] getAggregateArrayPath2() {
		String[] ret = null;

		if (StringUtils.isNotBlank(aggregateArrayPath)) {
			ret = aggregateArrayPath.split(",");
		}

		return ret;
	}

	public String[] getFlattenArrayPath2() {
		String[] ret = null;

		if (StringUtils.isNotBlank(flattenArrayPath)) {
			ret = flattenArrayPath.split(",");
		}

		return ret;
	}

	//extract DB id from JSON attributes, support multiple attributes
	public String getMessageId(JSONObject json) {
		String ret = null;

		if (StringUtils.isNotBlank(messageIdPath)) {
			String[] paths = messageIdPath.split(",");

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < paths.length; i++) {
				if (i > 0) {
					sb.append('^');
				}
				sb.append(json.query(paths[i]).toString());
			}
			ret = sb.toString();
		}

		return ret;
	}

	public TopicConfig getTopicConfig() {
		TopicConfig tConfig = new TopicConfig();

		tConfig.setId(getId());
		tConfig.setName(getName());
		tConfig.setLogin(getLogin());
		tConfig.setEnabled(isEnabled());
		tConfig.setDataFormat(dataFormat);
		tConfig.setSaveRaw(isSaveRaw());
		tConfig.setCorrelateClearedMessage(isCorrelateClearedMessage());
		tConfig.setMessageIdPath(getMessageIdPath());
		tConfig.setAggregateArrayPath(getAggregateArrayPath());
		tConfig.setFlattenArrayPath(getFlattenArrayPath());
		tConfig.setTtl(getTtl());
		
		Set<Db> topicDb = getDbs();
		List<Integer> dbList = new ArrayList<>();
		List<Integer> enabledDbList = new ArrayList<>();
		List<String> enabledDbList2 = new ArrayList<>();
		if (topicDb != null) {
			for (Db item : topicDb) {
				dbList.add(item.getId());
				if(item.isEnabled()) {
					enabledDbList.add(item.getId());
					enabledDbList2.add(item.getDbType().getId());
				}
			}
		}
		tConfig.setSinkdbs(dbList);
		tConfig.setEnabledSinkdbs(enabledDbList);
		Map<String,Integer> map = new HashMap<>();
		for (String string : enabledDbList2) {
			if(map.containsKey(string)) {
				map.put(string, map.get(string).intValue()+1);
			}else {
				map.put(string, new Integer(1));
			}
		}
		tConfig.setCountsDb(map);

		Set<Kafka> topicKafka = getKafkas();
		List<Integer> kafkaList = new ArrayList<>();
		if (topicKafka != null) {
			for (Kafka kafka : topicKafka) {
				kafkaList.add(kafka.getId());
			}
		}
		tConfig.setKafkas(kafkaList);
		tConfig.setCountsKafka(kafkaList.size());
		return tConfig;
	}

	@Override
	public String toString() {
		return String.format("Topic %s (enabled=%s, dbs=%s, kafkas=%s)", topicName, enabled, dbs, kafkas);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (this.getClass() != obj.getClass())
			return false;

		return id.equals(((Topic) obj).getId());
	}

	@Override
	public int hashCode() {
		return id;
	}

}
