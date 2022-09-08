/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *  Copyright (C) 2022 Huawei Canada Limited.
 *  ==============================================================================
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.vesnotification.NotificationFields;

import org.onap.slice.analysis.ms.service.ccvpn.CCVPNPmDatastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Handles Notification on dmaap for ves notification events
 */
@Component
public class VesNotificationCallback implements NotificationCallback {

    private Configuration configuration;
    private String NOTIFICATIONFIELDS = "notificationFields";
    private String EVENT = "event";
    private String vesNotifChangeIdentifier;
    private String vesNotfiChangeType;

    @Autowired
    CCVPNPmDatastore ccvpnPmDatastore;

    private static Logger log = LoggerFactory.getLogger(VesNotificationCallback.class);

    /**
     * init ves callback; load configuration.
     */
    @PostConstruct
    public void init(){
        configuration = Configuration.getInstance();
        vesNotifChangeIdentifier = configuration.getVesNotifChangeIdentifier();
        vesNotfiChangeType = configuration.getVesNotifChangeType();
    }

    /**
     * Triggers on handleNofitication method
     * @param msg incoming message
     */
    @Override
    public void activateCallBack(String msg) {
        handleNotification(msg);
    }

    /**
     * Parse Performance dmaap notification and save to DB 
     * @param msg incoming message
     */
    private void handleNotification(String msg) {
        log.info("Message received from VES : {}" ,msg);
        ObjectMapper obj = new ObjectMapper();
        NotificationFields output = null;
        String notifChangeIdentifier = "";
        String notifChangeType = "";
        String cllId = null;
        String uniId = null;
        String bw = null;
        try {
            JsonNode node = obj.readTree(msg);
            JsonNode notificationNode = node.get(EVENT).get(NOTIFICATIONFIELDS);
            output = obj.treeToValue(notificationNode, NotificationFields.class);

            //Filter out target notification changeIdentifier and changeType
            notifChangeIdentifier = output.getChangeIdentifier();
            notifChangeType = output.getChangeType();
            if (notifChangeType.equals(vesNotfiChangeType)
            && notifChangeIdentifier.equals(vesNotifChangeIdentifier)) {
                cllId = output.getArrayOfNamedHashMap().get(0).getHashMap().getCllId();
                uniId = output.getArrayOfNamedHashMap().get(0).getHashMap().getUniId();
                bw = output.getArrayOfNamedHashMap().get(0).getHashMap().getBandwidthValue();
            }
        }
        catch (IOException e) {
            log.error("Error converting VES msg to object, {}", e.getMessage());
        }
        if (cllId != null && uniId != null && bw != null){
            log.info("Saving new CCVPN service usage data into ccvpnPmDatastore");
            log.debug("new bandwidth data -- serviceId: {}, uniId: {}, bw: {}", cllId, uniId, bw);
            ccvpnPmDatastore.addUsedBwToEndpoint(cllId, uniId, bw);
        }

    }

}
