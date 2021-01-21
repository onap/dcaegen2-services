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

import java.util.List;
import java.util.Map;

import org.onap.dcaegen2.kpi.computation.KpiComputation;
import org.onap.dcaegen2.kpi.models.Configuration;
import org.onap.dcaegen2.kpi.models.VesEvent;
import org.onap.dcaegen2.kpi.service.KpiService;
import org.onap.dcaegen2.kpi.utils.VesJsonConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class KpiComputationCallBack implements NotificationCallback {

    private static Logger logger = LoggerFactory.getLogger(KpiComputationCallBack.class);

    @Autowired
    private KpiService kpiService;

//    @Autowired
//    private MongodbService mongodbService;

    @Override
    public void activateCallBack(String msg) {
        kpiComputation(msg);
    }

    private void kpiComputation(String msg) {
        logger.info("Original PM data: {}", msg);

        Configuration config = kpiService.getConfiguration();

        // do computation
        KpiComputation kpiComputation = new KpiComputation();
        List<VesEvent> vesEvents = kpiComputation.checkAndDoComputation(msg, kpiService.getConfiguration());

        // publish kpi computation result
        publish(vesEvents);
 
        Map<String, Object> streamsPublishes = config.getStreamsPublishes();

        @SuppressWarnings("unchecked")
        String topicUrl = ((Map<String, String>) ((Map<String, Object>) streamsPublishes.get("CL_topic"))
                .get("dmaap_info")).get("topic_url");
        String[] topicSplit = topicUrl.split("\\/");
        String topicName = topicSplit[topicSplit.length - 1];

    }

    /**
     * ves publish.
     *
     * @param vesEvents vesEvents
     *
     */
    public void publish(List<VesEvent> vesEvents) {
        logger.info("Publishing KPI VES events to messagerouter.");
        try {
            vesEvents.forEach(e -> {
                // publish kpi computation result
                String event = VesJsonConversion.convertVesEventToString(e);
                kpiService.messagePublish(event);
            });
            logger.info("KPI computation done successfully");
        } catch (Exception e) {
            logger.error("KPI computation done failed.", e);
        }
    }
}
