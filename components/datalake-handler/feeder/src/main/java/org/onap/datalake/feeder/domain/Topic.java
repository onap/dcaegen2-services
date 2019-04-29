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
import javax.persistence.Table;

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
	@Column(name="`name`")
	private String name;//topic name 


		//for protected Kafka topics
	@Column(name = "`login`")
	private String login;

	@Column(name = "`pass`")
	private String pass;

	//@ManyToMany(mappedBy = "topics", cascade=CascadeType.ALL)
	@JsonBackReference
	//@JsonManagedReference
	@ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(	name 				= "map_db_topic",
    			joinColumns 		= {  @JoinColumn(name="topic_name")  },
    			inverseJoinColumns 	= {  @JoinColumn(name="db_name")  }
    )
	protected Set<Db> dbs;

	/**
	 * indicate if we should monitor this topic
	 */
	@Column(name="`enabled`")
	private Boolean enabled;

	/**
	 * save raw message text
	 */
	@Column(name = "`save_raw`")
	private Boolean saveRaw;

	/**
	 * need to explicitly tell feeder the data format of the message.
	 * support JSON, XML, YAML, TEXT
	 */
	@Column(name="`data_format`")
	private String dataFormat;

	/**
	 * TTL in day
	 */
	private Integer ttl;

	//if this flag is true, need to correlate alarm cleared message to previous alarm 
	@Column(name = "`correlate_cleared_message`")
	private Boolean correlateClearedMessage;

	//paths to the values in the JSON that are used to composite DB id, comma separated, example: "/event-header/id,/event-header/entity-type,/entity/product-name"
	@Column(name = "`message_id_path`")
	private String messageIdPath;

	public Topic() {
	}

	public Topic(String name) {
		this.name = name;
	}

	public Topic clone() {	//TODO will use TopicConfig
		Topic ret = new Topic();
		ret.setCorrelateClearedMessage(correlateClearedMessage);
		ret.setDataFormat(dataFormat);
		ret.setDbs(dbs);
		ret.setEnabled(enabled);
		ret.setLogin(login);
		ret.setMessageIdPath(messageIdPath);
		ret.setName(name);
		ret.setPass(pass);
		ret.setSaveRaw(saveRaw);
		ret.setTtl(ttl);
		
		return ret;
	}
	
	public boolean isDefault() {
		return "_DL_DEFAULT_".equals(name);
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
		}  else {
			return 3650;//default to 10 years for safe
		}
	}

	public DataFormat getDataFormat() {
		if (dataFormat != null) {
			return DataFormat.fromString(dataFormat);
		}  else {
			return null;
		}
	}

	private boolean is(Boolean b) {
		return is(b, false);
	}

	private boolean is(Boolean b, boolean defaultValue) {
		if (b != null) {
			return b;
		}  else {
			return defaultValue;
		}
	}

	public boolean isSaveRaw() {
		return is(saveRaw);
	}

	public boolean supportElasticsearch() {
		return containDb("Elasticsearch");//TODO string hard codes
	}

	public boolean supportCouchbase() {
		return containDb("Couchbase");
	}

	public boolean supportDruid() {
		return containDb("Druid");
	}

	public boolean supportMongoDB() {
		return containDb("MongoDB");
	}

	private boolean containDb(String dbName) {
		Db db = new Db(dbName);

		if (dbs != null && dbs.contains(db)) {
			return true;
		} else {
			return false;
		}
	}

	//extract DB id from JSON attributes, support multiple attributes
	public String getMessageId(JSONObject json) {
		String id = null;

		if (StringUtils.isNotBlank(messageIdPath)) {
			String[] paths = messageIdPath.split(",");

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < paths.length; i++) {
				if (i > 0) {
					sb.append('^');
				}
				sb.append(json.query(paths[i]).toString());
			}
			id = sb.toString();
		}

		return id;
	}

	public TopicConfig getTopicConfig() {
		TopicConfig tConfig = new TopicConfig();
		
		tConfig.setName(getName());
		tConfig.setEnable(getEnabled());
		if(getDataFormat() != null)
			tConfig.setDataFormat(getDataFormat().toString());
		tConfig.setSaveRaw(getSaveRaw());
		tConfig.setCorrelatedClearredMessage((getCorrelateClearedMessage() == null) ? getCorrelateClearedMessage() : false);
		tConfig.setMessageIdPath(getMessageIdPath());
		tConfig.setTtl(getTtl());
		Set<Db> topicDb = getDbs();
		List<String> dbList = new ArrayList<>();
		for(Db item: topicDb)
		{
			dbList.add(item.getName());
		}
		tConfig.setSinkdbs(dbList);
		
		return tConfig;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (this.getClass() != obj.getClass())
			return false;

		return name.equals(((Topic) obj).getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

}
