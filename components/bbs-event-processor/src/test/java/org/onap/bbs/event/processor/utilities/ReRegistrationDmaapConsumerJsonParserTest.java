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

import static org.mockito.Mockito.spy;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.bbs.event.processor.model.ImmutableReRegistrationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.ReRegistrationConsumerDmaapModel;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReRegistrationDmaapConsumerJsonParserTest {

    private static JsonParser jsonParser;

    private static final String RE_REGISTRATION_EVENT_TEMPLATE = "{"
            + "\"correlationId\":\"%s\","
            + "\"additionalFields\": {"
            + " \"attachment-point\": \"%s\","
            + " \"remote-id\": \"%s\","
            + " \"cvlan\": \"%s\","
            + " \"svlan\": \"%s\""
            + "}}";

    private static final String RE_REGISTRATION_EVENT_TEMPLATE_MISSING_ATTACHMENT_POINT = "{"
            + "\"correlationId\":\"%s\","
            + "\"additionalFields\": {"
            + " \"remote-id\": \"%s\","
            + " \"cvlan\": \"%s\","
            + " \"svlan\": \"%s\""
            + "}}";

    private static final String RE_REGISTRATION_EVENT_TEMPLATE_MISSING_CORRELATION_ID = "{"
            + "\"additionalFields\": {"
            + " \"attachment-point\": \"%s\","
            + " \"remote-id\": \"%s\","
            + " \"cvlan\": \"%s\","
            + " \"svlan\": \"%s\""
            + "}}";

    private static final String RE_REGISTRATION_EVENT_TEMPLATE_MISSING_CORRELATION_ID_VALUE = "{"
            + "\"correlationId\":\"\","
            + "\"additionalFields\": {"
            + " \"attachment-point\": \"%s\","
            + " \"remote-id\": \"%s\","
            + " \"cvlan\": \"%s\","
            + " \"svlan\": \"%s\""
            + "}}";

    private static final String RE_REGISTRATION_EVENT_TEMPLATE_MISSING_ADDITIONAL_FIELDS = "{"
            + "\"correlationId\":\"%s\","
            + "\"somethingElse\": {"
            + " \"attachment-point\": \"%s\","
            + " \"remote-id\": \"%s\","
            + " \"cvlan\": \"%s\","
            + " \"svlan\": \"%s\""
            + "}}";

    @BeforeAll
    static void init() {
        jsonParser = new JsonParser();
    }

    @Test
    void passingNonJson_getIllegalStateException() {

        var consumerJsonParser = new ReRegistrationDmaapConsumerJsonParser();
        var jsonReader = new JsonReader(new StringReader("not JSON"));
        jsonReader.setLenient(true);
        JsonElement notJson = jsonParser.parse(jsonReader).getAsJsonPrimitive();
        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just(notJson)))
                .expectSubscription()
                .verifyError(IllegalStateException.class);
    }

    @Test
    void passingNoEvents_EmptyFluxIsReturned() {

        var consumerJsonParser = new ReRegistrationDmaapConsumerJsonParser();
        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just(jsonParser.parse("[]"))))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void passingOneCorrectEvent_validationSucceeds() {

        var correlationId = "PNF-CorrelationId";
        var attachmentPoint = "olt1/1/1";
        var remoteId = "remoteId";
        var cvlan = "1005";
        var svlan = "100";

        var event = String.format(RE_REGISTRATION_EVENT_TEMPLATE, correlationId, attachmentPoint,
                remoteId, cvlan, svlan);

        var consumerJsonParser = spy(new ReRegistrationDmaapConsumerJsonParser());
        var jsonElement = jsonParser.parse(event);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(consumerJsonParser).getJsonObjectFromAnArray(jsonElement);

        var eventsArray = "[" + event + "]";

        ReRegistrationConsumerDmaapModel expectedEventObject = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(correlationId)
                .attachmentPoint(attachmentPoint)
                .remoteId(remoteId)
                .cVlan(cvlan)
                .sVlan(svlan)
                .build();

        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just(jsonParser.parse(eventsArray))))
                .expectSubscription()
                .expectNext(expectedEventObject);
    }

    @Test
    void passingTwoCorrectEvents_validationSucceeds() {

        var correlationId1 = "PNF-CorrelationId1";
        var correlationId2 = "PNF-CorrelationId2";
        var attachmentPoint1 = "olt1/1/1";
        var attachmentPoint2 = "olt2/2/2";
        var remoteId1 = "remoteId1";
        var remoteId2 = "remoteId2";
        var cvlan = "1005";
        var svlan = "100";

        var firstEvent = String.format(RE_REGISTRATION_EVENT_TEMPLATE, correlationId1, attachmentPoint1,
                remoteId1, cvlan, svlan);
        var secondEvent = String.format(RE_REGISTRATION_EVENT_TEMPLATE, correlationId1, attachmentPoint1,
                remoteId1, cvlan, svlan);

        var consumerJsonParser = spy(new ReRegistrationDmaapConsumerJsonParser());
        var jsonElement1 = jsonParser.parse(firstEvent);
        Mockito.doReturn(Optional.of(jsonElement1.getAsJsonObject()))
                .when(consumerJsonParser).getJsonObjectFromAnArray(jsonElement1);
        var jsonElement2 = jsonParser.parse(secondEvent);
        Mockito.doReturn(Optional.of(jsonElement2.getAsJsonObject()))
                .when(consumerJsonParser).getJsonObjectFromAnArray(jsonElement2);

        var eventsArray = "[" + firstEvent + "," + secondEvent + "]";

        ReRegistrationConsumerDmaapModel expectedFirstEventObject = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(correlationId1)
                .attachmentPoint(attachmentPoint1)
                .remoteId(remoteId1)
                .cVlan(cvlan)
                .sVlan(svlan)
                .build();
        ReRegistrationConsumerDmaapModel expectedSecondEventObject = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(correlationId2)
                .attachmentPoint(attachmentPoint2)
                .remoteId(remoteId2)
                .cVlan(cvlan)
                .sVlan(svlan)
                .build();

        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just(jsonParser.parse(eventsArray))))
                .expectSubscription()
                .expectNext(expectedFirstEventObject)
                .expectNext(expectedSecondEventObject);
    }

    @Test
    void passingJsonWithMissingAttachmentPoint_validationFails() {

        var correlationId = "PNF-CorrelationId";
        var remoteId = "remoteId";
        var cvlan = "1005";
        var svlan = "100";

        var event = String.format(RE_REGISTRATION_EVENT_TEMPLATE_MISSING_ATTACHMENT_POINT,
                correlationId,
                remoteId,
                cvlan,
                svlan);

        var consumerJsonParser = spy(new ReRegistrationDmaapConsumerJsonParser());
        var jsonElement = jsonParser.parse(event);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(consumerJsonParser).getJsonObjectFromAnArray(jsonElement);

        var eventsArray = "[" + event + "]";

        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just(jsonParser.parse(eventsArray))))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void passingJsonWithMissingCorrelationId_validationFails() {

        var attachmentPoint = "olt1/1/1";
        var remoteId = "remoteId";
        var cvlan = "1005";
        var svlan = "100";

        var event = String.format(RE_REGISTRATION_EVENT_TEMPLATE_MISSING_CORRELATION_ID,
                attachmentPoint,
                remoteId,
                cvlan,
                svlan);

        var consumerJsonParser = spy(new ReRegistrationDmaapConsumerJsonParser());
        var jsonElement = jsonParser.parse(event);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(consumerJsonParser).getJsonObjectFromAnArray(jsonElement);

        var eventsArray = "[" + event + "]";

        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just(jsonParser.parse(eventsArray))))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void passingJsonWithMissingCorrelationIdValue_validationFails() {

        var attachmentPoint = "olt1/1/1";
        var remoteId = "remoteId";
        var cvlan = "1005";
        var svlan = "100";

        var event = String.format(RE_REGISTRATION_EVENT_TEMPLATE_MISSING_CORRELATION_ID_VALUE,
                attachmentPoint,
                remoteId,
                cvlan,
                svlan);

        var consumerJsonParser = spy(new ReRegistrationDmaapConsumerJsonParser());
        var jsonElement = jsonParser.parse(event);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(consumerJsonParser).getJsonObjectFromAnArray(jsonElement);

        var eventsArray = "[" + event + "]";

        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just(jsonParser.parse(eventsArray))))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void passingJsonWithMissingAdditionalFields_validationFails() {

        var correlationId = "PNF-CorrelationId";
        var attachmentPoint = "olt1/1/1";
        var remoteId = "remoteId";
        var cvlan = "1005";
        var svlan = "100";

        var event = String.format(RE_REGISTRATION_EVENT_TEMPLATE_MISSING_ADDITIONAL_FIELDS,
                correlationId,
                attachmentPoint,
                remoteId,
                cvlan,
                svlan);

        var consumerJsonParser = spy(new ReRegistrationDmaapConsumerJsonParser());
        var jsonElement = jsonParser.parse(event);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(consumerJsonParser).getJsonObjectFromAnArray(jsonElement);

        var eventsArray = "[" + event + "]";

        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just(jsonParser.parse(eventsArray))))
                .expectSubscription()
                .verifyComplete();
    }

}