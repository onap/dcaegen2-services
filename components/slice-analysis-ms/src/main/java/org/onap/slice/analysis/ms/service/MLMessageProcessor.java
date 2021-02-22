/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020-2021 Wipro Limited.
 *   ==============================================================================
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     ============LICENSE_END=========================================================
 *
 *******************************************************************************/

package org.onap.slice.analysis.ms.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.onap.slice.analysis.ms.configdb.AaiInterface;
import org.onap.slice.analysis.ms.configdb.CpsInterface;
import org.onap.slice.analysis.ms.configdb.IConfigDbService;
import org.onap.slice.analysis.ms.models.CUModel;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.MLOutputModel;
import org.onap.slice.analysis.ms.models.policy.AdditionalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Process the message sent by ML service and sends notification to policy
 */
@Component
@Scope("prototype")
public class MLMessageProcessor {
	private static Logger log = LoggerFactory.getLogger(MLMessageProcessor.class);

	@Autowired
	private IConfigDbService configDbService;

	@Autowired
	private PolicyService policyService;

	@Autowired
	private AaiInterface aaiInterface;

	@Autowired
	private CpsInterface cpsInterface;

	public void processMLMsg(MLOutputModel mlOutputMsg) {
		Boolean isConfigDbEnabled = (Objects.isNull(Configuration.getInstance().getConfigDbEnabled())) ? true
				: Configuration.getInstance().getConfigDbEnabled();
		Map<String, List<String>> ricToCellMapping = null;
		Map<String, String> serviceDetails = null;
		String snssai = mlOutputMsg.getSnssai();
		List<CUModel> cuData = mlOutputMsg.getData();
		if (isConfigDbEnabled) {
			ricToCellMapping = configDbService.fetchRICsOfSnssai(snssai);
		} else {
			ricToCellMapping = cpsInterface.fetchRICsOfSnssai(snssai);
		}
		log.debug("RIC to cell mapping of S-NSSAI {} is {}", snssai, ricToCellMapping);
		for (CUModel cuModel : cuData) {
			String cellId = String.valueOf(cuModel.getCellCUList().get(0).getCellLocalId());
			ricToCellMapping.forEach((ricId, cells) -> {
				if (cells.contains(cellId)) {
					cuModel.setNearRTRICId(ricId);
				}
			});
		}
		AdditionalProperties<MLOutputModel> addProps = new AdditionalProperties<>();
		addProps.setResourceConfig(mlOutputMsg);

		if (isConfigDbEnabled) {
			serviceDetails = configDbService.fetchServiceDetails(snssai);
		} else {
			serviceDetails = aaiInterface.fetchServiceDetails(snssai);

		}
		policyService.sendOnsetMessageToPolicy(snssai, addProps, serviceDetails);
	}
}

