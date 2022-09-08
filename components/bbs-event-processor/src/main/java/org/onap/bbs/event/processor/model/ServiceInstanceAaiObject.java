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

import java.util.Optional;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters(fieldNamingStrategy = true, emptyAsNulls = true)
public interface ServiceInstanceAaiObject {

    @SerializedName(value = "service-instance-id", alternate = "service-instance-id")
    String getServiceInstanceId();

    @SerializedName(value = "orchestration-status", alternate = "orchestration-status")
    Optional<String> getOrchestrationStatus();

    @SerializedName(value = "relationship-list", alternate = "relationship-list")
    RelationshipListAaiObject getRelationshipListAaiObject();

    @SerializedName(value = "metadata", alternate = "metadata")
    Optional<MetadataListAaiObject> getMetadataListAaiObject();
}
