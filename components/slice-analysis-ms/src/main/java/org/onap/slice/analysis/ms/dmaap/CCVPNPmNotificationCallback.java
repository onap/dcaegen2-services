/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.slice.analysis.ms.data.beans.PerformanceNotifications;
import org.onap.slice.analysis.ms.data.repository.PerformanceNotificationsRepository;
import org.onap.slice.analysis.ms.models.ccvpnnotification.CCVPNNotificationFields;
import org.onap.slice.analysis.ms.models.ccvpnnotification.CCVPNPmDatastore;
import org.onap.slice.analysis.ms.utils.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles Notification on dmaap for ccvpn notification events
 */
@Component
public class CCVPNPmNotificationCallback implements NotificationCallback {

    @Autowired
    CCVPNPmDatastore ccvpnPmDatastore;

    private static Logger log = LoggerFactory.getLogger(CCVPNPmNotificationCallback.class);

    /**
     * Triggers on handleNofitication method
     */
    @Override
    public void activateCallBack(String msg) {
        handleNotification(msg);
    }

    /**
     * Parse Performance dmaap notification and save to DB 
     * @param msg
     */
    private void handleNotification(String msg) {
        log.info("Message received from VES : {}" ,msg);
        ObjectMapper obj = new ObjectMapper();
        CCVPNNotificationFields output = null;
        String cllId = null;
        String uniId = null;
        String bw = null;
        try {
            output = obj.readValue(msg, new TypeReference<CCVPNNotificationFields>(){});
            cllId = output.getArrayOfNamedHashMap().get(0).getHashMap().getCllId();
            uniId = output.getArrayOfNamedHashMap().get(0).getHashMap().getUniId();
            bw = output.getArrayOfNamedHashMap().get(0).getHashMap().getBandwidthValue();
        }
        catch (IOException e) {
            log.error("Error converting VES msg to object, {}",e.getMessage());
        }
        ccvpnPmDatastore.addUsedBwToEndpoint(cllId, uniId, bw);
    }

}
