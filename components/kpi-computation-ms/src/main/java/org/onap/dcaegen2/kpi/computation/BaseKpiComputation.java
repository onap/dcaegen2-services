/*-
* ============LICENSE_START=======================================================
*  Copyright (C) 2020-2021 China Mobile.
* ================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* SPDX-License-Identifier: Apache-2.0
* ============LICENSE_END=========================================================
*/

package org.onap.dcaegen2.kpi.computation;

import org.onap.dcaegen2.kpi.models.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
* RatioKpiComputation.
*
* @author curated code
*/
public abstract class BaseKpiComputation implements Command {

     public static VesEvent generateVesEvent(final PerformanceEvent pmEvent, final String measInfoIdValue,
            final BigDecimal result, final String measType) {

        // Create ves kpi data
        CommonEventHeader commonEventHeader = new CommonEventHeader();
        commonEventHeader.setDomain(pmEvent.getCommonEventHeader().getDomain());
        commonEventHeader.setEventId(UUID.randomUUID().toString());
        commonEventHeader.setSequence(1);
        commonEventHeader.setEventName(pmEvent.getCommonEventHeader().getEventName());
        commonEventHeader.setSourceName(pmEvent.getCommonEventHeader().getSourceName());
        commonEventHeader.setReportingEntityName(pmEvent.getCommonEventHeader().getReportingEntityName());
        commonEventHeader.setPriority(pmEvent.getCommonEventHeader().getPriority());
        commonEventHeader.setStartEpochMicrosec(pmEvent.getCommonEventHeader().getStartEpochMicrosec());
        commonEventHeader.setLastEpochMicrosec(pmEvent.getCommonEventHeader().getLastEpochMicrosec());
        commonEventHeader.setVersion(pmEvent.getCommonEventHeader().getVersion());
        commonEventHeader.setVesEventListenerVersion(pmEvent.getCommonEventHeader().getVesEventListenerVersion());
        commonEventHeader.setTimeZoneOffset(pmEvent.getCommonEventHeader().getTimeZoneOffset());
        Perf3gppFields perf3gppFields = new Perf3gppFields();
        perf3gppFields.setPerf3gppFieldsVersion(pmEvent.getPerf3gppFields().getPerf3gppFieldsVersion());
        MeasDataCollection tmpMeasDataCollection = new MeasDataCollection();
        tmpMeasDataCollection
                .setGranularityPeriod(pmEvent.getPerf3gppFields().getMeasDataCollection().getGranularityPeriod());
        tmpMeasDataCollection.setMeasuredEntityUserName(
                pmEvent.getPerf3gppFields().getMeasDataCollection().getMeasuredEntityUserName());
        tmpMeasDataCollection
                .setMeasuredEntityDn(pmEvent.getPerf3gppFields().getMeasDataCollection().getMeasuredEntityDn());
        tmpMeasDataCollection.setMeasuredEntitySoftwareVersion(
                pmEvent.getPerf3gppFields().getMeasDataCollection().getMeasuredEntitySoftwareVersion());
        MeasInfoId measInfoId = new MeasInfoId();
        measInfoId.setMeasInfoId(measInfoIdValue);
        MeasTypes measTypes = new MeasTypes();
        List<String> measTypesList = new ArrayList<>();
        measTypesList.add(measType);
        measTypes.setMeasTypesList(measTypesList);
        MeasValues measValue = new MeasValues();
        measValue.setSuspectFlag(false);
        List<MeasResult> measResults = new ArrayList<>();
        MeasResult measureMent = new MeasResult();

        measureMent.setPvalue(1);
        measureMent.setSvalue(result.toString());
        measResults.add(measureMent);
        MeasInfo measInfo = new MeasInfo();
        measValue.setMeasResults(measResults);
        List<MeasValues> measValuesList = new ArrayList<>();
        measValuesList.add(measValue);
        measInfo.setMeasInfoId(measInfoId);
        measInfo.setMeasTypes(measTypes);
        measInfo.setMeasValuesList(measValuesList);
        List<MeasInfo> measInfoList = new ArrayList<>();
        measInfoList.add(measInfo);
        tmpMeasDataCollection.setMeasInfoList(measInfoList);
        perf3gppFields.setMeasDataCollection(tmpMeasDataCollection);
        VesEvent kpiVesEvent = new VesEvent();
        PerformanceEvent kpiEvent = new PerformanceEvent();
        kpiEvent.setCommonEventHeader(commonEventHeader);
        kpiEvent.setPerf3gppFields(perf3gppFields);
        kpiVesEvent.setEvent(kpiEvent);
        
        return kpiVesEvent;
    }

}

