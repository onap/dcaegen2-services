/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2019 China Mobile
 *=================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.datalake.feeder.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FaultFields {

    private String eventSeverity;

    private String alarmCondition;

    private Integer faultFieldsVersion;

    private String specificProblem;

    private String alarmInterfaceA;

    private List<AlarmAdditionalInformation> alarmAdditionalInformation;

    private String eventSourceType;

    private String vfStatus;

    @Override
    public String toString() {
        return "FaultFields{" +
                "eventSeverity='" + eventSeverity + '\'' +
                ", alarmCondition='" + alarmCondition + '\'' +
                ", faultFieldsVersion=" + faultFieldsVersion +
                ", specificProblem='" + specificProblem + '\'' +
                ", alarmInterfaceA='" + alarmInterfaceA + '\'' +
                ", alarmAdditionalInformation=" + alarmAdditionalInformation +
                ", eventSourceType='" + eventSourceType + '\'' +
                ", vfStatus='" + vfStatus + '\'' +
                '}';
    }
}
