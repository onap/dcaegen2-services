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

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

/**
 * This class indicates whether new pm notification
 * is set for the slice-analysis-ms
 */
@Component
public class NewPmNotification {

    private boolean newNotif;

    /**
     * Initialize new pm Notification flag
     */
    @PostConstruct
    public void init() {
        newNotif = false;
    }

    public boolean getNewNotif() {
        return newNotif;
    }

    public void setNewNotif(boolean newNotif) {
        this.newNotif = newNotif;
    }

    public NewPmNotification(boolean newNotif) {
        super();
        this.newNotif = newNotif;
    }

    /**
     * Default constructor
     */
    public NewPmNotification() {

    }

}
