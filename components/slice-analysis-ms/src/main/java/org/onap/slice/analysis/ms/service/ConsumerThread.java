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

import org.onap.slice.analysis.ms.configdb.IConfigDbService;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.SubCounter;
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
	private int samples;

	/**
	 * Default constructor.
	 */
	public ConsumerThread() {
		super();
		this.pmDataQueue = BeanUtil.getBean(PmDataQueue.class);
		this.configDbService = BeanUtil.getBean(IConfigDbService.class);
		this.initialDelaySec = Configuration.getInstance().getInitialDelaySeconds();
		this.samples = Configuration.getInstance().getSamples();
	}

	/**
	 * Consumes data from PM data queue, process the data and sends onset message to policy if needed
	 */
	@Override
	public void run() {    
		boolean done = false;
		boolean result = false;
		String snssai = "";
		List<String> nfs = null;
		while (!done) {
			try {
				Thread.sleep(initialDelaySec);
				log.info("Starting Consumer Thread");
				snssai = pmDataQueue.getSnnsaiFromQueue();
				if (!snssai.equals("")) {
					log.info("Consumer thread processing data for s-nssai {}",snssai);    
					try {
						nfs = configDbService.fetchNetworkFunctionsOfSnssai(snssai);
					}
					catch(Exception e) {
						pmDataQueue.putSnssaiToQueue(snssai);
						log.error("Exception caught while fetching nfs of snssai {}, {}", snssai, e.getMessage());
					}
					if(nfs != null && checkForEnoughSamples(nfs, snssai)) {
               					this.snssaiSamplesProcessor = BeanUtil.getBean(SnssaiSamplesProcessor.class);
						result = snssaiSamplesProcessor.processSamplesOfSnnsai(snssai, nfs);
						if(!result) {
							log.info("Not enough samples to process for {}",snssai);
							pmDataQueue.putSnssaiToQueue(snssai);
						}
					}
				}
			} catch (Exception e) {
				log.error("Exception in Consumer Thread, {}", e.getMessage());
				done = true;
			}
		}
	}

    /**
     * Checks whether enough samples are available for the network functions
     */
	public boolean checkForEnoughSamples(List<String> nfs, String snssai) {
		for(String nf : nfs) {
			if(! pmDataQueue.checkSamplesInQueue(new SubCounter(nf, snssai), samples)) {
				log.info("Not enough samples to process for network function {} of snssai {}", nf, snssai);
				pmDataQueue.putSnssaiToQueue(snssai);
				return false;
			}
		}
		return true;
	}
}
