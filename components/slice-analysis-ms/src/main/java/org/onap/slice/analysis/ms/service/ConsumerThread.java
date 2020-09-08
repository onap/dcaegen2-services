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
import org.onap.slice.analysis.ms.utils.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerThread extends Thread {
	private static Logger log = LoggerFactory.getLogger(PmThread.class);
    private PmDataQueue pmDataQueue;
    private IConfigDbService configDbService;
    private SnssaiSamplesProcessor snssaiSamplesProcessor;
    
    /**
     * parameterized constructor.
     */
    public ConsumerThread() {
        super();
        this.pmDataQueue = BeanUtil.getBean(PmDataQueue.class);
        this.configDbService = BeanUtil.getBean(IConfigDbService.class);
        this.snssaiSamplesProcessor = BeanUtil.getBean(SnssaiSamplesProcessor.class);
    }
    
    /**
     * check for new PM notification. Fetch notification from the database, process and put it in the pm data queue
     */
    @Override
    public void run() {    
        boolean done = false;
        String snssai = "";
        while (!done) {
            try {
                Thread.sleep(360000);
                log.info("Consumer thread starting ...");    
                snssai = pmDataQueue.getSnnsaiFromQueue();
                snssaiSamplesProcessor.processSamplesOfSnnsai(snssai, configDbService.fetchNetworkFunctionsOfSnssai(snssai));
            } catch (Exception e) {
                log.error("Exception in Consumer Thread ", e);
                done = true;
            }
        }
    }
}
