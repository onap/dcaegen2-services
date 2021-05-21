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

import org.onap.slice.analysis.ms.data.beans.PerformanceNotifications;
import org.onap.slice.analysis.ms.data.repository.PerformanceNotificationsRepository;
import org.onap.slice.analysis.ms.utils.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Notification on dmaap for Performance events
 */
public class PmNotificationCallback implements NotificationCallback {

    private static Logger log = LoggerFactory.getLogger(PmNotificationCallback.class);

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

        PerformanceNotificationsRepository performanceNotificationsRepository = BeanUtil
                .getBean(PerformanceNotificationsRepository.class);
        PerformanceNotifications performanceNotification = new PerformanceNotifications();
        performanceNotification.setNotification(msg);
        log.info("Performance notification {}", performanceNotification);
        NewPmNotification newNotification = BeanUtil.getBean(NewPmNotification.class);
        performanceNotificationsRepository.save(performanceNotification);
        newNotification.setNewNotif(true);
    }

}
