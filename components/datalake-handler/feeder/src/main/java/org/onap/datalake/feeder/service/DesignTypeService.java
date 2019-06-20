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

import org.onap.datalake.feeder.domain.DesignType;
import org.onap.datalake.feeder.dto.DesignTypeConfig;
import org.onap.datalake.feeder.repository.DesignTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for designTypes
 *
 * @author guochunmeng
 */
@Service
public class DesignTypeService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	DesignTypeRepository designTypeRepository;

	public List<DesignTypeConfig> getDesignTypes(){
		
		List<DesignType> designTypeList = null;
		List<DesignTypeConfig> designTypeConfigList = new ArrayList<>();
		designTypeList = (List<DesignType>)designTypeRepository.findAll();
		if (designTypeList != null && designTypeList.size() > 0) {
			log.info("DesignTypeList is not null");
			for(DesignType designType : designTypeList) {
				designTypeConfigList.add(designType.getDesignTypeConfig());
			}
		}
		
		return designTypeConfigList;
	}

}
