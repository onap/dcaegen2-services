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

import java.util.Optional;

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

	public PortalDesign fillPortalDesignConfiguration(PortalDesignConfig portalDesignConfig)
	{
		PortalDesign portalDesign = new PortalDesign();
		fillPortalDesign(portalDesignConfig, portalDesign);
		return portalDesign;
	}
	public void fillPortalDesignConfiguration(PortalDesignConfig portalDesignConfig, PortalDesign portalDesign)
	{
		fillPortalDesign(portalDesignConfig, portalDesign);
	}

	private void fillPortalDesign(PortalDesignConfig portalDesignConfig, PortalDesign portalDesign) {

		Topic topic = new Topic();

		DesignType designType = new DesignType();

		portalDesign.setId(portalDesignConfig.getId());

		portalDesign.setBody(portalDesignConfig.getBody());

		portalDesign.setName(portalDesignConfig.getName());

		portalDesign.setNote(portalDesignConfig.getNote());

		portalDesign.setSubmitted(portalDesignConfig.getSubmitted());

		if (portalDesignConfig.getTopic() != null) {
			topic = topicRepository.findById(portalDesignConfig.getTopic()).get();
			if (topic != null) {
				portalDesign.setTopic(topic);
			}
		}else {
			portalDesign.setTopic(topic);
		}

		if (portalDesignConfig.getDesignType() != null) {
			designType = designTypeRepository.findById(portalDesignConfig.getDesignType()).get();
			if (designType != null) {
				portalDesign.setDesignType(designType);
			}
		}else {
			portalDesign.setDesignType(designType);
		}

	}
	
	public PortalDesign getPortalDesign(Integer id) {
		
		Optional<PortalDesign> ret = portalDesignRepository.findById(id);
		return ret.isPresent() ? ret.get() : null;
	}
}
