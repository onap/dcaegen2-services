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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.ServiceLoader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.onap.bbs.event.processor.config.AaiClientConfiguration;
import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.model.ImmutableMetadataEntryAaiObject;
import org.onap.bbs.event.processor.model.ImmutableMetadataListAaiObject;
import org.onap.bbs.event.processor.model.ImmutablePnfAaiObject;
import org.onap.bbs.event.processor.model.ImmutablePropertyAaiObject;
import org.onap.bbs.event.processor.model.ImmutableRelationshipDataEntryAaiObject;
import org.onap.bbs.event.processor.model.ImmutableRelationshipEntryAaiObject;
import org.onap.bbs.event.processor.model.ImmutableRelationshipListAaiObject;
import org.onap.bbs.event.processor.model.ImmutableServiceInstanceAaiObject;
import org.onap.bbs.event.processor.model.MetadataListAaiObject;
import org.onap.bbs.event.processor.model.PnfAaiObject;
import org.onap.bbs.event.processor.model.RelationshipListAaiObject;
import org.onap.bbs.event.processor.model.ServiceInstanceAaiObject;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
class AaiReactiveClientTest {

    private static final int PORT = 9999;

    private static AaiReactiveClient reactiveClient;
    private static Gson gson;
    private static WireMockServer wireMockServer;

    @BeforeAll
    static void init() {
        var gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        gson = gsonBuilder.create();

        var configuration = Mockito.mock(ApplicationConfiguration.class);
        var aaiClientConfiguration = Mockito.mock(AaiClientConfiguration.class);
        when(configuration.getAaiClientConfiguration()).thenReturn(aaiClientConfiguration);
        when(aaiClientConfiguration.aaiUserName()).thenReturn("AAI");
        when(aaiClientConfiguration.aaiUserPassword()).thenReturn("AAI");
        when(aaiClientConfiguration.aaiHeaders()).thenReturn(new HashMap<>());
        when(aaiClientConfiguration.enableAaiCertAuth()).thenReturn(false);

        reactiveClient = new AaiReactiveClient(configuration, gson);

        wireMockServer = new WireMockServer(PORT);
        WireMock.configureFor("localhost", PORT);
    }

    @BeforeEach
    void wireMockSetup() {
        wireMockServer.start();
    }

    @AfterEach
    void wireMockTearDown() {
        wireMockServer.start();
    }

    @Test
    void sendingReactiveRequestForPnf_Succeeds() {

        var pnfName = "pnf-1";
        var attachmentPoint = "olt1-1-1";

        var pnfUrl = String.format("/aai/v14/network/pnfs/pnf/%s?depth=1", pnfName);

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
                        .relatedTo("logical-link")
                        .relatedLink("/network/logical-links/logical-link/" + attachmentPoint)
                        .relationshipData(Collections.singletonList(ImmutableRelationshipDataEntryAaiObject.builder()
                                .relationshipKey("logical-link.link-name")
                                .relationshipValue(attachmentPoint).build()))
                        .build();

        RelationshipListAaiObject relationshipListAaiObject = ImmutableRelationshipListAaiObject.builder()
                .relationshipEntries(Arrays.asList(firstRelationshipEntry, secondRelationshipEntry))
                .build();

        // Finally construct PNF object data
        PnfAaiObject pnfAaiObject = ImmutablePnfAaiObject.builder()
                .pnfName(pnfName)
                .isInMaintenance(true)
                .relationshipListAaiObject(relationshipListAaiObject)
                .build();

        givenThat(get(urlEqualTo(pnfUrl))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(gson.toJson(pnfAaiObject, ImmutablePnfAaiObject.class))));

        StepVerifier.create(reactiveClient.getPnfObjectDataFor("http://127.0.0.1:" + PORT + pnfUrl))
                .expectSubscription()
                .consumeNextWith(pnf -> {
                    Assertions.assertEquals(pnfName, pnf.getPnfName(), "PNF Name in response does not match");
                    var extractedAttachmentPoint = pnf.getRelationshipListAaiObject().getRelationshipEntries()
                            .stream()
                            .filter(e -> e.getRelatedTo().equals("logical-link"))
                            .flatMap(e -> e.getRelationshipData().stream())
                            .filter(d -> d.getRelationshipKey().equals("logical-link.link-name"))
                            .map(RelationshipListAaiObject.RelationshipDataEntryAaiObject::getRelationshipValue)
                            .findFirst().orElseThrow(AaiReactiveClientTestException::new);
                    Assertions.assertEquals(attachmentPoint, extractedAttachmentPoint,
                            "Attachment point in response does not match");
                })
                .verifyComplete();
    }

    @Test
    void sendingReactiveRequestForServiceInstance_Succeeds() {

        var serviceInstanceId = "84003b26-6b76-4c75-b805-7b14ab4ffaef";
        var orchestrationStatus = "active";

        var serviceInstanceUrl =
                String.format("/aai/v14/nodes/service-instances/service-instance/%s?format=resource_and_url",
                serviceInstanceId);

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

        givenThat(get(urlEqualTo(serviceInstanceUrl))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(gson.toJson(serviceInstanceAaiObject, ImmutableServiceInstanceAaiObject.class))));

        StepVerifier.create(
                reactiveClient.getServiceInstanceObjectDataFor("http://127.0.0.1:" + PORT + serviceInstanceUrl)
        )
                .expectSubscription()
                .consumeNextWith(serviceInstance -> {
                    Assertions.assertEquals(serviceInstanceId, serviceInstance.getServiceInstanceId(),
                            "Service Instance ID in response does not match");

                    var extractedMetadataListObject =
                            serviceInstance.getMetadataListAaiObject().orElseThrow(AaiReactiveClientTestException::new);

                    var extractedMetadataEntry =
                            extractedMetadataListObject.getMetadataEntries()
                            .stream()
                            .filter(m -> m.getMetaname().equals("cvlan"))
                            .findFirst().orElseThrow(AaiReactiveClientTestException::new);

                    Assertions.assertEquals("1005", extractedMetadataEntry.getMetavalue(),
                            "CVLAN in response does not match");
                })
                .verifyComplete();
    }

    private static class AaiReactiveClientTestException extends RuntimeException {}

}