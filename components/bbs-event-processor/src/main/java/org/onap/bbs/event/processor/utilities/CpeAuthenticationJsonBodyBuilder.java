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
import com.google.gson.TypeAdapterFactory;

import java.util.ServiceLoader;

import org.onap.bbs.event.processor.model.CpeAuthenticationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.ImmutableCpeAuthenticationConsumerDmaapModel;

public class CpeAuthenticationJsonBodyBuilder {

    /**
     * Serialize the CPE authentication DMaaP model with GSON.
     * @param cpeAuthenticationConsumerDmaapModel object to be serialized
     * @return String output of serialization
     */
    public String createJsonBody(CpeAuthenticationConsumerDmaapModel cpeAuthenticationConsumerDmaapModel) {
        var gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        return gsonBuilder.create().toJson(ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(cpeAuthenticationConsumerDmaapModel.getCorrelationId())
                .oldAuthenticationState(cpeAuthenticationConsumerDmaapModel.getOldAuthenticationState())
                .newAuthenticationState(cpeAuthenticationConsumerDmaapModel.getNewAuthenticationState())
                .stateInterface(cpeAuthenticationConsumerDmaapModel.getStateInterface().orElse(""))
                .rgwMacAddress(cpeAuthenticationConsumerDmaapModel.getRgwMacAddress().orElse(""))
                .swVersion(cpeAuthenticationConsumerDmaapModel.getSwVersion().orElse(""))
                .build());
    }
}
