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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;


/**
 * Domain class representing bid data storage
 * 
 * @author Guobiao Mo
 *
 */
@Setter
@Getter
@Entity
@Table(name = "db")
public class Db {
	@Id
	@Column(name="`name`")
	private String name;

	@Column(name="`host`")
	private String host;

	@Column(name="`port`")
	private Integer port;

	@Column(name="`login`")
	private String login;

	@Column(name="`pass`")
	private String pass;

	@Column(name="`database`")
	private String database;

	@Column(name="`encrypt`")
	private Boolean encrypt;

	@Column(name="`property1`")
	private String property1;

	@Column(name="`property2`")
	private String property2;

	@Column(name="`property3`")
	private String property3;

	@JsonBackReference
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(	name 				= "map_db_topic",
			joinColumns 		= {  @JoinColumn(name="db_name")  },
			inverseJoinColumns 	= {  @JoinColumn(name="topic_name")  }
	)
	protected Set<Topic> topics;

	public Db() {
	}

	public Db(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (this.getClass() != obj.getClass())
			return false;

		return name.equals(((Db) obj).getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
