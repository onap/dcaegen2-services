/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 China Mobile.
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

import java.io.IOException;
import java.util.Map;

import org.onap.dcaegen2.kpi.models.Configuration;
import org.onap.dcaegen2.kpi.utils.DmaapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.nsa.cambria.client.CambriaBatchingPublisher;

/**
 * Client class to handle kpi interactions.
 */
public class KpiDmaapClient {

    private static Logger logger = LoggerFactory.getLogger(KpiDmaapClient.class);

    private DmaapUtils dmaapUtils;

    private Configuration configuration;

    public KpiDmaapClient(DmaapUtils dmaapUtils, Configuration configuration) {
        this.dmaapUtils = dmaapUtils;
        this.configuration = configuration;
    }

    /**
     * Method stub for sending kpi msg to dmaap.
     */
    @SuppressWarnings("unchecked")
    public boolean sendNotificationToDmaap(String msg) {

        Map<String, Object> streamsPublishes = configuration.getStreamsPublishes();

        logger.info("streamsPublishes: {}", streamsPublishes);
        String topicUrl = ((Map<String, String>) ((Map<String, Object>) streamsPublishes.get("kpi_topic"))
                .get("dmaap_info")).get("topic_url");

        logger.info("Kpi publish topic url: {}", topicUrl);
        String[] topicSplit = topicUrl.split("\\/");
        String topic = topicSplit[topicSplit.length - 1];
        CambriaBatchingPublisher cambriaBatchingPublisher;
        try {

            cambriaBatchingPublisher = dmaapUtils.buildPublisher(configuration, topic);

            NotificationProducer notificationProducer = new NotificationProducer(cambriaBatchingPublisher);
            notificationProducer.sendNotification(msg);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}
