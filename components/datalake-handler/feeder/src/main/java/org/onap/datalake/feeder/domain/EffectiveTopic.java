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

/**
 * A warper of parent Topic
 * 
 * @author Guobiao Mo
 *
 */
 
public class EffectiveTopic {
	private Topic topic; //base Topic
	
	String name;

	public EffectiveTopic(Topic baseTopic) {
		topic = baseTopic;
	}

	public EffectiveTopic(Topic baseTopic, String name ) {
		topic = baseTopic;
		this.name = name;
	}
	
	public String getName() {
		return name==null?topic.getName():name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Topic getTopic() {
		return topic;
	}
 
	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	@Override
	public String toString() {
		return String.format("EffectiveTopic %s (base Topic=%s)", getName(), topic.toString());
	}
 
}
