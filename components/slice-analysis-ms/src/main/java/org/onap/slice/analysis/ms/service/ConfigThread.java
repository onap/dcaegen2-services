/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Huawei Technologies Co., Ltd.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.onap.slice.analysis.ms.models.ConfigPolicy;
import org.onap.slice.analysis.ms.models.SubCounter;
import org.onap.slice.analysis.ms.models.pmnotification.PmNotification;
import org.onap.slice.analysis.ms.service.ccvpn.CCVPNPmDatastore;
import org.onap.slice.analysis.ms.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigThread extends Thread{

    @Autowired
    CCVPNPmDatastore ccvpnPmDatastore;
    private static Logger log = LoggerFactory.getLogger(ConfigThread.class);


    public ConfigThread () {
        super();
    }

    public void run() {
        log.info("Config Thread is starting...");
        boolean done = false;
        while(!done) {
            try {
                Thread.sleep(1000);
                ConfigPolicy configPolicy = ConfigPolicy.getInstance();
                if(configPolicy != null) {
                    // config content
                    //timer = (double) configPolicy.getConfig().get("PCI_NEIGHBOR_CHANGE_CLUSTER_TIMEOUT_IN_SECS");
                    String cllId = String.valueOf(configPolicy.getConfig().get("cllId"));
                    Boolean clBwAssuranceStatus = String.valueOf(configPolicy.getConfig().get("closedLoopStatus")).equalsIgnoreCase("true");
                    int originalBw = Integer.parseInt(String.valueOf(configPolicy.getConfig().get("originalBw")));
                    ccvpnPmDatastore.updateConfigFromPolicy(cllId, clBwAssuranceStatus, originalBw);
                    log.debug("Successfully updated runtime configurations. cllId: {}, closedLoopStatus: {}, originalBw :{}", cllId, clBwAssuranceStatus, originalBw);
                } else {
                    log.debug("Config policy is empty, nothing to update.");
                }
            } catch (Exception e) {
                log.error("Exception in Config Thread ", e);
                done = true;
            }
        }
    }
}
