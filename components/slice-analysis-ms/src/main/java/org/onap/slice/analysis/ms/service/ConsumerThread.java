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

package org.onap.slice.analysis.ms.service;

import org.onap.slice.analysis.ms.configdb.IConfigDbService;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.utils.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * This Thread class consumes message from pm data queue and sends onset message to policy
 */
public class ConsumerThread extends Thread {
	private static Logger log = LoggerFactory.getLogger(ConsumerThread.class);
	private PmDataQueue pmDataQueue;
	private IConfigDbService configDbService;
	private SnssaiSamplesProcessor snssaiSamplesProcessor;
	private long initialDelaySec; 

	/**
	 * Default constructor.
	 */
	public ConsumerThread() {
		super();
		this.pmDataQueue = BeanUtil.getBean(PmDataQueue.class);
		this.configDbService = BeanUtil.getBean(IConfigDbService.class);
		this.snssaiSamplesProcessor = BeanUtil.getBean(SnssaiSamplesProcessor.class);
		this.initialDelaySec = Configuration.getInstance().getInitialDelaySeconds();
	}

	/**
	 * Consumes data from PM data queue, process the data and sends onset message to policy if needed
	 */
	@Override
	public void run() {    
		boolean done = false;
		String snssai = "";
		boolean result = false;
		while (!done) {
			try {
				Thread.sleep(initialDelaySec);
				snssai = pmDataQueue.getSnnsaiFromQueue();
				if (!snssai.equals("")) {
					log.info("Consumer thread processing data for s-nssai {}",snssai);    
					result = snssaiSamplesProcessor.processSamplesOfSnnsai(snssai, configDbService.fetchNetworkFunctionsOfSnssai(snssai));
					if(!result) {
						log.info("Not enough samples to process for {}",snssai);
						pmDataQueue.putSnssaiToQueue(snssai);
					}
				}
			} catch (Exception e) {
				log.error("Exception in Consumer Thread ", e);
				done = true;
			}
		}
	}
}
