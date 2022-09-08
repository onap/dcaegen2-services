/*
* ============LICENSE_START=======================================================
* ONAP : DATALAKE
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

package org.onap.datalake.feeder.config;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Mapping from src/main/resources/application.properties to Java configuration
 * object
 * 
 * @author Guobiao Mo
 *
 */
@Getter
@Setter
@SpringBootConfiguration
@ConfigurationProperties
@EnableAutoConfiguration
public class ApplicationConfiguration {

	final ReentrantReadWriteLock shutdownLock = new ReentrantReadWriteLock();
	
	//App general
	private boolean async;
	private boolean enableSSL;

	private String timestampLabel;
	private String rawDataLabel;

	private String defaultTopicName;

	private long checkTopicInterval; //in millisecond

	private String elasticsearchType;

	//HDFS
	private int hdfsBufferSize;	
	private long hdfsFlushInterval;
	private int hdfsBatchSize;

	//Version
	private String datalakeVersion;
  
  	//Kibana
	private String kibanaDashboardImportApi;
	private Integer kibanaPort;

	//Elasticsearch
	private String esTemplateMappingApi;
	private Integer esPort;
}
