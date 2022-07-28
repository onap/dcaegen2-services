/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
 *   Copyright (C) 2022 Huawei Canada Limited.
 *   Copyright (C) 2022 CTC, Inc.
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

import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.utils.DcaeDmaapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class initializes and starts the dmaap client
 * to listen on application required dmaap events
 */
@Component
public class DmaapClient {

    private static final String AAI_SUBSCRIBER = "aai_subscriber";
    private Configuration configuration;
    private static Logger log = LoggerFactory.getLogger(DmaapClient.class);

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

        String policyResponseTopicUrl = ((Map<String, String>) ((Map<String, Object>) streamSubscribes
                .get("dcae_cl_response_topic")).get("dmaap_info")).get("topic_url");

        String intelligentSlicingTopicUrl = ((Map<String, String>) ((Map<String, Object>) streamSubscribes
                .get("intelligent_slicing_topic")).get("dmaap_info")).get("topic_url");

        // Parsing ccvpn notification topic
        String ccvpnNotiTopicUrl = ((Map<String, String>) ((Map<String, Object>) streamSubscribes
                .get("ves_ccvpn_notification_topic")).get("dmaap_info")).get("topic_url");

        MessageRouterSubscriber pmNotifSubscriber = DcaeDmaapUtil.buildSubscriber();
        MessageRouterSubscribeRequest pmNotifReqest = DcaeDmaapUtil.buildSubscriberRequest("performance_management_topic", pmTopicUrl);

        MessageRouterSubscriber policyNotifSubscriber = DcaeDmaapUtil.buildSubscriber();
        MessageRouterSubscribeRequest policyNotifReqest = DcaeDmaapUtil.buildSubscriberRequest("dcae_cl_response_topic", policyResponseTopicUrl);

        MessageRouterSubscriber intelligentSlicingSubscriber = DcaeDmaapUtil.buildSubscriber();
        MessageRouterSubscribeRequest intelligentSlicingReqest = DcaeDmaapUtil.buildSubscriberRequest("intelligent_slicing_topic", intelligentSlicingTopicUrl);

        MessageRouterSubscriber ccvpnNotiSubscriber = DcaeDmaapUtil.buildSubscriber();
        MessageRouterSubscribeRequest ccvpnNotiReqest = DcaeDmaapUtil.buildSubscriberRequest("ves_ccvpn_notification_topic", ccvpnNotiTopicUrl);

        ScheduledExecutorService executorPool;

        // create notification consumers for PM
        NotificationConsumer pmNotificationConsumer = new NotificationConsumer(pmNotifSubscriber, pmNotifReqest,
                new PmNotificationCallback());
        // start pm notification consumer threads
        executorPool = Executors.newScheduledThreadPool(10);
        executorPool.scheduleAtFixedRate(pmNotificationConsumer, 0, configuration.getPollingInterval(),
                TimeUnit.SECONDS);

        // create notification consumers for Policy
        NotificationConsumer policyNotificationConsumer = new NotificationConsumer(policyNotifSubscriber, policyNotifReqest,
                new PolicyNotificationCallback());
        // start policy notification consumer threads
        executorPool = Executors.newScheduledThreadPool(10);
        executorPool.scheduleAtFixedRate(policyNotificationConsumer, 0, configuration.getPollingInterval(),
                TimeUnit.SECONDS);

        // create notification consumers for ML MS
        NotificationConsumer intelligentSlicingConsumer = new NotificationConsumer(intelligentSlicingSubscriber, intelligentSlicingReqest,
                intelligentSlicingCallback);
        // start intelligent Slicing notification consumer threads
        executorPool = Executors.newScheduledThreadPool(10);
        executorPool.scheduleAtFixedRate(intelligentSlicingConsumer, 0, configuration.getPollingInterval(),
                TimeUnit.SECONDS);

        // create notification consumers for ccvpn close-loop PM
        NotificationConsumer ccvpnNotiConsumer = new NotificationConsumer(ccvpnNotiSubscriber, ccvpnNotiReqest,
                vesNotificationCallback);
        executorPool = Executors.newScheduledThreadPool(1);
        executorPool.scheduleWithFixedDelay(ccvpnNotiConsumer, 0, configuration.getVesNotifPollingInterval(),
                TimeUnit.SECONDS);

        // start AAI-EVENT dmaap topic monitor
        MRTopicMonitor mrTopicMonitor = new MRTopicMonitor(AAI_SUBSCRIBER, aaiEventNotificationCallback);
        mrTopicMonitor.start();
    }

}
