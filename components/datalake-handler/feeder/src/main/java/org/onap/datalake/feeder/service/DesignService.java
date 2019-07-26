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

	private static String POST_FLAG;

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
			if (dbs.size() > 0)
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
			resultMap = deployKibanaImport(design);
			if (!resultMap.isEmpty()) {
				Iterator<Map.Entry<Integer, Boolean>> it = resultMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<Integer, Boolean> entry = it.next();
					if (entry.getValue()) {
						design.setSubmitted(true);
						designRepository.save(design);
					}
				}
			}
		case ES_MAPPING:
			//FIXME
			//return postEsMappingTemplate(design, design.getTopicName().getId().toLowerCase());
		default:
			log.error("Not implemented {}", designTypeEnum);
		}
		return resultMap;
	}

	private Map<Integer, Boolean> deployKibanaImport(Design design) {
		POST_FLAG = "KibanaDashboardImport";
		String requestBody = design.getBody();
		Set<Db> dbs =  design.getDbs();
		Map<Integer, Boolean> deployMap = new HashMap<>();

		if (!dbs.isEmpty()) {
			Map<Integer, String> map = new HashMap<>();
			for (Db item : dbs) {
				if (item.isEnabled()) {
					map.put(item.getId(), kibanaImportUrl(item.getHost(), item.getPort()));
				}
			}
			if (!map.isEmpty()) {
				Iterator<Map.Entry<Integer, String>> it = map.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<Integer, String> entry = it.next();
					deployMap.put(entry.getKey(), HttpClientUtil.sendPostKibana(entry.getValue(), requestBody, POST_FLAG));
				}
			}
			return deployMap;
		} else {
			return deployMap;
		}
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
	 * @param design
	 * @param templateName
	 * @return flag
	 */
	public boolean postEsMappingTemplate(Design design, String templateName) {
		POST_FLAG = "ElasticsearchMappingTemplate";
		String requestBody = design.getBody();

		//FIXME
		Set<Db> dbs = design.getDbs();
		//submit to each ES in dbs

		//return HttpClientUtil.sendPostHttpClient("http://"+dbService.getElasticsearch().getHost()+":9200/_template/"+templateName, requestBody, POST_FLAG);
		return false;
	}

}
