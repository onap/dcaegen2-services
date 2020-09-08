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

package org.onap.slice.analysis.ms.models.policy;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OnsetMessage {

    private String closedLoopControlName;
    private Long closedLoopAlarmStart;
    private String closedLoopEventClient;
    private String closedLoopEventStatus;
    private String requestID;
    
    @JsonProperty("target_type")
    private String targetType;   
    
    @JsonProperty("AAI")
    private AAI aai;
    
    private String target;
    private Payload payload;
    private String from;
    private String version;

    public String getClosedLoopControlName() {
        return closedLoopControlName;
    }

    public void setClosedLoopControlName(String closedLoopControlName) {
        this.closedLoopControlName = closedLoopControlName;
    }

    public Long getClosedLoopAlarmStart() {
        return closedLoopAlarmStart;
    }

    public void setClosedLoopAlarmStart(Long closedLoopAlarmStart) {
        this.closedLoopAlarmStart = closedLoopAlarmStart;
    }

    public String getClosedLoopEventClient() {
        return closedLoopEventClient;
    }

    public void setClosedLoopEventClient(String closedLoopEventClient) {
        this.closedLoopEventClient = closedLoopEventClient;
    }

    public String getClosedLoopEventStatus() {
        return closedLoopEventStatus;
    }

    public void setClosedLoopEventStatus(String closedLoopEventStatus) {
        this.closedLoopEventStatus = closedLoopEventStatus;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public AAI getAai() {
        return aai;
    }

    public void setAai(AAI aAI) {
        this.aai = aAI;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
