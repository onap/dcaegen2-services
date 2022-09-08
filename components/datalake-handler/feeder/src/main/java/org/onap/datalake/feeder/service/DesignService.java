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
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.*;
import org.onap.datalake.feeder.domain.Design;
import org.onap.datalake.feeder.dto.DesignConfig;
import org.onap.datalake.feeder.enumeration.DesignTypeEnum;
import org.onap.datalake.feeder.repository.DbRepository;
import org.onap.datalake.feeder.repository.DesignTypeRepository;
import org.onap.datalake.feeder.repository.DesignRepository;
import org.onap.datalake.feeder.repository.TopicNameRepository;
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
public class DesignService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private static final String ELASTIC_SEARCH = "Elasticsearch";
	private static final String KIBANA = "Kibana";

	@Autowired
	private DesignRepository designRepository;

	@Autowired
	private TopicNameRepository topicNameRepository;

	@Autowired
	private DesignTypeRepository designTypeRepository;

	@Autowired
	private ApplicationConfiguration applicationConfiguration;

	@Autowired
	private DbRepository dbRepository;

	public Design fillDesignConfiguration(DesignConfig designConfig) {
		Design design = new Design();
		fillDesign(designConfig, design);
		return design;
	}

	public void fillDesignConfiguration(DesignConfig designConfig, Design design) {
		fillDesign(designConfig, design);
	}

	private void fillDesign(DesignConfig designConfig, Design design) throws IllegalArgumentException {
		design.setId(designConfig.getId());
		design.setBody(designConfig.getBody());
		design.setName(designConfig.getName());
		design.setNote(designConfig.getNote());
		design.setSubmitted(designConfig.getSubmitted());

		if (designConfig.getTopicName() == null)
			throw new IllegalArgumentException("Can not find topicName in tpoic_name, topic name: " + designConfig.getTopicName());
		Optional<TopicName> topicName = topicNameRepository.findById(designConfig.getTopicName());
		if (!topicName.isPresent())
			throw new IllegalArgumentException("topicName is null " + designConfig.getTopicName());
		design.setTopicName(topicName.get());

		if (designConfig.getDesignType() == null)
			throw new IllegalArgumentException("Can not find designType in design_type, designType id " + designConfig.getDesignType());
		Optional<DesignType> designType = designTypeRepository.findById(designConfig.getDesignType());
		if (!designType.isPresent())
			throw new IllegalArgumentException("designType is null");
		design.setDesignType(designType.get());

		Set<Db> dbs = new HashSet<>();
		if (designConfig.getDbs() != null) {
			for (Integer item : designConfig.getDbs()) {
				Optional<Db> db = dbRepository.findById(item);
				if (db.isPresent()) {
					dbs.add(db.get());
				}
			}
			if (!dbs.isEmpty())
				design.setDbs(dbs);
			else {
				design.getDbs().clear();
				design.setDbs(dbs);
			}
		} else {
			design.setDbs(dbs);
		}
	}

	public Design getDesign(Integer id) {

		Optional<Design> ret = designRepository.findById(id);
		return ret.isPresent() ? ret.get() : null;
	}

	public List<DesignConfig> queryAllDesign() {

		List<Design> designList = null;
		List<DesignConfig> designConfigList = new ArrayList<>();
		designList = (List<Design>) designRepository.findAll();
		if (!designList.isEmpty()) {
			log.info("DesignList is not null");
			for (Design design : designList) {
				designConfigList.add(design.getDesignConfig());
			}
		}
		return designConfigList;
	}

	public Map<Integer, Boolean> deploy(Design design) {
		Map<Integer, Boolean> resultMap = null;
		DesignType designType = design.getDesignType();
		DesignTypeEnum designTypeEnum = DesignTypeEnum.valueOf(designType.getId());

		switch (designTypeEnum) {
		case KIBANA_DB:
			log.info("Deploy kibana dashboard");
			resultMap = deployKibanaDashboardImport(design);
			deploySave(resultMap, design);
			break;
		case ES_MAPPING:
			log.info("Deploy elasticsearch mapping template");
			resultMap = postEsMappingTemplate(design, design.getTopicName().getId().toLowerCase());
			deploySave(resultMap, design);
			break;
		default:
			log.error("Not implemented {}", designTypeEnum);
			break;
		}
		log.info("Response resultMap: " + resultMap);
		return resultMap;
	}

	private Map<Integer, Boolean> deployKibanaDashboardImport(Design design) {
		String POST_FLAG = "KibanaDashboardImport";
		String URL_FlAG = KIBANA;

		String requestBody = design.getBody();
		Set<Db> dbs =  design.getDbs();
		Map<Integer, Boolean> deployKibanaMap = new HashMap<>();

		if (!dbs.isEmpty()) {
			Map<Integer, String> map = urlMap(dbs, URL_FlAG);
			log.info("Deploy kibana dashboard url map: " + map);
			if (!map.isEmpty()) {
				for (Map.Entry<Integer, String> entry : map.entrySet()) {
					deployKibanaMap.put(entry.getKey(), HttpClientUtil.sendHttpClientPost(entry.getValue(), requestBody, POST_FLAG, URL_FlAG));
				}
			}
			return deployKibanaMap;
		} else {
			return deployKibanaMap;
		}
	}

	/**
	 * successed resp: { "acknowledged": true }
	 * 
	 * @param design
	 * @param templateName
	 * @return flag
	 */
	private Map<Integer, Boolean> postEsMappingTemplate(Design design, String templateName) {
		String URL_FlAG = ELASTIC_SEARCH;
		String POST_FLAG = "ElasticsearchMappingTemplate";

		String requestBody = design.getBody();
		Set<Db> dbs = design.getDbs();
		Map<Integer, Boolean> deployEsMap = new HashMap<>();

		if (!dbs.isEmpty()) {
			Map<Integer, String> map = urlMap(dbs, URL_FlAG);
			log.info("Deploy elasticsearch url map: " + map);
			if (!map.isEmpty()) {
				for (Map.Entry<Integer, String> entry : map.entrySet()) {
					deployEsMap.put(entry.getKey(), HttpClientUtil.sendHttpClientPost(entry.getValue() + templateName, requestBody, POST_FLAG, URL_FlAG));
				}
			}
			return deployEsMap;
		} else {
			return deployEsMap;
		}
	}

	private Map<Integer, String> urlMap (Set<Db> dbs, String flag) {
		Map<Integer, String> map = new HashMap<>();
		for (Db item : dbs) {
			if (item.isEnabled()) {
				map.put(item.getId(), httpRequestUrl(item.getHost(), item.getPort(), flag));
			}
		}
		return map;
	}

	private String httpRequestUrl(String host, Integer port, String urlFlag) {
		String url = "";
		switch (urlFlag) {
			case KIBANA:
				if (port == null) {
					port = applicationConfiguration.getKibanaPort();
				}
				url = "http://" + host + ":" + port + applicationConfiguration.getKibanaDashboardImportApi();
				log.info("Kibana url: " + url);
				break;
			case ELASTIC_SEARCH:
				if (port == null) {
					port = applicationConfiguration.getEsPort();
				}
				url = "http://" + host + ":" + port + applicationConfiguration.getEsTemplateMappingApi();
				log.info("Elasticsearch url: " + url);
				break;
			default:
				break;
		}
		return url;
	}

	private void deploySave(Map<Integer, Boolean> map, Design design) {
		if (!map.isEmpty()) {
			for (Map.Entry<Integer, Boolean> entry : map.entrySet()) {
				if (entry.getValue()) {
					design.setSubmitted(true);
					designRepository.save(design);
					log.info("Status was modified");
				}
			}
		}
	}

}
