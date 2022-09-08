/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2019-2020 QCT
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
import java.util.Map;

/**
 * JSON request body for Topic manipulation.
 *
 * @author Kate Hsuan
 *
 */

@Getter
@Setter

public class TopicConfig {

	private Integer id;
	private String name;
	private String login;
	private String password;
	private List<Integer> sinkdbs;
	private List<Integer> enabledSinkdbs;//only include enabled db
	private boolean enabled;
	private boolean saveRaw;
	private String dataFormat;
	private int ttl;
	private boolean correlateClearedMessage;
	private String messageIdPath;
	private String aggregateArrayPath;
	private String flattenArrayPath;
	private List<Integer> kafkas;
	private Map<String,Integer> countsDb;
	private int countsKafka;

	@Override
	public String toString() {
		return String.format("TopicConfig %s(enabled=%s, enabledSinkdbs=%s)", name, enabled, enabledSinkdbs);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (this.getClass() != obj.getClass())
			return false;

		return id==((TopicConfig) obj).getId();
	}

	@Override
	public int hashCode() {
		return id;
	}

}
