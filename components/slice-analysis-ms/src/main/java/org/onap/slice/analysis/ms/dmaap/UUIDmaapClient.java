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

package org.onap.slice.analysis.ms.dmaap;

import java.io.IOException;
import java.util.Map;

import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.utils.DcaeDmaapUtil;

/**
 * Client class to handle intent analysis server interactions
 */
public class UUIDmaapClient {

    private Configuration configuration;

    public UUIDmaapClient(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Method stub for sending notification to UUI intent analysis server.
     */
    @SuppressWarnings("unchecked")
    public boolean sendNotificationToPolicy(String msg) {
        Map<String, Object> streamsPublishes = configuration.getStreamsPublishes();
        String topicUrl = ((Map<String, String>) ((Map<String, Object>) streamsPublishes.get("CCVPN_CL_DCAE_EVENT"))
            .get("dmaap_info")).get("topic_url");
        try {
            MessageRouterPublisher publisher = DcaeDmaapUtil.buildPublisher();
            MessageRouterPublishRequest request = DcaeDmaapUtil.buildPublisherRequest("CCVPN_CL_DCAE_EVENT", topicUrl);

            NotificationProducer notificationProducer = new NotificationProducer(publisher, request);
            notificationProducer.sendNotification(msg);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}
