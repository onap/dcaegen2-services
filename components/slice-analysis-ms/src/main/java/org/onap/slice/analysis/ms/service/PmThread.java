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

import java.util.List;
import java.util.Map;

import org.onap.slice.analysis.ms.data.repository.PerformanceNotificationsRepository;
import org.onap.slice.analysis.ms.dmaap.NewPmNotification;
import org.onap.slice.analysis.ms.models.MeasurementObject;
import org.onap.slice.analysis.ms.models.SubCounter;
import org.onap.slice.analysis.ms.models.pmnotification.PmNotification;
import org.onap.slice.analysis.ms.utils.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PmThread extends Thread {
	private static Logger log = LoggerFactory.getLogger(PmThread.class);
    private NewPmNotification newPmNotification;
    private PerformanceNotificationsRepository performanceNotificationsRepository;
    private IPmEventProcessor pmEventProcessor;
    private PmDataQueue pmDataQueue;
    
    /**
     * parameterized constructor.
     */
    public PmThread(NewPmNotification newPmNotification) {
        super();
        this.newPmNotification = newPmNotification;
        this.performanceNotificationsRepository = BeanUtil.getBean(PerformanceNotificationsRepository.class);
        this.pmEventProcessor = BeanUtil.getBean(IPmEventProcessor.class);
        this.pmDataQueue = BeanUtil.getBean(PmDataQueue.class);
    }
    
    /**
     * check for new PM notification. Fetch notification from the database, process and put it in the pm data queue
     */
    @Override
    public void run() {
        log.info("PM thread starting ...");        
        boolean done = false;
		PmNotification pmNotification;
		Map<String, List<MeasurementObject>> processedData;
        while (!done) {
            try {
                Thread.sleep(1000);
                if (newPmNotification.getNewNotif()) {
                    log.info("New PM notification from Dmaap");
                    String pmNotificationString = performanceNotificationsRepository.getPerformanceNotificationFromQueue();
                    if(pmNotificationString != null) {
                    	ObjectMapper mapper = new ObjectMapper();
            			pmNotification = mapper.readValue(pmNotificationString, PmNotification.class);
            			processedData = pmEventProcessor.processEvent(pmNotification.getEvent());
            			String networkFunction = pmNotification.getEvent().getPerf3gppFields().getMeasDataCollection().getMeasuredEntityDn();
            			processedData.forEach((key,value) -> {
            				SubCounter subCounter = new SubCounter(networkFunction, key);
            				pmDataQueue.putDataToQueue(subCounter, value);
            				pmDataQueue.putSnssaiToQueue(subCounter.getMeasuredObject());
            			});
            		
                    }
                }
            } catch (Exception e) {
                log.error("Exception in PM Thread ", e);
                done = true;
            }
        }

    }
}
