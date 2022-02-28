/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
 *   Copyright (C) 2022 Huawei Canada Limited.
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

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.utils.DmaapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.att.nsa.cambria.client.CambriaConsumer;

/**
 * This class initializes and starts the dmaap client
 * to listen on application required dmaap events
 */
@Component
public class DmaapClient {

    private static final String AAI_SUBSCRIBER = "aai_subscriber";
    private Configuration configuration;
    private static Logger log = LoggerFactory.getLogger(DmaapClient.class);

    private DmaapUtils dmaapUtils;

    @Autowired
    private IntelligentSlicingCallback intelligentSlicingCallback;

    @Autowired
    private VesNotificationCallback vesNotificationCallback;

    @Autowired
    private AaiEventNotificationCallback aaiEventNotificationCallback;

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

        String policyResponseTopicUrl = ((Map<String, String>) ((Map<String, Object>) streamSubscribes
                .get("dcae_cl_response_topic")).get("dmaap_info")).get("topic_url");
        String[] policyResponseTopicUrlSplit = policyResponseTopicUrl.split("\\/");
        String policyResponseTopic = policyResponseTopicUrlSplit[policyResponseTopicUrlSplit.length - 1];
        log.debug("policyResponse Topic : {}", policyResponseTopic);

        String intelligentSlicingTopicUrl = ((Map<String, String>) ((Map<String, Object>) streamSubscribes
                .get("intelligent_slicing_topic")).get("dmaap_info")).get("topic_url");
        String[] intelligentSlicingTopicSplit = intelligentSlicingTopicUrl.split("\\/");
        String intelligentSlicingTopic = intelligentSlicingTopicSplit[intelligentSlicingTopicSplit.length - 1];
        log.debug("intelligent slicing topic : {}", pmTopic);

        // Parsing ccvpn notification topic
        String ccvpnNotiTopicUrl = ((Map<String, String>) ((Map<String, Object>) streamSubscribes
                .get("ves_ccvpn_notification_topic")).get("dmaap_info")).get("topic_url");
        String[] ccvpnNotiTopicSplit = ccvpnNotiTopicUrl.split("\\/");
        String ccvpnNotiTopic = ccvpnNotiTopicSplit[ccvpnNotiTopicSplit.length - 1];
        log.debug("ccvpn notification topic : {}", ccvpnNotiTopic);

        CambriaConsumer pmNotifCambriaConsumer = dmaapUtils.buildConsumer(configuration, pmTopic);
        CambriaConsumer policyResponseCambriaConsumer = dmaapUtils.buildConsumer(configuration, policyResponseTopic);
        CambriaConsumer intelligentSlicingCambriaConsumer = dmaapUtils.buildConsumer(configuration, intelligentSlicingTopic);
        // Creating ccvpn notification cambriaconsumer
        CambriaConsumer ccvpnNotiCambriaConsumer = dmaapUtils.buildConsumer(configuration, ccvpnNotiTopic);

        ScheduledExecutorService executorPool;

        // create notification consumers for PM
        NotificationConsumer pmNotificationConsumer = new NotificationConsumer(pmNotifCambriaConsumer,
                new PmNotificationCallback());
        // start pm notification consumer threads
        executorPool = Executors.newScheduledThreadPool(10);
        executorPool.scheduleAtFixedRate(pmNotificationConsumer, 0, configuration.getPollingInterval(),
                TimeUnit.SECONDS);

        // create notification consumers for Policy
        NotificationConsumer policyNotificationConsumer = new NotificationConsumer(policyResponseCambriaConsumer,
                new PolicyNotificationCallback());
        // start policy notification consumer threads
        executorPool = Executors.newScheduledThreadPool(10);
        executorPool.scheduleAtFixedRate(policyNotificationConsumer, 0, configuration.getPollingInterval(),
                TimeUnit.SECONDS);

        // create notification consumers for ML MS
        NotificationConsumer intelligentSlicingConsumer = new NotificationConsumer(intelligentSlicingCambriaConsumer,
                intelligentSlicingCallback);
        // start intelligent Slicing notification consumer threads
        executorPool = Executors.newScheduledThreadPool(10);
        executorPool.scheduleAtFixedRate(intelligentSlicingConsumer, 0, configuration.getPollingInterval(),
                TimeUnit.SECONDS);

        // create notification consumers for ccvpn close-loop PM
        NotificationConsumer ccvpnNotiConsumer = new NotificationConsumer(ccvpnNotiCambriaConsumer,
                vesNotificationCallback);
        executorPool = Executors.newScheduledThreadPool(1);
        executorPool.scheduleWithFixedDelay(ccvpnNotiConsumer, 0, configuration.getVesNotifPollingInterval(),
                TimeUnit.SECONDS);

        // start AAI-EVENT dmaap topic monitor
        MRTopicMonitor mrTopicMonitor = new MRTopicMonitor(AAI_SUBSCRIBER, aaiEventNotificationCallback);
        mrTopicMonitor.start();
    }

}
