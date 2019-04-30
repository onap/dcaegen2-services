/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2019 QCT
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

package org.onap.datalake.feeder.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.onap.datalake.feeder.enumeration.DataFormat;

/**
 * JSON request body for Topic manipulation.
 *
 * @author Kate Hsuan
 *
 */

@Getter
@Setter

public class TopicConfig {

	private String name;
	private String login;
	private String password;
	private List<String> sinkdbs;
	private boolean enabled;
	private boolean saveRaw;
	private String dataFormat;
	private int ttl;
	private boolean correlateClearedMessage;
	private String messageIdPath;

	public DataFormat getDataFormat2() {
		if (dataFormat != null) {
			return DataFormat.fromString(dataFormat);
		} else {
			return null;
		}
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
		return (sinkdbs != null && sinkdbs.contains(dbName));
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

		return name.equals(((TopicConfig) obj).getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

}
