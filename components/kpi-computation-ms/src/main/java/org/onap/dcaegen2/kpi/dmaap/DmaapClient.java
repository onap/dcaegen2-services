/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 China Mobile.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.kpi.dmaap;

import com.att.nsa.cambria.client.CambriaConsumer;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;


import org.onap.dcaegen2.kpi.models.Configuration;
import org.onap.dcaegen2.kpi.utils.DmaapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.stereotype.Component;

/**
 * This class initializes and starts the dmaap client 
 * to listen on application required dmaap events.
 */
@Component
public class DmaapClient {

    private Configuration configuration;
    private static Logger log = LoggerFactory.getLogger(DmaapClient.class);

    private DmaapUtils dmaapUtils;
   
    /**
     * init dmaap client.
     */
    @PostConstruct
    public void initClient() {
        log.debug("initializing client");
        dmaapUtils = new DmaapUtils();
        configuration = Configuration.getInstance();
        if (log.isDebugEnabled()) {
            log.debug(configuration.toString());
        }

        startClient();
    }

    /**
     * start dmaap client.
     */
    @SuppressWarnings("unchecked")
    public synchronized void startClient() {

        Map<String, Object> streamSubscribes = configuration.getStreamsSubscribes();
       
        String pmTopicUrl = ((Map<String, String>) ((Map<String, Object>) streamSubscribes
                .get("performance_management_topic")).get("dmaap_info")).get("topic_url");
        String[] pmTopicSplit = pmTopicUrl.split("\\/");
        String pmTopic = pmTopicSplit[pmTopicSplit.length - 1];
        log.debug("pm topic : {}", pmTopic);
        
       
        CambriaConsumer pmNotifCambriaConsumer = dmaapUtils.buildConsumer(configuration, pmTopic);

        ScheduledExecutorService executorPool;

        // create notification consumers for PM
        NotificationConsumer pmNotificationConsumer = new NotificationConsumer(pmNotifCambriaConsumer,
                new KpiComputationCallBack());
        // start pm notification consumer threads
        executorPool = Executors.newScheduledThreadPool(10);
        executorPool.scheduleAtFixedRate(pmNotificationConsumer, 0, configuration.getPollingInterval(),
                TimeUnit.SECONDS);

    }

}
