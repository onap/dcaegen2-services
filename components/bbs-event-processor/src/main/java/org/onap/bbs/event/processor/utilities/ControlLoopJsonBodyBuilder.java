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

package org.onap.bbs.event.processor.utilities;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapterFactory;

import java.util.ServiceLoader;

import org.onap.bbs.event.processor.model.ControlLoopPublisherDmaapModel;
import org.onap.bbs.event.processor.model.ImmutableControlLoopPublisherDmaapModel;

public class ControlLoopJsonBodyBuilder {

    /**
     * Serialize the Control Loop DMaaP model with GSON.
     * @param publisherDmaapModel object to be serialized
     * @return String output of serialization
     */
    public String createJsonBody(ControlLoopPublisherDmaapModel publisherDmaapModel) {
        var gsonBuilder = new GsonBuilder().disableHtmlEscaping();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        return gsonBuilder.create().toJson(ImmutableControlLoopPublisherDmaapModel.builder()
                .closedLoopEventClient(publisherDmaapModel.getClosedLoopEventClient())
                .policyVersion(publisherDmaapModel.getPolicyVersion())
                .policyName(publisherDmaapModel.getPolicyName())
                .policyScope(publisherDmaapModel.getPolicyScope())
                .targetType(publisherDmaapModel.getTargetType())
                .aaiEnrichmentData(publisherDmaapModel.getAaiEnrichmentData())
                .closedLoopAlarmStart(publisherDmaapModel.getClosedLoopAlarmStart())
                .closedLoopEventStatus(publisherDmaapModel.getClosedLoopEventStatus())
                .closedLoopControlName(publisherDmaapModel.getClosedLoopControlName())
                .version(publisherDmaapModel.getVersion())
                .target(publisherDmaapModel.getTarget())
                .requestId(publisherDmaapModel.getRequestId())
                .originator(publisherDmaapModel.getOriginator())
                .build());
    }

    /**
     * Serialize the Control Loop DMaaP model with GSON.
     * @param publisherDmaapModel object to be serialized
     * @return String output of serialization
     */
    public static JsonElement createAsJsonElement(ControlLoopPublisherDmaapModel publisherDmaapModel) {
        var gsonBuilder = new GsonBuilder().disableHtmlEscaping();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        return gsonBuilder.create().toJsonTree(ImmutableControlLoopPublisherDmaapModel.builder()
                .closedLoopEventClient(publisherDmaapModel.getClosedLoopEventClient())
                .policyVersion(publisherDmaapModel.getPolicyVersion())
                .policyName(publisherDmaapModel.getPolicyName())
                .policyScope(publisherDmaapModel.getPolicyScope())
                .targetType(publisherDmaapModel.getTargetType())
                .aaiEnrichmentData(publisherDmaapModel.getAaiEnrichmentData())
                .closedLoopAlarmStart(publisherDmaapModel.getClosedLoopAlarmStart())
                .closedLoopEventStatus(publisherDmaapModel.getClosedLoopEventStatus())
                .closedLoopControlName(publisherDmaapModel.getClosedLoopControlName())
                .version(publisherDmaapModel.getVersion())
                .target(publisherDmaapModel.getTarget())
                .requestId(publisherDmaapModel.getRequestId())
                .originator(publisherDmaapModel.getOriginator())
                .build());
    }

}
