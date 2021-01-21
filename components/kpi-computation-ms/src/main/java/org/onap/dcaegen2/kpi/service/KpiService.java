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

package org.onap.dcaegen2.kpi.service;

import javax.annotation.PostConstruct;

import org.onap.dcaegen2.kpi.dmaap.KpiDmaapClient;
import org.onap.dcaegen2.kpi.models.Configuration;
import org.onap.dcaegen2.kpi.utils.DmaapUtils;
import org.springframework.stereotype.Component;

@Component
public class KpiService {
    private KpiDmaapClient kpiDmaapClient;

    private Configuration configuration;

    @PostConstruct
    public void init() {
        Configuration configuration = Configuration.getInstance();
        kpiDmaapClient = new KpiDmaapClient(new DmaapUtils(), configuration);
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }

    public void messagePublish(String vesMsg) {
        kpiDmaapClient.sendNotificationToPolicy(vesMsg);
    }
}
