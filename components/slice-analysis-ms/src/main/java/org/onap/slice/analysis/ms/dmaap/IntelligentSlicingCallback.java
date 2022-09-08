/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
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

package org.onap.slice.analysis.ms.dmaap;

import java.io.IOException;

import org.onap.slice.analysis.ms.models.MLOutputModel;
import org.onap.slice.analysis.ms.service.MLMessageProcessor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles Notification on dmaap for ML ms events
 */
@Component
public class IntelligentSlicingCallback implements NotificationCallback {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(IntelligentSlicingCallback.class);
	
	@Autowired
	private MLMessageProcessor mlMsMessageProcessor;

	/**
	 * Trigger on Notification from ML ms
	 */
	@Override
	public void activateCallBack(String msg) {
		handlePolicyNotification(msg);
	}

	/**
	 * Parse and take actions on reception of Notification from ML ms
	 * @param msg
	 */
	private void handlePolicyNotification(String msg) {
		log.info("Message received from ML ms: {}" ,msg);
		ObjectMapper obj = new ObjectMapper();
		MLOutputModel output = null;
		try { 
			output = obj.readValue(msg, new TypeReference<MLOutputModel>(){});
			mlMsMessageProcessor.processMLMsg(output);
		} 
		catch (IOException e) { 
			log.error("Error converting ML msg to object, {}",e.getMessage());
		} 
	}
}
