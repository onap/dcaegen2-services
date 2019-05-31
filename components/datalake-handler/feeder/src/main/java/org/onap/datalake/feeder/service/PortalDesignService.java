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

import org.onap.datalake.feeder.domain.PortalDesign;
import org.onap.datalake.feeder.dto.PortalDesignConfig;
import org.onap.datalake.feeder.repository.PortalDesignRepository;
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
    
	public PortalDesign fillPortalDesignConfiguration(PortalDesignConfig portalDesignConfig) {
		
			PortalDesign portalDesignResult = new PortalDesign();

			portalDesignResult.setId(portalDesignConfig.getId());
			
			portalDesignResult.setBody(portalDesignConfig.getBody());
			
			portalDesignResult.setName(portalDesignConfig.getName());
			
			portalDesignResult.setNote(portalDesignConfig.getNote());
			
			portalDesignResult.setSubmitted(portalDesignConfig.getSubmitted());
			
			portalDesignResult.setTopic(portalDesignConfig.getTopic());
			
			portalDesignResult.setDesignType(portalDesignConfig.getDesignType());
			
		return portalDesignResult;
	}
	
	public PortalDesign getPortalDesign(Integer id) {
		
		Optional<PortalDesign> ret = portalDesignRepository.findById(id);
		return ret.isPresent() ? ret.get() : null;
	}
}
