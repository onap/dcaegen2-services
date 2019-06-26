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
package org.onap.datalake.feeder.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.onap.datalake.feeder.dto.TopicConfig;

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
	protected Set<Db> dbs;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "map_kafka_topic", joinColumns = { @JoinColumn(name = "topic_id") }, inverseJoinColumns = { @JoinColumn(name = "kafka_id") })
	protected Set<Kafka> kafkas;

	/**
	 * indicate if we should monitor this topic
	 */
	@Column(name = "`enabled`")
	private Boolean enabled;

	/**
	 * save raw message text
	 */
	@Column(name = "`save_raw`")
	private Boolean saveRaw;

	/**
	 * need to explicitly tell feeder the data format of the message. support JSON,
	 * XML, YAML, TEXT
	 */
	@Column(name = "`data_format`")
	private String dataFormat;

	/**
	 * TTL in day
	 */
	@Column(name = "`ttl_day`")
	private Integer ttl;

	//if this flag is true, need to correlate alarm cleared message to previous alarm 
	@Column(name = "`correlate_cleared_message`")
	private Boolean correlateClearedMessage;

	//paths to the values in the JSON that are used to composite DB id, comma separated, example: "/event-header/id,/event-header/entity-type,/entity/product-name"
	@Column(name = "`message_id_path`")
	private String messageIdPath;

	//paths to the array that need aggregation, comma separated, example: "/event/measurementsForVfScalingFields/diskUsageArray,/event/measurementsForVfScalingFields/cpuUsageArray,/event/measurementsForVfScalingFields/vNicPerformanceArray"
	@Column(name = "`aggregate_array_path`") 
	private String aggregateArrayPath;

	//paths to the element in array that need flatten, this element is used as label, comma separated, 
	//example: "/event/measurementsForVfScalingFields/astriMeasurement/astriDPMeasurementArray/astriInterface,..."
	@Column(name = "`flatten_array_path`") 
	private String flattenArrayPath;
	
	public Topic() {
	}

	public Topic(String name) {//TODO
		//this.name = name;
	}

	public String getName() {
		return topicName.getId();
	}
	
	public boolean isEnabled() {
		return is(enabled);
	}

	public boolean isCorrelateClearedMessage() {
		return is(correlateClearedMessage);
	}

	public int getTtl() {
		if (ttl != null) {
			return ttl;
		} else {
			return 3650;//default to 10 years for safe
		}
	}

	private boolean is(Boolean b) {
		return is(b, false);
	}

	private boolean is(Boolean b, boolean defaultValue) {
		if (b != null) {
			return b;
		} else {
			return defaultValue;
		}
	}

	public boolean isSaveRaw() {
		return is(saveRaw);
	}

	public TopicConfig getTopicConfig() {
		TopicConfig tConfig = new TopicConfig();

		//tConfig.setName(getName());
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
		List<String> dbList = new ArrayList<>();
		List<String> enabledDbList = new ArrayList<>();
		if (topicDb != null) {
			for (Db item : topicDb) {
				dbList.add(item.getName());
				if(item.isEnabled()) {
					enabledDbList.add(item.getName());
				}
			}
		}
		tConfig.setSinkdbs(dbList);
		tConfig.setEnabledSinkdbs(enabledDbList);

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
