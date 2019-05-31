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
import org.onap.datalake.feeder.domain.PortalDesign;
import org.onap.datalake.feeder.domain.Topic;
import org.onap.datalake.feeder.dto.PortalDesignConfig;
import org.onap.datalake.feeder.repository.DesignTypeRepository;
import org.onap.datalake.feeder.repository.PortalDesignRepository;
import org.onap.datalake.feeder.repository.TopicRepository;
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
	private TopicRepository topicRepository;

	@Autowired
	private DesignTypeRepository designTypeRepository;

	@Autowired
	private ApplicationConfiguration applicationConfiguration;

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

	private void fillPortalDesign(PortalDesignConfig portalDesignConfig, PortalDesign portalDesign) throws Exception {

		portalDesign.setId(portalDesignConfig.getId());

		portalDesign.setBody(portalDesignConfig.getBody());

		portalDesign.setName(portalDesignConfig.getName());

		portalDesign.setNote(portalDesignConfig.getNote());

		portalDesign.setSubmitted(portalDesignConfig.getSubmitted());

		if (portalDesignConfig.getTopic() != null) {
			Topic topic = topicRepository.findById(portalDesignConfig.getTopic()).get();
			if (topic != null) {
				portalDesign.setTopic(topic);
			}
		}else {
			throw new Exception();
		}

		if (portalDesignConfig.getDesignType() != null) {
			DesignType designType = designTypeRepository.findById(portalDesignConfig.getDesignType()).get();
			if (designType != null) {
				portalDesign.setDesignType(designType);
			}
		}else {
			throw new Exception();
		}

	}

	
	public PortalDesign getPortalDesign(Integer id) {
		
		Optional<PortalDesign> ret = portalDesignRepository.findById(id);
		return ret.isPresent() ? ret.get() : null;
	}


	public String kibanaImportUrl(String host, Integer port){
		return "http://"+host+":"+port+applicationConfiguration.getKibanaDashboardImportApi();
	}


	/**
	 * respFailedDemo:{
	 *     "objects": [
	 *         {
	 *             "id": "37cc8650-b882-11e8-a6d9-e546fe2bba5f",
	 *             "type": "visualization",
	 *             "error": {
	 *                 "statusCode": 409,
	 *                 "message": "version conflict, document already exists"
	 *             }
	 *         },
	 *         {
	 *             "id": "722b74f0-b882-11e8-a6d9-e546fe2bba5f",
	 *             "type": "dashboard",
	 *             "error": {
	 *                 "statusCode": 409,
	 *                 "message": "version conflict, document already exists"
	 *             }
	 *         }
	 *     ]
	 * }
	 * @param kibanaResponse
	 * @return flag
	 */
	public Boolean isKibanaResponse(String kibanaResponse) {
		Boolean flag = false;
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
}
