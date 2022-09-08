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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.junit.jupiter.api.Test;
import org.onap.bbs.event.processor.utilities.ControlLoopJsonBodyBuilder;
import org.onap.bbs.event.processor.utilities.CpeAuthenticationJsonBodyBuilder;
import org.onap.bbs.event.processor.utilities.ReRegistrationJsonBodyBuilder;

class GsonSerializationsTest {

    @Test
    void creatingReRegistrationJsonBody_returnsJsonInString() {

        var correlationId = "NokiaCorrelationId";
        var attachmentPoint = "olt2/1/1";
        var remoteId = "RemoteId";
        var cvlan = "1005";
        var svlan = "100";

        var template = "{"
                + "\"correlationId\":\"%s\","
                + "\"attachment-point\":\"%s\","
                + "\"remote-id\":\"%s\","
                + "\"cvlan\":\"%s\","
                + "\"svlan\":\"%s\""
                + "}";

        ReRegistrationConsumerDmaapModel model = ImmutableReRegistrationConsumerDmaapModel.builder()
                .correlationId(correlationId)
                .attachmentPoint(attachmentPoint)
                .remoteId(remoteId)
                .cVlan(cvlan)
                .sVlan(svlan)
                .build();


        var expectedResult = String.format(template, correlationId, attachmentPoint, remoteId, cvlan, svlan);

        assertEquals(expectedResult, new ReRegistrationJsonBodyBuilder().createJsonBody(model));
    }

    @Test
    void creatingCpeAuthenticationJsonBody_returnsJsonInString() {

        var correlationId = "NokiaCorrelationID";
        var oldAuthenticationState = AuthenticationState.IN_SERVICE;
        var newAuthenticationState = AuthenticationState.OUT_OF_SERVICE;
        var stateInterface = "stateInterface";
        var rgwMacAddress = "00:0a:95:8d:78:16";
        var swVersion = "1.2";

        var template = "{"
                + "\"correlationId\":\"%s\","
                + "\"old-authentication-state\":\"%s\","
                + "\"new-authentication-state\":\"%s\","
                + "\"state-interface\":\"%s\","
                + "\"rgw-mac-address\":\"%s\","
                + "\"sw-version\":\"%s\""
                + "}";

        CpeAuthenticationConsumerDmaapModel model = ImmutableCpeAuthenticationConsumerDmaapModel.builder()
                .correlationId(correlationId)
                .oldAuthenticationState(oldAuthenticationState.getNameInOnap())
                .newAuthenticationState(newAuthenticationState.getNameInOnap())
                .stateInterface(stateInterface)
                .rgwMacAddress(rgwMacAddress)
                .swVersion(swVersion)
                .build();


        var expectedResult = String.format(template, correlationId, oldAuthenticationState.getNameInOnap(),
                newAuthenticationState.getNameInOnap(), stateInterface, rgwMacAddress, swVersion);

        assertEquals(expectedResult, new CpeAuthenticationJsonBodyBuilder().createJsonBody(model));
    }

    @Test
    void creatingDcaeControlLoopJsonBody_returnsJsonInString() {

        var closedLoopEventClient = "DCAE.BBS_mSInstance";
        var policyVersion = "1.0.0.5";
        var policyName = "CPE_Authentication";
        var policyScope =
                "service=HSIAService,type=SampleType,"
                        + "closedLoopControlName=CL-CPE_A-d925ed73-8231-4d02-9545-db4e101f88f8";
        var targetType = "VM";
        var closedLoopAlarmStart = 1484677482204798L;
        var closedLoopEventStatus = "ONSET";
        var closedLoopControlName = "ControlLoop-CPE_A-2179b738-fd36-4843-a71a-a8c24c70c88b";
        var version = "1.0.2";
        var target = "vserver.vserver-name";
        var requestId = "97964e10-686e-4790-8c45-bdfa61df770f";
        var from = "DCAE";

        Map<String, String> aaiEnrichmentData = new LinkedHashMap<>();
        aaiEnrichmentData.put("service-information.service-instance-id", "service-instance-id-example");
        aaiEnrichmentData.put("cvlan-id", "example cvlan-id");
        aaiEnrichmentData.put("svlan-id", "example svlan-id");

        var template = "{"
                + "\"closedLoopEventClient\":\"%s\","
                + "\"policyVersion\":\"%s\","
                + "\"policyName\":\"%s\","
                + "\"policyScope\":\"%s\","
                + "\"target_type\":\"%s\","
                + "\"AAI\":{"
                + "\"service-information.service-instance-id\":\"service-instance-id-example\","
                + "\"cvlan-id\":\"example cvlan-id\","
                + "\"svlan-id\":\"example svlan-id\""
                + "},"
                + "\"closedLoopAlarmStart\":%s,"
                + "\"closedLoopEventStatus\":\"%s\","
                + "\"closedLoopControlName\":\"%s\","
                + "\"version\":\"%s\","
                + "\"target\":\"%s\","
                + "\"requestID\":\"%s\","
                + "\"from\":\"%s\""
                + "}";


        ControlLoopPublisherDmaapModel model = ImmutableControlLoopPublisherDmaapModel.builder()
                .closedLoopEventClient(closedLoopEventClient)
                .policyVersion(policyVersion)
                .policyName(policyName)
                .policyScope(policyScope)
                .targetType(targetType)
                .aaiEnrichmentData(aaiEnrichmentData)
                .closedLoopAlarmStart(closedLoopAlarmStart)
                .closedLoopEventStatus(closedLoopEventStatus)
                .closedLoopControlName(closedLoopControlName)
                .version(version)
                .target(target)
                .requestId(requestId)
                .originator(from)
                .build();

        var expectedResult = String.format(template,
                closedLoopEventClient,
                policyVersion,
                policyName,
                policyScope,
                targetType,
                closedLoopAlarmStart,
                closedLoopEventStatus,
                closedLoopControlName,
                version,
                target,
                requestId,
                from);

        assertEquals(expectedResult, new ControlLoopJsonBodyBuilder().createJsonBody(model));
    }

    @Test
    void pnfAaiObject_IsSerializedSuccessfully() {

        var gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        var gson = gsonBuilder.create();

        var pnfName = "NokiaCorrelationID";
        var swVersion = "1.2";

        var template = "{"
                + "\"pnf-name\":\"%s\","
                + "\"in-maint\":true,"
                + "\"sw-version\":\"%s\","
                + "\"relationship-list\":{"
                + "\"relationship\":["
                + "{"
                + "\"related-to\":\"service-instance\","
                + "\"relationship-label\":\"org.onap.relationships.inventory.ComposedOf\","
                + "\"related-link\":\"/aai/v14/business/customers/customer/Demonstration/service-subscriptions"
                + "/service-subscription/BBS/service-instances/service-instance/84003b26-6b76-4c75-b805-7b14ab4ffaef\","
                + "\"relationship-data\":["
                + "{"
                + "\"relationship-key\":\"customer.global-customer-id\","
                + "\"relationship-value\":\"Demonstration\""
                + "},"
                + "{"
                + "\"relationship-key\":\"service-subscription.service-type\","
                + "\"relationship-value\":\"BBS\""
                + "},"
                + "{"
                + "\"relationship-key\":\"service-instance.service-instance-id\","
                + "\"relationship-value\":\"84003b26-6b76-4c75-b805-7b14ab4ffaef\""
                + "}"
                + "],"
                + "\"related-to-property\":["
                + "{"
                + "\"property-key\":\"service-instance.service-instance-name\","
                + "\"property-value\":\"bbs-instance\""
                + "}"
                + "]"
                + "},"
                + "{"
                + "\"related-to\":\"platform\","
                + "\"relationship-label\":\"org.onap.relationships.inventory.Uses\","
                + "\"related-link\":\"/aai/v14/business/platforms/platform/bbs-platform\","
                + "\"relationship-data\":["
                + "{"
                + "\"relationship-key\":\"platform.platform-name\","
                + "\"relationship-value\":\"bbs-platform\""
                + "}"
                + "]"
                + "}"
                + "]"
                + "}"
                + "}";

        // Build Relationship Data
        RelationshipListAaiObject.RelationshipEntryAaiObject firstRelationshipEntry =
                ImmutableRelationshipEntryAaiObject.builder()
                        .relatedTo("service-instance")
                        .relatedLink("/aai/v14/business/customers/customer/Demonstration/service-subscriptions"
                            + "/service-subscription/BBS/service-instances"
                            + "/service-instance/84003b26-6b76-4c75-b805-7b14ab4ffaef")
                        .relationshipLabel("org.onap.relationships.inventory.ComposedOf")
                        .relationshipData(Arrays.asList(
                            ImmutableRelationshipDataEntryAaiObject.builder()
                                .relationshipKey("customer.global-customer-id")
                                .relationshipValue("Demonstration").build(),
                            ImmutableRelationshipDataEntryAaiObject.builder()
                                .relationshipKey("service-subscription.service-type")
                                .relationshipValue("BBS").build(),
                            ImmutableRelationshipDataEntryAaiObject.builder()
                                .relationshipKey("service-instance.service-instance-id")
                                .relationshipValue("84003b26-6b76-4c75-b805-7b14ab4ffaef").build())
                )
                        .relatedToProperties(Collections.singletonList(
                                ImmutablePropertyAaiObject.builder()
                                        .propertyKey("service-instance.service-instance-name")
                                        .propertyValue("bbs-instance").build())
                        )
                .build();

        RelationshipListAaiObject.RelationshipEntryAaiObject secondRelationshipEntry =
                ImmutableRelationshipEntryAaiObject.builder()
                .relatedTo("platform")
                .relatedLink("/aai/v14/business/platforms/platform/bbs-platform")
                .relationshipLabel("org.onap.relationships.inventory.Uses")
                .relationshipData(Collections.singletonList(ImmutableRelationshipDataEntryAaiObject.builder()
                        .relationshipKey("platform.platform-name")
                        .relationshipValue("bbs-platform").build()))
                .build();

        RelationshipListAaiObject relationshipListAaiObject = ImmutableRelationshipListAaiObject.builder()
                .relationshipEntries(Arrays.asList(firstRelationshipEntry, secondRelationshipEntry))
                .build();

        // Finally construct PNF object data
        PnfAaiObject pnfAaiObject = ImmutablePnfAaiObject.builder()
                .pnfName(pnfName)
                .isInMaintenance(true)
                .swVersion(swVersion)
                .relationshipListAaiObject(relationshipListAaiObject)
                .build();


        var jsonPnfObject = String.format(template, pnfName, swVersion);

        assertEquals(jsonPnfObject, gson.toJson(pnfAaiObject));
        assertEquals(pnfAaiObject, gson.fromJson(jsonPnfObject, ImmutablePnfAaiObject.class));
    }

    @Test
    void serviceInstanceAaiObject_IsSerializedSuccessfully() {

        var gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        var gson = gsonBuilder.create();

        var serviceInstanceId = "84003b26-6b76-4c75-b805-7b14ab4ffaef";
        var orchestrationStatus = "active";

        var template = "{"
                + "\"service-instance-id\":\"%s\","
                + "\"orchestration-status\":\"%s\","
                + "\"relationship-list\":{"
                + "\"relationship\":["
                + "{"
                + "\"related-to\":\"service-instance\","
                + "\"relationship-label\":\"org.onap.relationships.inventory.ComposedOf\","
                + "\"related-link\":\"/aai/v14/business/customers/customer/Demonstration/service-subscriptions"
                + "/service-subscription/BBS-CFS"
                + "/service-instances/service-instance/bb374844-44e4-488f-8381-fb5a0e3e6989\","
                + "\"relationship-data\":["
                + "{"
                + "\"relationship-key\":\"service-instance.service-instance-id\","
                + "\"relationship-value\":\"bb374844-44e4-488f-8381-fb5a0e3e6989\""
                + "}"
                + "]"
                + "}"
                + "]"
                + "},"
                + "\"metadata\":{"
                + "\"metadatum\":["
                + "{"
                + "\"metaname\":\"cvlan\","
                + "\"metaval\":\"1005\""
                + "}"
                + "]"
                + "}"
                + "}";

        // Build Relationship Data
        RelationshipListAaiObject.RelationshipEntryAaiObject relationshipEntry =
                ImmutableRelationshipEntryAaiObject.builder()
                        .relatedTo("service-instance")
                        .relatedLink("/aai/v14/business/customers/customer/Demonstration/service-subscriptions"
                                + "/service-subscription/BBS-CFS/service-instances"
                                + "/service-instance/bb374844-44e4-488f-8381-fb5a0e3e6989")
                        .relationshipLabel("org.onap.relationships.inventory.ComposedOf")
                        .relationshipData(Collections.singletonList(ImmutableRelationshipDataEntryAaiObject.builder()
                                .relationshipKey("service-instance.service-instance-id")
                                .relationshipValue("bb374844-44e4-488f-8381-fb5a0e3e6989").build()))
                        .build();

        RelationshipListAaiObject relationshipListAaiObject = ImmutableRelationshipListAaiObject.builder()
                .relationshipEntries(Collections.singletonList(relationshipEntry))
                .build();

        MetadataListAaiObject.MetadataEntryAaiObject metadataEntry =
                ImmutableMetadataEntryAaiObject.builder()
                .metaname("cvlan")
                .metavalue("1005")
                .build();

        MetadataListAaiObject metadataListAaiObject = ImmutableMetadataListAaiObject.builder()
                .metadataEntries(Collections.singletonList(metadataEntry))
                .build();

        // Finally construct Service Instance object data
        ServiceInstanceAaiObject serviceInstanceAaiObject = ImmutableServiceInstanceAaiObject.builder()
                .serviceInstanceId(serviceInstanceId)
                .orchestrationStatus(orchestrationStatus)
                .relationshipListAaiObject(relationshipListAaiObject)
                .metadataListAaiObject(metadataListAaiObject)
                .build();


        var jsonServiceInstanceObject = String.format(template, serviceInstanceId, orchestrationStatus);

        assertEquals(jsonServiceInstanceObject, gson.toJson(serviceInstanceAaiObject));
        assertEquals(serviceInstanceAaiObject, gson.fromJson(jsonServiceInstanceObject,
                ImmutableServiceInstanceAaiObject.class));
    }
}