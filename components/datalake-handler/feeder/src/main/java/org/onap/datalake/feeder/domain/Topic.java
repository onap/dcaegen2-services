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

import java.util.function.Predicate;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.onap.datalake.feeder.enumeration.DataFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.couchbase.core.mapping.Document;

import lombok.Setter;
 
/**
 * Domain class representing topic table in Couchbase
 * 
 * @author Guobiao Mo
 *
 */
@Document
@Setter
public class Topic {
	@NotNull
	@Id
	private String id;//topic name 

	@Transient
	private Topic defaultTopic;

	//for protected Kafka topics
	private String login;
	private String pass;

	/**
	 *  indicate if we should monitor this topic
	 */
	private Boolean enabled;
	
	/**
	 * save raw message text
	 */
	private Boolean saveRaw;

	/**
	 * true: save it to Elasticsearch false: don't save null: use default
	 */
	private Boolean supportElasticsearch;
	private Boolean supportCouchbase;
	private Boolean supportDruid;

	/**
	 * need to explicitly tell feeder the data format of the message
	 * support JSON, XML, YAML, TEXT
	 */
	private DataFormat dataFormat;

	/**
	 * TTL in day
	 */
	private Integer ttl; 
	
	//if this flag is true, need to correlate alarm cleared message to previous alarm 
	private Boolean correlateClearedMessage;
	
	//the value in the JSON with this path will be used as DB id
	private String messageIdPath;

	public Topic() {
	}

	public Topic(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	public void setDefaultTopic(Topic defaultTopic) {
		this.defaultTopic = defaultTopic;
	}

	public boolean isEnabled() {
		return is(enabled, Topic::isEnabled);	
	}

	public boolean isCorrelateClearedMessage() {
		return is(correlateClearedMessage, Topic::isCorrelateClearedMessage);
	}
	
	public int getTtl() {
		if (ttl != null) {
			return ttl;
		} else if (defaultTopic != null) {
			return defaultTopic.getTtl();
		} else {
			return 3650;//default to 10 years for safe
		}		
	}
	
	public DataFormat getDataFormat() {
		if (dataFormat != null) {
			return dataFormat;
		} else if (defaultTopic != null) {
			return defaultTopic.getDataFormat();
		} else {
			return null;
		}
	}

	//if 'this' Topic does not have the setting, use default Topic's
	private boolean is(Boolean b, Predicate<Topic> pre) {
		return is(b, pre, false);
	}
	
	private boolean is(Boolean b, Predicate<Topic> pre, boolean defaultValue) {
		if (b != null) {
			return b;
		} else if (defaultTopic != null) {
			return pre.test(defaultTopic);
		} else {
			return defaultValue;
		}
	}

	public boolean isSaveRaw() {
		return is(saveRaw, Topic::isSaveRaw);
	}

	public boolean isSupportElasticsearch() {
		return is(supportElasticsearch, Topic::isSupportElasticsearch);
	}

	public boolean isSupportCouchbase() {
		return is(supportCouchbase, Topic::isSupportCouchbase);
	}

	public boolean isSupportDruid() {
		return is(supportDruid, Topic::isSupportDruid);
	}

	//extract DB id from a JSON attribute, TODO support multiple attributes
	public String getMessageId(JSONObject json) {
		String id = null;
		
		if(StringUtils.isNotBlank(messageIdPath)) {
			id = json.query(messageIdPath).toString();
		}
		
		return id;
	}
	
	@Override
	public String toString() {
		return id;
	}

	/**
	 * @return the messageIdPath
	 */
	public String getMessageIdPath() {
		return messageIdPath;
	}

	/**
	 * @param messageIdPath the messageIdPath to set
	 */
	public void setMessageIdPath(String messageIdPath) {
		this.messageIdPath = messageIdPath;
	}
}
