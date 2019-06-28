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

package org.onap.datalake.feeder.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.Db;
import org.onap.datalake.feeder.domain.DbType;
import org.onap.datalake.feeder.domain.DesignType;
import org.onap.datalake.feeder.domain.Portal;
import org.onap.datalake.feeder.domain.PortalDesign;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.domain.TopicName;
import org.onap.datalake.feeder.dto.PortalDesignConfig;
import org.onap.datalake.feeder.enumeration.DbTypeEnum;
import org.onap.datalake.feeder.enumeration.DesignTypeEnum;
import org.onap.datalake.feeder.repository.DesignTypeRepository;
import org.onap.datalake.feeder.repository.PortalDesignRepository;
import org.onap.datalake.feeder.repository.TopicNameRepository;
import org.onap.datalake.feeder.service.db.CouchbaseService;
import org.onap.datalake.feeder.service.db.DbStoreService;
import org.onap.datalake.feeder.service.db.ElasticsearchService;
import org.onap.datalake.feeder.service.db.HdfsService;
import org.onap.datalake.feeder.service.db.MongodbService;
import org.onap.datalake.feeder.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for portalDesigns
 *
 * @author guochunmeng
 */

@Service
public class PortalDesignService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	static String POST_FLAG;

	@Autowired
	private PortalDesignRepository portalDesignRepository;

	@Autowired
	private TopicNameRepository topicNameRepository;

	@Autowired
	private DesignTypeRepository designTypeRepository;

	@Autowired
	private ApplicationConfiguration applicationConfiguration;

	public PortalDesign fillPortalDesignConfiguration(PortalDesignConfig portalDesignConfig) throws Exception {
		PortalDesign portalDesign = new PortalDesign();
		fillPortalDesign(portalDesignConfig, portalDesign);
		return portalDesign;
	}

	public void fillPortalDesignConfiguration(PortalDesignConfig portalDesignConfig, PortalDesign portalDesign) throws Exception {
		fillPortalDesign(portalDesignConfig, portalDesign);
	}

	private void fillPortalDesign(PortalDesignConfig portalDesignConfig, PortalDesign portalDesign) throws IllegalArgumentException {

		portalDesign.setId(portalDesignConfig.getId());
		portalDesign.setBody(portalDesignConfig.getBody());
		portalDesign.setName(portalDesignConfig.getName());
		portalDesign.setNote(portalDesignConfig.getNote());
		portalDesign.setSubmitted(portalDesignConfig.getSubmitted());

		if (portalDesignConfig.getTopic() != null) {
			Optional<TopicName> topicName = topicNameRepository.findById(portalDesignConfig.getTopic());
			if (topicName.isPresent()) {
				portalDesign.setTopicName(topicName.get());
			} else {
				throw new IllegalArgumentException("topic is null " + portalDesignConfig.getTopic());
			}
		} else {
			throw new IllegalArgumentException("Can not find topic in DB, topic name: " + portalDesignConfig.getTopic());
		}

		if (portalDesignConfig.getDesignType() != null) {
			DesignType designType = designTypeRepository.findById(portalDesignConfig.getDesignType()).get();
			if (designType == null)
				throw new IllegalArgumentException("designType is null");
			portalDesign.setDesignType(designType);
		} else {
			throw new IllegalArgumentException("Can not find designType in Design_type, designType name " + portalDesignConfig.getDesignType());
		}

	}

	public PortalDesign getPortalDesign(Integer id) {

		Optional<PortalDesign> ret = portalDesignRepository.findById(id);
		return ret.isPresent() ? ret.get() : null;
	}

	public List<PortalDesignConfig> queryAllPortalDesign() {

		List<PortalDesign> portalDesignList = null;
		List<PortalDesignConfig> portalDesignConfigList = new ArrayList<>();
		portalDesignList = (List<PortalDesign>) portalDesignRepository.findAll();
		if (portalDesignList != null && portalDesignList.size() > 0) {
			log.info("PortalDesignList is not null");
			for (PortalDesign portalDesign : portalDesignList) {
				portalDesignConfigList.add(portalDesign.getPortalDesignConfig());
			}
		}
		return portalDesignConfigList;
	}

	public boolean deploy(PortalDesign portalDesign) {
		DesignType designType = portalDesign.getDesignType();
		DesignTypeEnum designTypeEnum = DesignTypeEnum.valueOf(designType.getId());

		switch (designTypeEnum) {
		case KIBANA_DB:
			return deployKibanaImport(portalDesign);
		case ES_MAPPING:
			return postEsMappingTemplate(portalDesign, portalDesign.getTopicName().getId().toLowerCase());
		default:
			log.error("Not implemented {}", designTypeEnum);
			return false;
		}
	}

	private boolean deployKibanaImport(PortalDesign portalDesign) throws RuntimeException {
		POST_FLAG = "KibanaDashboardImport";
		String requestBody = portalDesign.getBody();
		Portal portal = portalDesign.getDesignType().getPortal();
		String portalHost = portal.getHost();
		Integer portalPort = portal.getPort();
		String url = "";

		if (portalHost == null || portalPort == null) {
			String dbHost = portal.getDb().getHost();
			Integer dbPort = portal.getDb().getPort();
			url = kibanaImportUrl(dbHost, dbPort);
		} else {
			url = kibanaImportUrl(portalHost, portalPort);
		}
		return HttpClientUtil.sendPostHttpClient(url, requestBody, POST_FLAG);

	}

	private String kibanaImportUrl(String host, Integer port) {
		if (port == null) {
			port = applicationConfiguration.getKibanaPort();
		}
		return "http://" + host + ":" + port + applicationConfiguration.getKibanaDashboardImportApi();
	}

	/**
	 * successed resp: { "acknowledged": true }
	 * 
	 * @param portalDesign
	 * @param templateName
	 * @return flag
	 */
	public boolean postEsMappingTemplate(PortalDesign portalDesign, String templateName) throws RuntimeException {
		POST_FLAG = "ElasticsearchMappingTemplate";
		String requestBody = portalDesign.getBody();

		//FIXME
		Set<Db> dbs = portalDesign.getDbs();
		//submit to each ES in dbs

		//return HttpClientUtil.sendPostHttpClient("http://"+dbService.getElasticsearch().getHost()+":9200/_template/"+templateName, requestBody, POST_FLAG);
		return false;
	}

}
