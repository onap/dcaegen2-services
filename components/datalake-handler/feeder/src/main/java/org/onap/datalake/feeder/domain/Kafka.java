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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import org.onap.datalake.feeder.dto.KafkaConfig;


/**
 * Domain class representing Kafka cluster
 * 
 * @author Guobiao Mo
 *
 */
@Setter
@Getter
@Entity
@Table(name = "kafka")
public class Kafka {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private int id;
	
	@Column(name="`name`", nullable = false)
	private String name;

	@Column(name="`enabled`", nullable = false)
	private boolean	enabled;

	@Column(name="broker_list", nullable = false)
	private String brokerList;//message-router-kafka:9092,message-router-kafka2:9092

	@Column(name="`zk`", nullable = false)
	private String zooKeeper;//message-router-zookeeper:2181

	@Column(name="`group`", columnDefinition = "varchar(255) DEFAULT 'datalake'")
	private String group;

	@Column(name="`secure`", columnDefinition = " bit(1) DEFAULT 0")
	private boolean secure;
	
	@Column(name="`login`")
	private String login;

	@Column(name="`pass`")
	private String pass;

	@Column(name="`security_protocol`")
	private String securityProtocol;

	//by default, all topics started with '__' are excluded, here one can explicitly include them
	//example: '__consumer_offsets,__transaction_state'
	@Column(name="`included_topic`")
	private String includedTopic;
	
	@Column(name="`excluded_topic`", columnDefinition = "varchar(1023) default '__consumer_offsets,__transaction_state'")
	private String excludedTopic;

	@Column(name="`consumer_count`", columnDefinition = "integer default 3")
	private Integer consumerCount;
	
	//don't show this field in admin UI 
	@Column(name="`timeout_sec`", columnDefinition = "integer default 10")
	private Integer timeout;

	@JsonBackReference
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(	name 				= "map_kafka_topic",
			joinColumns 		= {  @JoinColumn(name="kafka_id")  },
			inverseJoinColumns 	= {  @JoinColumn(name="topic_id")  }
	)
	private Set<Topic> topics;

	@Override
	public String toString() {
		return String.format("Kafka %s (name=%s, enabled=%s)", id, name, enabled);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (this.getClass() != obj.getClass())
			return false;

		return id == ((Kafka) obj).getId();
	}

	@Override
	public int hashCode() {
		return id;
	}

	public KafkaConfig getKafkaConfig() {
		KafkaConfig kafkaConfig = new KafkaConfig();

		kafkaConfig.setId(getId());
		kafkaConfig.setBrokerList(getBrokerList());
		kafkaConfig.setConsumerCount(getConsumerCount());
		kafkaConfig.setEnabled(isEnabled());
		kafkaConfig.setExcludedTopic(getExcludedTopic());
		kafkaConfig.setGroup(getGroup());
		kafkaConfig.setIncludedTopic(getIncludedTopic());
		kafkaConfig.setLogin(getLogin());
		kafkaConfig.setName(getName());
		kafkaConfig.setPass(getPass());
		kafkaConfig.setSecure(isSecure());
		kafkaConfig.setSecurityProtocol(getSecurityProtocol());
		kafkaConfig.setTimeout(getTimeout());
		kafkaConfig.setZooKeeper(getZooKeeper());

		return kafkaConfig;
	}
}
