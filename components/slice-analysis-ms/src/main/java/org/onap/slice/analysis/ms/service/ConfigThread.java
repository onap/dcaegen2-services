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

import org.onap.slice.analysis.ms.models.ConfigPolicy;
import org.onap.slice.analysis.ms.service.ccvpn.CCVPNPmDatastore;
import org.onap.slice.analysis.ms.utils.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This thread is used to convert ccvpn runtime configurations from policy to our local memory CCVPNPmDatastore
 */
public class ConfigThread extends Thread{

    CCVPNPmDatastore ccvpnPmDatastore = (CCVPNPmDatastore) SpringContextUtil.getBean(CCVPNPmDatastore.class);
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
                if(configPolicy.getConfig() != null) {
                    String cllId = null;
                    Boolean clBwAssuranceStatus = null;
                    int originalBw = 0;
                    if(configPolicy.getConfig().containsKey("cllId")){
                        cllId = String.valueOf(configPolicy.getConfig().get("cllId"));
                    }
                    if(configPolicy.getConfig().containsKey("closedLoopStatus")){
                        clBwAssuranceStatus = String.valueOf(configPolicy.getConfig().get("closedLoopStatus")).equalsIgnoreCase("true");
                    }
                    if(configPolicy.getConfig().containsKey("originalBw")){
                        originalBw = Integer.parseInt(String.valueOf(configPolicy.getConfig().get("originalBw")));
                    }
                    if(cllId!=null && clBwAssuranceStatus!=null){
                        ccvpnPmDatastore.updateConfigFromPolicy(cllId, clBwAssuranceStatus, originalBw);
                    }
                } else {
                    log.error("Config policy is empty, nothing to update.");
                }

            } catch (Exception e) {
                log.error("Exception in Config Thread ", e);
                done = true;
            }
        }
    }
}
