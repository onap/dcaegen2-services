/*
* ============LICENSE_START=======================================================
* ONAP : DCAE
* ================================================================================
* Copyright 2018 TechMahindra
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
package org.onap.datalake.feeder.enumeration;

/**
 * Design type
 * 
 * @author Guobiao Mo
 *
 */
public enum DesignTypeEnum { 
	KIBANA_DB("Kibana Dashboard"), KIBANA_SEARCH("Kibana Search"), KIBANA_VISUAL("Kibana Visualization"), 
	ES_MAPPING("Elasticsearch Field Mapping Template"), DRUID_KAFKA_SPEC("Druid Kafka Indexing Service Supervisor Spec");

	private final String name;

	DesignTypeEnum(String name) {
		this.name = name;
	}

}
