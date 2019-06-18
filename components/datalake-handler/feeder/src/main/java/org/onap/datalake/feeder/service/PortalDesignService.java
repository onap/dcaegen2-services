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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.onap.datalake.feeder.domain.DesignType;
import org.onap.datalake.feeder.domain.Portal;
import org.onap.datalake.feeder.domain.PortalDesign;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.dto.PortalDesignConfig;
import org.onap.datalake.feeder.repository.DesignTypeRepository;
import org.onap.datalake.feeder.repository.PortalDesignRepository;
import org.onap.datalake.feeder.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for portalDesigns
 *
 * @author guochunmeng
 */

@Service
public class PortalDesignService {
	
    @Autowired
    private PortalDesignRepository portalDesignRepository;

    @Autowired
	private TopicService topicService;

	@Autowired
	private DesignTypeRepository designTypeRepository;

	@Autowired
	private ApplicationConfiguration applicationConfiguration;

	@Autowired
	private ElasticsearchService elasticsearchService;

	public PortalDesign fillPortalDesignConfiguration(PortalDesignConfig portalDesignConfig) throws Exception
	{
		PortalDesign portalDesign = new PortalDesign();
		fillPortalDesign(portalDesignConfig, portalDesign);
		return portalDesign;
	}
	public void fillPortalDesignConfiguration(PortalDesignConfig portalDesignConfig, PortalDesign portalDesign) throws Exception
	{
		fillPortalDesign(portalDesignConfig, portalDesign);
	}

	private void fillPortalDesign(PortalDesignConfig portalDesignConfig, PortalDesign portalDesign) throws IllegalArgumentException {

		portalDesign.setId(portalDesignConfig.getId());

		portalDesign.setBody(portalDesignConfig.getBody());

		portalDesign.setName(portalDesignConfig.getName());

		portalDesign.setNote(portalDesignConfig.getNote());

		portalDesign.setSubmitted(portalDesignConfig.getSubmitted());

		if (portalDesignConfig.getTopic() != null) {
			Topic topic = topicService.getTopic(portalDesignConfig.getTopic());
			if (topic == null) throw new IllegalArgumentException("topic is null");
			portalDesign.setTopic(topic);
		}else {
			throw new IllegalArgumentException("Can not find topic in DB, topic name: "+portalDesignConfig.getTopic());
		}

		if (portalDesignConfig.getDesignType() != null) {
			DesignType designType = designTypeRepository.findById(portalDesignConfig.getDesignType()).get();
			if (designType == null) throw new IllegalArgumentException("designType is null");
			portalDesign.setDesignType(designType);
		}else {
			throw new IllegalArgumentException("Can not find designType in Design_type, designType name "+portalDesignConfig.getDesignType());
		}

	}

	
	public PortalDesign getPortalDesign(Integer id) {
		
		Optional<PortalDesign> ret = portalDesignRepository.findById(id);
		return ret.isPresent() ? ret.get() : null;
	}


	private String kibanaImportUrl(String host){
		return "http://"+host+":"+applicationConfiguration.getKibanaPort()+applicationConfiguration.getKibanaDashboardImportApi();
	}


	private boolean deployKibanaImport(PortalDesign portalDesign) {
		boolean flag = false;
		String requestBody = portalDesign.getBody();
		Portal portal = portalDesign.getDesignType().getPortal();
		String portalHost = portal.getHost();
		String url = "";

		if (portalHost == null) {
			String dbHost = portal.getDb().getHost();
			url = kibanaImportUrl(dbHost);
		} else {
			url = kibanaImportUrl(portalHost);
		}

		//Send httpclient to kibana
		String kibanaResponse = HttpClientUtil.sendPostToKibana(url, requestBody);
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<>();
		map = gson.fromJson(kibanaResponse, map.getClass());
		List objectsList = (List) map.get("objects");

		if (objectsList != null && objectsList.size() > 0) {
			Map<String, Object> map2 = new HashMap<>();
			for (int i = 0; i < objectsList.size(); i++){
				map2 = (Map<String, Object>)objectsList.get(i);
				for(String key : map2.keySet()){
					if ("error".equals(key)) {
						return true;
					}
				}
			}
		}
		return flag;
	}


	public boolean deployPortalDesign(PortalDesign portalDesign){
		boolean flag =false;
		if (portalDesign.getDesignType() != null && portalDesign.getDesignType().getName().startsWith("Kibana")) {
			flag = deployKibanaImport(portalDesign);
		} else if (portalDesign.getDesignType() != null && portalDesign.getDesignType().getName().startsWith("Elasticsearch")) {
			flag = elasticsearchService.setEsMappingTemplate(portalDesign, portalDesign.getTopic().getName().toLowerCase());
		} else {
			//TODO Druid import
			flag =true;
		}
		return flag;
	}

}
