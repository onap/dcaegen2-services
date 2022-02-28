/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Huawei Canada Limited.
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
package org.onap.slice.analysis.ms.models.vesnotification;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

/**
 * Model class for Ves notification message
 */
@Generated("jsonschema2pojo")
public class NotificationFields {

    private String changeIdentifier;
    private String changeType;
    private String notificationFieldsVersion;
    private List<ArrayOfNamedHashMap> arrayOfNamedHashMap = new ArrayList<ArrayOfNamedHashMap>();

    public String getChangeIdentifier() {
        return changeIdentifier;
    }

    public void setChangeIdentifier(String changeIdentifier) {
        this.changeIdentifier = changeIdentifier;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getNotificationFieldsVersion() {
        return notificationFieldsVersion;
    }

    public void setNotificationFieldsVersion(String notificationFieldsVersion) {
        this.notificationFieldsVersion = notificationFieldsVersion;
    }

    public List<ArrayOfNamedHashMap> getArrayOfNamedHashMap() {
        return arrayOfNamedHashMap;
    }

    public void setArrayOfNamedHashMap(List<ArrayOfNamedHashMap> arrayOfNamedHashMap) {
        this.arrayOfNamedHashMap = arrayOfNamedHashMap;
    }

}
