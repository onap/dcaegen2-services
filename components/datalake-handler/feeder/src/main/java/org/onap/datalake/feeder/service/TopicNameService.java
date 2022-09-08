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

package org.onap.datalake.feeder.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.onap.datalake.feeder.domain.TopicName;
import org.onap.datalake.feeder.repository.TopicNameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for TopicName
 * 
 * @author Guobiao Mo
 *
 */
@Service
public class TopicNameService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TopicNameRepository topicNameRepository;

	public void update(Collection<String> allTopicNames) {

		List<TopicName> all = allTopicNames.stream().map(s-> new TopicName(s)).collect(Collectors.toList());
		List<TopicName> allInDb = (List<TopicName>) topicNameRepository.findAll();
		
		Collection<TopicName> additions =  CollectionUtils.subtract(all, allInDb);

		if(!additions.isEmpty())
			topicNameRepository.saveAll(additions);
		 
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}
}
