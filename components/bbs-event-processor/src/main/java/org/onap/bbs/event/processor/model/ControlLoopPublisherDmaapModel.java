/*
 * ============LICENSE_START=======================================================
 * BBS-RELOCATION-CPE-AUTHENTICATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.bbs.event.processor.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters(fieldNamingStrategy = true)
public interface ControlLoopPublisherDmaapModel {

    @SerializedName(value = "closedLoopEventClient", alternate = "closedLoopEventClient")
    String getClosedLoopEventClient();

    @SerializedName(value = "policyVersion", alternate = "policyVersion")
    String getPolicyVersion();

    @SerializedName(value = "policyName", alternate = "policyName")
    String getPolicyName();

    @SerializedName(value = "policyScope", alternate = "policyScope")
    String getPolicyScope();

    @SerializedName(value = "target_type", alternate = "target_type")
    String getTargetType();

    // It will handle all pieces of information that the microservice needs to send
    // towards Policy
    @SerializedName(value = "AAI", alternate = "AAI")
    Map<String, String> getAaiEnrichmentData();

    @SerializedName(value = "closedLoopAlarmStart", alternate = "closedLoopAlarmStart")
    long getClosedLoopAlarmStart();

    @SerializedName(value = "closedLoopEventStatus", alternate = "closedLoopEventStatus")
    String getClosedLoopEventStatus();

    @SerializedName(value = "closedLoopControlName", alternate = "closedLoopControlName")
    String getClosedLoopControlName();

    @SerializedName(value = "version", alternate = "version")
    String getVersion();

    @SerializedName(value = "target", alternate = "target")
    String getTarget();

    @SerializedName(value = "requestID", alternate = "requestID")
    String getRequestId();

    @SerializedName(value = "from", alternate = "from")
    String getOriginator();
}
