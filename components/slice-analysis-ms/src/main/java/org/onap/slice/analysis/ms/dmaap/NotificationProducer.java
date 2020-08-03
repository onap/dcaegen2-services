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

import com.att.nsa.cambria.client.CambriaBatchingPublisher;

import java.io.IOException;

/**
 * Produces Notification on DMAAP events
 */
public class NotificationProducer {

    private CambriaBatchingPublisher cambriaBatchingPublisher;
     

    /**
     * Parameterized constructor.
     */
    public NotificationProducer(CambriaBatchingPublisher cambriaBatchingPublisher) {
        super();
        this.cambriaBatchingPublisher = cambriaBatchingPublisher;
    }

    /**
     * sends notification to dmaap.
     */
    public int sendNotification(String msg) throws IOException {
    
        return cambriaBatchingPublisher.send("", msg);

    }

}
