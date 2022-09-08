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
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * Domain class representing unique topic names
 * 
 * @author Guobiao Mo
 *
 */
@Setter
@Getter
@Entity
@Table(name = "topic_name")
public class TopicName  {
	@Id	
	@Column(name = "`id`")
	private String id;//topic name 


	@OneToMany(fetch = FetchType.LAZY, mappedBy = "topicName")
	protected Set<Design> designs;
	

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "topicName")
	protected Set<Topic> topics;
	
	public TopicName() {
	}

	public TopicName(String name) {
		id = name;
	}

	@Override
	public String toString() {
		return "TopicName "+ id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (this.getClass() != obj.getClass())
			return false;

		return id.equals(((TopicName) obj).getId());
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
