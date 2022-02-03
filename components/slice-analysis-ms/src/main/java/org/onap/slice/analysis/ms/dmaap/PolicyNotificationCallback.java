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

import org.slf4j.Logger;

/**
 * Handles Notification on dmaap for Policy events
 */
public class PolicyNotificationCallback implements NotificationCallback {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PolicyNotificationCallback.class);

    /**
     * Trigger on Notification from policy component
     */
    @Override
    public void activateCallBack(String msg) {
        handlePolicyNotification(msg);
    }

    /**
     * Parse and take actions on reception of Notification from Policy
     * 
     * @param msg
     */
    private void handlePolicyNotification(String msg) {
        log.info("Message received from policy: " + msg);
        // TBD - actions to perform on reception of notification from policy
    }
}
