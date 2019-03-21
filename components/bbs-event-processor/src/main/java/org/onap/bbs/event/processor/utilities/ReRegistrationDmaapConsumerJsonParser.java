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

import static org.onap.bbs.event.processor.utilities.CommonEventFields.COMMON_FORMAT;
import static org.onap.bbs.event.processor.utilities.CommonEventFields.CORRELATION_ID;
import static org.onap.bbs.event.processor.utilities.ReRegistrationEventFields.ADDITIONAL_FIELDS;
import static org.onap.bbs.event.processor.utilities.ReRegistrationEventFields.ATTACHMENT_POINT;
import static org.onap.bbs.event.processor.utilities.ReRegistrationEventFields.CVLAN;
import static org.onap.bbs.event.processor.utilities.ReRegistrationEventFields.REMOTE_ID;
import static org.onap.bbs.event.processor.utilities.ReRegistrationEventFields.SVLAN;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.Optional;
import java.util.stream.StreamSupport;

import org.onap.bbs.event.processor.exceptions.DmaapException;
import org.onap.bbs.event.processor.model.ImmutableReRegistrationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.ReRegistrationConsumerDmaapModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReRegistrationDmaapConsumerJsonParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReRegistrationDmaapConsumerJsonParser.class);

    private static final String RE_REGISTRATION_DUMPING_TEMPLATE = "%n{"
            + "\"" + CORRELATION_ID + COMMON_FORMAT + ","
            + "\"" + ATTACHMENT_POINT + COMMON_FORMAT + ","
            + "\"" + REMOTE_ID + COMMON_FORMAT + ","
            + "\"" + CVLAN + COMMON_FORMAT + ","
            + "\"" + SVLAN + COMMON_FORMAT
            + "}";

    private String pnfCorrelationId;

    private String attachmentPoint;
    private String remoteId;
    private String cvlan;
    private String svlan;

    /**
     * Translates a response from DMaaP to a reactive {@link ReRegistrationConsumerDmaapModel} model
     * @param dmaapResponse Response from DMaaP
     * @return Re-Registration Consumer DMaaP reactive model
     */
    public Flux<ReRegistrationConsumerDmaapModel> extractModelFromDmaap(Mono<String> dmaapResponse) {
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

    private Flux<ReRegistrationConsumerDmaapModel> createTargetFlux(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            return doCreateTargetFlux(Flux.defer(() -> Flux.just(jsonElement.getAsJsonObject())));
        }
        return doCreateTargetFlux(
                Flux.defer(() -> Flux.fromStream(StreamSupport.stream(jsonElement.getAsJsonArray().spliterator(), false)
                        .map(jsonElementFromArray -> getJsonObjectFromAnArray(jsonElementFromArray)
                                .orElseGet(JsonObject::new)))));
    }

    private Flux<ReRegistrationConsumerDmaapModel> doCreateTargetFlux(Flux<JsonObject> jsonObject) {
        return jsonObject
                .flatMap(this::transform)
                .onErrorResume(exception -> exception instanceof DmaapException, e -> Mono.empty());
    }

    private Mono<ReRegistrationConsumerDmaapModel> transform(JsonObject dmaapResponseJsonObject) {

        if (!containsProperHeaders(dmaapResponseJsonObject)) {
            LOGGER.warn("Incorrect JsonObject - missing headers");
            return Mono.empty();
        }

        JsonObject pnfReRegistrationFields =
                dmaapResponseJsonObject.getAsJsonObject(ADDITIONAL_FIELDS);

        pnfCorrelationId = getValueFromJson(dmaapResponseJsonObject, CORRELATION_ID);

        attachmentPoint = getValueFromJson(pnfReRegistrationFields, ATTACHMENT_POINT);
        remoteId = getValueFromJson(pnfReRegistrationFields, REMOTE_ID);
        cvlan = getValueFromJson(pnfReRegistrationFields, CVLAN);
        svlan = getValueFromJson(pnfReRegistrationFields, SVLAN);

        if (StringUtils.isEmpty(pnfCorrelationId) || anyImportantPropertyMissing()) {
            String incorrectEvent = dumpJsonData();
            LOGGER.warn("Incorrect Re-Registration JSON event: {}", incorrectEvent);
            return Mono.empty();
        }

        return Mono.just(ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(pnfCorrelationId)
                .attachmentPoint(attachmentPoint)
                .remoteId(remoteId)
                .cVlan(cvlan)
                .sVlan(svlan)
                .build());
    }

    private boolean anyImportantPropertyMissing() {
        return StringUtils.isEmpty(attachmentPoint)
                || StringUtils.isEmpty(remoteId)
                || StringUtils.isEmpty(cvlan)
                || StringUtils.isEmpty(svlan);
    }

    private boolean containsProperHeaders(JsonObject jsonObject) {
        return jsonObject.has(ADDITIONAL_FIELDS);
    }

    private String dumpJsonData() {
        return String.format(RE_REGISTRATION_DUMPING_TEMPLATE,
                pnfCorrelationId,
                attachmentPoint,
                remoteId,
                cvlan,
                svlan
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
