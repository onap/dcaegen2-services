/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021 China Mobile.
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

import org.onap.dcaegen2.kpi.computation.KpiComputation;
import org.onap.dcaegen2.kpi.models.Configuration;
import org.onap.dcaegen2.kpi.models.VesEvent;
import org.onap.dcaegen2.kpi.utils.DmaapUtils;
import org.onap.dcaegen2.kpi.utils.VesJsonConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SumKpiComputation.
 *
 * @author Kai Lu
 */
public class KpiComputationCallBack implements NotificationCallback {

    private static Logger logger = LoggerFactory.getLogger(KpiComputationCallBack.class);

    @Override
    public void activateCallBack(String msg) {
        Configuration config = Configuration.getInstance();
        if (config == null) {
            logger.info("Config is not exist");
            return;
        }

        kpiComputation(msg, config);
    }

    /**
     * do KPI computation and publish result to dmaap.
     *
     * @param msg    msg
     * @param config config
     *
     */
    public void kpiComputation(String msg, Configuration config) {
        logger.info("Original PM data: {}", msg);

        logger.info("Kpi Config: {}", config.getKpiConfig());

        // do computation
        KpiComputation kpiComputation = new KpiComputation();
        List<VesEvent> vesEvents = kpiComputation.checkAndDoComputation(msg, config);

        if (vesEvents == null || vesEvents.isEmpty()) {
            logger.info("No Kpi Event exist");
            return;
        }
        logger.info("KPI results: {}", vesEvents);

        // publish kpi computation result
        if (publish(vesEvents, config)) {
            logger.info("publish success");
        }

        logger.error("publish events failed: {}", vesEvents);

    }

    /**
     * ves publish.
     *
     * @param vesEvents vesEvents
     *
     */
    private boolean publish(List<VesEvent> vesEvents, Configuration configuration) {
        logger.info("Publishing KPI VES events to messagerouter.");
        KpiDmaapClient kpiDmaapClient = new KpiDmaapClient(new DmaapUtils(), configuration);
        try {
            vesEvents.forEach(e -> {
                // publish kpi computation result
                String event = VesJsonConversion.convertVesEventToString(e);

                logger.info("Publishing event: {}.", event);

                kpiDmaapClient.sendNotificationToDmaap(event);
            });
            logger.info("KPI computation done successfully");
            return true;
        } catch (Exception e) {
            logger.error("KPI computation done failed.", e);
        }
        return false;
    }
}
