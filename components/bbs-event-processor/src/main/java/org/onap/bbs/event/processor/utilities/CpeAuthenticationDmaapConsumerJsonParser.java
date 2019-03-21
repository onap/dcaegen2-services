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

import static org.onap.bbs.event.processor.utilities.CommonEventFields.COMMON_EVENT_HEADER;
import static org.onap.bbs.event.processor.utilities.CommonEventFields.COMMON_FORMAT;
import static org.onap.bbs.event.processor.utilities.CommonEventFields.CORRELATION_ID;
import static org.onap.bbs.event.processor.utilities.CommonEventFields.EVENT;
import static org.onap.bbs.event.processor.utilities.CommonEventFields.SOURCE_NAME;
import static org.onap.bbs.event.processor.utilities.CpeAuthenticationEventFields.ADDITIONAL_FIELDS;
import static org.onap.bbs.event.processor.utilities.CpeAuthenticationEventFields.NEW_AUTHENTICATION_STATE;
import static org.onap.bbs.event.processor.utilities.CpeAuthenticationEventFields.OLD_AUTHENTICATION_STATE;
import static org.onap.bbs.event.processor.utilities.CpeAuthenticationEventFields.RGW_MAC_ADDRESS;
import static org.onap.bbs.event.processor.utilities.CpeAuthenticationEventFields.STATE_CHANGE_FIELDS;
import static org.onap.bbs.event.processor.utilities.CpeAuthenticationEventFields.STATE_INTERFACE;
import static org.onap.bbs.event.processor.utilities.CpeAuthenticationEventFields.SW_VERSION;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.Optional;
import java.util.stream.StreamSupport;

import org.onap.bbs.event.processor.exceptions.DmaapException;
import org.onap.bbs.event.processor.model.CpeAuthenticationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.ImmutableCpeAuthenticationConsumerDmaapModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CpeAuthenticationDmaapConsumerJsonParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CpeAuthenticationDmaapConsumerJsonParser.class);

    private static final String CPE_AUTHENTICATION_DUMPING_TEMPLATE = "%n{"
            + "\"" + CORRELATION_ID + COMMON_FORMAT + ","
            + "\"" + OLD_AUTHENTICATION_STATE + COMMON_FORMAT + ","
            + "\"" + NEW_AUTHENTICATION_STATE + COMMON_FORMAT + ","
            + "\"" + STATE_INTERFACE + COMMON_FORMAT + ","
            + "\"" + RGW_MAC_ADDRESS + COMMON_FORMAT + ","
            + "\"" + SW_VERSION + COMMON_FORMAT
            + "}";

    private String pnfSourceName;

    private String oldAuthenticationStatus;
    private String newAuthenticationStatus;
    private String stateInterface;
    private String rgwMacAddress;
    private String swVersion;

    /**
     * Translates a response from DMaaP to a reactive {@link CpeAuthenticationConsumerDmaapModel} model.
     * @param dmaapResponse Response from DMaaP
     * @return CPE Authentication Consumer DMaaP reactive model
     */
    public Flux<CpeAuthenticationConsumerDmaapModel> extractModelFromDmaap(Mono<String> dmaapResponse) {
        return dmaapResponse
                .flatMapMany(this::parseToMono)
                .flatMap(this::createTargetFlux);
    }

    private Mono<JsonElement> parseToMono(String message) {
        if (StringUtils.isEmpty(message)) {
            LOGGER.warn("DMaaP response is empty");
            return Mono.empty();
        }
        return Mono.fromCallable(() -> new JsonParser().parse(message))
                .doOnError(e -> e instanceof JsonSyntaxException, e -> LOGGER.error("Invalid JSON. Ignoring"))
                .onErrorResume(e -> e instanceof JsonSyntaxException, e -> Mono.empty());
    }

    private Flux<CpeAuthenticationConsumerDmaapModel> createTargetFlux(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            return doCreateTargetFlux(Flux.defer(() -> Flux.just(jsonElement.getAsJsonObject())));
        }
        return doCreateTargetFlux(
                Flux.defer(() -> Flux.fromStream(StreamSupport.stream(jsonElement.getAsJsonArray().spliterator(), false)
                        .map(jsonElementFromArray -> getJsonObjectFromAnArray(jsonElementFromArray)
                                .orElseGet(JsonObject::new)))));
    }

    private Flux<CpeAuthenticationConsumerDmaapModel> doCreateTargetFlux(Flux<JsonObject> jsonObject) {
        return jsonObject
                .flatMap(this::transform)
                .onErrorResume(exception -> exception instanceof DmaapException, e -> Mono.empty());
    }

    private Mono<CpeAuthenticationConsumerDmaapModel> transform(JsonObject dmaapResponseJsonObject) {

        if (!containsProperHeaders(dmaapResponseJsonObject)) {
            LOGGER.warn("Incorrect CPE Authentication JSON event - missing headers");
            return Mono.empty();
        }

        JsonObject commonEventHeader = dmaapResponseJsonObject.getAsJsonObject(EVENT)
                .getAsJsonObject(COMMON_EVENT_HEADER);
        JsonObject stateChangeFields = dmaapResponseJsonObject.getAsJsonObject(EVENT)
                .getAsJsonObject(STATE_CHANGE_FIELDS);

        pnfSourceName = getValueFromJson(commonEventHeader, SOURCE_NAME);

        oldAuthenticationStatus = getValueFromJson(stateChangeFields, OLD_AUTHENTICATION_STATE);
        newAuthenticationStatus = getValueFromJson(stateChangeFields, NEW_AUTHENTICATION_STATE);
        stateInterface = getValueFromJson(stateChangeFields, STATE_INTERFACE);

        if (stateChangeFields.has(ADDITIONAL_FIELDS)) {
            JsonObject additionalFields = stateChangeFields.getAsJsonObject(ADDITIONAL_FIELDS);
            rgwMacAddress = getValueFromJson(additionalFields, RGW_MAC_ADDRESS);
            swVersion = getValueFromJson(additionalFields, SW_VERSION);
        }

        if (StringUtils.isEmpty(pnfSourceName)
                || authenticationStatusMissing(oldAuthenticationStatus)
                || authenticationStatusMissing(newAuthenticationStatus)) {
            String incorrectEvent = dumpJsonData();
            LOGGER.warn("Incorrect CPE Authentication JSON event: {}", incorrectEvent);
            return Mono.empty();
        }

        return Mono.just(ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(pnfSourceName)
                .oldAuthenticationState(oldAuthenticationStatus)
                .newAuthenticationState(newAuthenticationStatus)
                .stateInterface(stateInterface)
                .rgwMacAddress(rgwMacAddress)
                .swVersion(swVersion)
                .build());
    }

    private boolean authenticationStatusMissing(String authenticationStatus) {
        return StringUtils.isEmpty(authenticationStatus);
    }

    private boolean containsProperHeaders(JsonObject jsonObject) {
        return jsonObject.has(EVENT) && jsonObject.getAsJsonObject(EVENT).has(COMMON_EVENT_HEADER)
                && jsonObject.getAsJsonObject(EVENT).has(STATE_CHANGE_FIELDS);
    }

    private String dumpJsonData() {
        return String.format(CPE_AUTHENTICATION_DUMPING_TEMPLATE,
                pnfSourceName,
                oldAuthenticationStatus,
                newAuthenticationStatus,
                stateInterface,
                rgwMacAddress,
                swVersion
        );
    }

    Optional<JsonObject> getJsonObjectFromAnArray(JsonElement element) {
        JsonParser jsonParser = new JsonParser();
        return element.isJsonPrimitive() ? Optional.of(jsonParser.parse(element.getAsString()).getAsJsonObject())
                : Optional.of(jsonParser.parse(element.toString()).getAsJsonObject());
    }

    private String getValueFromJson(JsonObject jsonObject, String jsonKey) {
        return jsonObject.has(jsonKey) ? jsonObject.get(jsonKey).getAsString() : "";
    }
}
