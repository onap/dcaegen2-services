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

package org.onap.slice.analysis.ms.models.pmnotification;

/**
 * Model class for the CommonEventHeader Object
 */
public class CommonEventHeader {

    private String domain;
    private String eventId;
    private long sequence;
    private String eventName;
    private String sourceName;
    private String reportingEntityName;
    private String priority;
    private long startEpochMicrosec;
    private long lastEpochMicrosec;
    private String version;
    private String vesEventListenerVersion;
    private String timeZoneOffset;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getReportingEntityName() {
        return reportingEntityName;
    }

    public void setReportingEntityName(String reportingEntityName) {
        this.reportingEntityName = reportingEntityName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public long getStartEpochMicrosec() {
        return startEpochMicrosec;
    }

    public void setStartEpochMicrosec(long startEpochMicrosec) {
        this.startEpochMicrosec = startEpochMicrosec;
    }

    public long getLastEpochMicrosec() {
        return lastEpochMicrosec;
    }

    public void setLastEpochMicrosec(long lastEpochMicrosec) {
        this.lastEpochMicrosec = lastEpochMicrosec;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVesEventListenerVersion() {
        return vesEventListenerVersion;
    }

    public void setVesEventListenerVersion(String vesEventListenerVersion) {
        this.vesEventListenerVersion = vesEventListenerVersion;
    }

    public String getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(String timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

}
