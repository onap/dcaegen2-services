/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2023 Huawei Technologies Co., Ltd. All rights reserved.
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

import java.util.Date;
import javax.annotation.PostConstruct;
import org.onap.slice.analysis.ms.dmaap.UUIDmaapClient;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.models.uui.UUIEntity;
import org.onap.slice.analysis.ms.models.uui.UUIOnsetMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serivce to generate and publish onsetMessage to UUI
 */
@Component
public class UUIService {
    private final static int SERVICE_RATE_INTERVAL = 5000; // in ms

    private UUIDmaapClient uuiDmaapClient;
    private static Logger log = LoggerFactory.getLogger(PolicyService.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private RateLimiter rateLimiter;

    /**
     * Initialization
     */
    @PostConstruct
    public void init() {
        Configuration configuration = Configuration.getInstance();
        uuiDmaapClient = new UUIDmaapClient(configuration);
        rateLimiter = new RateLimiter(1, SERVICE_RATE_INTERVAL);
    }

    /**
     * Form onset message sent to uui in CCVPN IBN use case
     * @param cllId
     * @param result
     * @param reason
     * @return
     * @param <T>
     */
    public <T> UUIOnsetMessage formUUIOnsetMessage(String cllId, String result, String reason) {
        UUIOnsetMessage onsetMessage = new UUIOnsetMessage();
        UUIEntity entity = new UUIEntity();
        entity.setId(cllId);
        entity.setOperation("assurance");
        entity.setResult(result);
        entity.setReason(reason);
        onsetMessage.setSource("DCAE");
        onsetMessage.setTimestamp(new Date());
        onsetMessage.setEntity(entity);
        return onsetMessage;
    }

    /**
     * Sending the onsetMessage to UUI through UUIDmaapClient
     * @param uuiOnsetMessage
     * @param <T>
     */
    public <T> void sendOnsetMessageToUUI(UUIOnsetMessage uuiOnsetMessage){
        String msg =  "";
        try {
            msg = objectMapper.writeValueAsString(uuiOnsetMessage);
            rateLimiter.getToken();
            log.info("Sending onset message to uui for ControlLoop-CCVPN-CLL, the msg: {}", msg);
            uuiDmaapClient.sendNotificationToUUI(msg);
        }
        catch (Exception e) {
            log.error("Error sending notification to uui, {}",e.getMessage());
        }
    }
}
