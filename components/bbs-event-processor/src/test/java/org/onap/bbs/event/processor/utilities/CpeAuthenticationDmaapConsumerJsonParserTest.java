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

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.bbs.event.processor.model.CpeAuthenticationConsumerDmaapModel;
import org.onap.bbs.event.processor.model.ImmutableCpeAuthenticationConsumerDmaapModel;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CpeAuthenticationDmaapConsumerJsonParserTest {

    private static JsonParser jsonParser;

    private static final String CPE_AUTHENTICATION_EVENT_TEMPLATE = "{\"event\": {"
            + "\"commonEventHeader\": { \"sourceName\":\"%s\"},"
            + "\"stateChangeFields\": {"
            + " \"oldState\": \"%s\","
            + " \"newState\": \"%s\","
            + " \"stateInterface\": \"%s\","
            + " \"additionalFields\": {"
            + "   \"macAddress\": \"%s\","
            + "   \"swVersion\": \"%s\""
            + "}}}}";

    private static final String CPE_AUTHENTICATION_EVENT_TEMPLATE_WITH_MISSING_AUTHENTICATION_STATE = "{\"event\": {"
            + "\"commonEventHeader\": { \"sourceName\":\"%s\"},"
            + "\"stateChangeFields\": {"
            + " \"oldState\": \"%s\","
            + " \"stateInterface\": \"%s\","
            + " \"additionalFields\": {"
            + "   \"macAddress\": \"%s\","
            + "   \"swVersion\": \"%s\""
            + "}}}}";

    private static final String CPE_AUTHENTICATION_EVENT_TEMPLATE_WITH_MISSING_SOURCE_NAME = "{\"event\": {"
            + "\"commonEventHeader\": { },"
            + "\"stateChangeFields\": {"
            + " \"oldState\": \"%s\","
            + " \"newState\": \"%s\","
            + " \"stateInterface\": \"%s\","
            + " \"additionalFields\": {"
            + "   \"macAddress\": \"%s\","
            + "   \"swVersion\": \"%s\""
            + "}}}}";

    private static final String CPE_AUTHENTICATION_EVENT_TEMPLATE_WITH_MISSING_SOURCE_NAME_VALUE = "{\"event\": {"
            + "\"commonEventHeader\": { \"sourceName\":\"\"},"
            + "\"stateChangeFields\": {"
            + " \"oldState\": \"%s\","
            + " \"newState\": \"%s\","
            + " \"stateInterface\": \"%s\","
            + " \"additionalFields\": {"
            + "   \"macAddress\": \"%s\","
            + "   \"swVersion\": \"%s\""
            + "}}}}";

    private static final String CPE_AUTHENTICATION_EVENT_TEMPLATE_WITH_MISSING_STATE_CHANGE_FIELDS = "{\"event\": {"
            + "\"commonEventHeader\": { \"sourceName\":\"\"},"
            + "\"somethingElse\": {"
            + " \"oldState\": \"%s\","
            + " \"newState\": \"%s\","
            + " \"stateInterface\": \"%s\","
            + " \"additionalFields\": {"
            + "   \"macAddress\": \"%s\","
            + "   \"swVersion\": \"%s\""
            + "}}}}";

    @BeforeAll
    static void init() {
        jsonParser = new JsonParser();
    }

    @Test
    void passingNonJson_EmptyFluxIsReturned() {

        CpeAuthenticationDmaapConsumerJsonParser consumerJsonParser =
                new CpeAuthenticationDmaapConsumerJsonParser();

        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just("not JSON")))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void passingNoEvents_EmptyFluxIsReturned() {

        CpeAuthenticationDmaapConsumerJsonParser consumerJsonParser =
                new CpeAuthenticationDmaapConsumerJsonParser();

        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just("[]")))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void passingOneCorrectEvent_validationSucceeds() {

        String sourceName = "PNF-CorrelationId";
        String oldAuthenticationState = "outOfService";
        String newAuthenticationState = "inService";
        String stateInterface = "stateInterface";
        String rgwMacAddress = "00:0a:95:8d:78:16";
        String swVersion = "1.2";

        String event = String.format(CPE_AUTHENTICATION_EVENT_TEMPLATE, sourceName, oldAuthenticationState,
                newAuthenticationState, stateInterface, rgwMacAddress, swVersion);

        CpeAuthenticationDmaapConsumerJsonParser consumerJsonParser =
                spy(new CpeAuthenticationDmaapConsumerJsonParser());
        JsonElement jsonElement = jsonParser.parse(event);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(consumerJsonParser).getJsonObjectFromAnArray(jsonElement);

        String eventsArray = "[" + event + "]";

        CpeAuthenticationConsumerDmaapModel expectedEventObject = ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(sourceName)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rGWMacAddress(rgwMacAddress)
                .swVersion(swVersion)
                .build();

        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just(eventsArray)))
                .expectSubscription()
                .expectNext(expectedEventObject);
    }

    @Test
    void passingTwoCorrectEvents_validationSucceeds() {

        String sourceName1 = "PNF-CorrelationId";
        String sourceName2 = "PNF-CorrelationId";
        String oldAuthenticationState = "outOfService";
        String newAuthenticationState = "inService";
        String stateInterface = "stateInterface";
        String rgwMacAddress1 = "00:0a:95:8d:78:16";
        String rgwMacAddress2 = "00:0a:95:8d:78:17";
        String swVersion = "1.2";

        String firstEvent = String.format(CPE_AUTHENTICATION_EVENT_TEMPLATE, sourceName1, oldAuthenticationState,
                newAuthenticationState, stateInterface, rgwMacAddress1, swVersion);
        String secondEvent = String.format(CPE_AUTHENTICATION_EVENT_TEMPLATE, sourceName2, oldAuthenticationState,
                newAuthenticationState, stateInterface, rgwMacAddress2, swVersion);

        CpeAuthenticationDmaapConsumerJsonParser consumerJsonParser =
                spy(new CpeAuthenticationDmaapConsumerJsonParser());
        JsonElement jsonElement1 = jsonParser.parse(firstEvent);
        Mockito.doReturn(Optional.of(jsonElement1.getAsJsonObject()))
                .when(consumerJsonParser).getJsonObjectFromAnArray(jsonElement1);
        JsonElement jsonElement2 = jsonParser.parse(secondEvent);
        Mockito.doReturn(Optional.of(jsonElement2.getAsJsonObject()))
                .when(consumerJsonParser).getJsonObjectFromAnArray(jsonElement2);

        String eventsArray = "[" + firstEvent + secondEvent + "]";

        CpeAuthenticationConsumerDmaapModel expectedFirstEventObject =
                ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(sourceName1)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rGWMacAddress(rgwMacAddress1)
                .swVersion(swVersion)
                .build();
        CpeAuthenticationConsumerDmaapModel expectedSecondEventObject =
                ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(sourceName2)
                .oldAuthenticationState(oldAuthenticationState)
                .newAuthenticationState(newAuthenticationState)
                .stateInterface(stateInterface)
                .rGWMacAddress(rgwMacAddress2)
                .swVersion(swVersion)
                .build();

        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just(eventsArray)))
                .expectSubscription()
                .expectNext(expectedFirstEventObject)
                .expectNext(expectedSecondEventObject);
    }

    @Test
    void passingJsonWithMissingAuthenticationState_validationFails() {

        String sourceName = "PNF-CorrelationId";
        String oldAuthenticationState = "outOfService";
        String stateInterface = "stateInterface";
        String rgwMacAddress = "00:0a:95:8d:78:16";
        String swVersion = "1.2";

        String event = String.format(CPE_AUTHENTICATION_EVENT_TEMPLATE_WITH_MISSING_AUTHENTICATION_STATE, sourceName,
                oldAuthenticationState, stateInterface, rgwMacAddress, swVersion);

        CpeAuthenticationDmaapConsumerJsonParser consumerJsonParser =
                spy(new CpeAuthenticationDmaapConsumerJsonParser());
        JsonElement jsonElement = jsonParser.parse(event);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(consumerJsonParser).getJsonObjectFromAnArray(jsonElement);

        String eventsArray = "[" + event + "]";

        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just(eventsArray)))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void passingJsonWithMissingSourceName_validationFails() {

        String oldAuthenticationState = "outOfService";
        String newAuthenticationState = "inService";
        String stateInterface = "stateInterface";
        String rgwMacAddress = "00:0a:95:8d:78:16";
        String swVersion = "1.2";

        String event = String.format(CPE_AUTHENTICATION_EVENT_TEMPLATE_WITH_MISSING_SOURCE_NAME,
                oldAuthenticationState, newAuthenticationState, stateInterface, rgwMacAddress, swVersion);

        CpeAuthenticationDmaapConsumerJsonParser consumerJsonParser =
                spy(new CpeAuthenticationDmaapConsumerJsonParser());
        JsonElement jsonElement = jsonParser.parse(event);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(consumerJsonParser).getJsonObjectFromAnArray(jsonElement);

        String eventsArray = "[" + event + "]";

        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just(eventsArray)))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void passingJsonWithMissingSourceNameValue_validationFails() {

        String oldAuthenticationState = "outOfService";
        String newAuthenticationState = "inService";
        String stateInterface = "stateInterface";
        String rgwMacAddress = "00:0a:95:8d:78:16";
        String swVersion = "1.2";

        String event = String.format(CPE_AUTHENTICATION_EVENT_TEMPLATE_WITH_MISSING_SOURCE_NAME_VALUE,
                oldAuthenticationState, newAuthenticationState, stateInterface, rgwMacAddress, swVersion);

        CpeAuthenticationDmaapConsumerJsonParser consumerJsonParser =
                spy(new CpeAuthenticationDmaapConsumerJsonParser());
        JsonElement jsonElement = jsonParser.parse(event);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(consumerJsonParser).getJsonObjectFromAnArray(jsonElement);

        String eventsArray = "[" + event + "]";

        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just(eventsArray)))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void passingJsonWithMissingStateChangeFieldsHeader_validationFails() {

        String oldAuthenticationState = "outOfService";
        String newAuthenticationState = "inService";
        String stateInterface = "stateInterface";
        String rgwMacAddress = "00:0a:95:8d:78:16";
        String swVersion = "1.2";

        String event = String.format(CPE_AUTHENTICATION_EVENT_TEMPLATE_WITH_MISSING_STATE_CHANGE_FIELDS,
                oldAuthenticationState, newAuthenticationState, stateInterface, rgwMacAddress, swVersion);

        CpeAuthenticationDmaapConsumerJsonParser consumerJsonParser =
                spy(new CpeAuthenticationDmaapConsumerJsonParser());
        JsonElement jsonElement = jsonParser.parse(event);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(consumerJsonParser).getJsonObjectFromAnArray(jsonElement);

        String eventsArray = "[" + event + "]";

        StepVerifier.create(consumerJsonParser.extractModelFromDmaap(Mono.just(eventsArray)))
                .expectSubscription()
                .verifyComplete();
    }
}