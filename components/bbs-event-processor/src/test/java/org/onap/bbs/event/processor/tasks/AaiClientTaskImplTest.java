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

package org.onap.bbs.event.processor.tasks;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.ServiceLoader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.bbs.event.processor.exceptions.AaiTaskException;
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
import org.onap.bbs.event.processor.utilities.AaiReactiveClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AaiClientTaskImplTest {

    private AaiReactiveClient reactiveClient;

    private AaiClientTask task;

    @BeforeEach
    void init() {
        var gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        reactiveClient = Mockito.mock(AaiReactiveClient.class);
        task = new AaiClientTaskImpl(reactiveClient);
    }

    @Test
    void passingEmptyPnfObject_NothingHappens() throws AaiTaskException {
        when(reactiveClient.getPnfObjectDataFor(any(String.class))).thenReturn(Mono.empty());
        var pnf = task.executePnfRetrieval("Empty PNF task", "some-url");

        verify(reactiveClient).getPnfObjectDataFor("some-url");
        assertNull(pnf.block(Duration.ofSeconds(5)));
    }

    @Test
    void passingEmptyServiceInstanceObject_NothingHappens() throws AaiTaskException {
        when(reactiveClient.getServiceInstanceObjectDataFor(any(String.class))).thenReturn(Mono.empty());
        var serviceInstance =
                task.executeServiceInstanceRetrieval("Empty Service Instance task", "some-url");

        verify(reactiveClient).getServiceInstanceObjectDataFor("some-url");
        assertNull(serviceInstance.block(Duration.ofSeconds(5)));
    }

    @Test
    void passingPnfObject_taskSucceeds() throws AaiTaskException {

        var pnfName = "pnf-1";
        var attachmentPoint = "olt1-1-1";

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

        when(reactiveClient.getPnfObjectDataFor(any(String.class))).thenReturn(Mono.just(pnfAaiObject));
        var pnf = task.executePnfRetrieval("Normal PNF retrieval task", "some-url");

        verify(reactiveClient).getPnfObjectDataFor("some-url");
        assertNotNull(pnf.block(Duration.ofSeconds(5)));

        StepVerifier.create(pnf)
                .expectSubscription()
                .consumeNextWith(aPnf -> {
                    Assertions.assertEquals(pnfName, aPnf.getPnfName(), "PNF Name in response does not match");
                    var extractedAttachmentPoint = aPnf.getRelationshipListAaiObject().getRelationshipEntries()
                            .stream()
                            .filter(e -> e.getRelatedTo().equals("logical-link"))
                            .flatMap(e -> e.getRelationshipData().stream())
                            .filter(d -> d.getRelationshipKey().equals("logical-link.link-name"))
                            .map(RelationshipListAaiObject.RelationshipDataEntryAaiObject::getRelationshipValue)
                            .findFirst().orElseThrow(AaiClientTaskTestException::new);
                    Assertions.assertEquals(attachmentPoint, extractedAttachmentPoint,
                            "Attachment point in response does not match");
                })
                .verifyComplete();
    }

    @Test
    void passingServiceInstanceObject_taskSucceeds() throws AaiTaskException {

        var serviceInstanceId = "84003b26-6b76-4c75-b805-7b14ab4ffaef";
        var orchestrationStatus = "active";

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

        when(reactiveClient.getServiceInstanceObjectDataFor(any(String.class)))
                .thenReturn(Mono.just(serviceInstanceAaiObject));
        var serviceInstance =
                task.executeServiceInstanceRetrieval("Normal Service Instance retrieval task",
                        "some-url");

        verify(reactiveClient).getServiceInstanceObjectDataFor("some-url");
        assertNotNull(serviceInstance.block(Duration.ofSeconds(5)));

        StepVerifier.create(serviceInstance)
                .expectSubscription()
                .consumeNextWith(instance -> {
                    Assertions.assertEquals(serviceInstanceId, instance.getServiceInstanceId(),
                            "Service Instance ID in response does not match");

                    var extractedMetadataListObject =
                            instance.getMetadataListAaiObject().orElseThrow(AaiClientTaskTestException::new);

                    var extractedMetadataEntry =
                            extractedMetadataListObject.getMetadataEntries()
                                    .stream()
                                    .filter(m -> m.getMetaname().equals("cvlan"))
                                    .findFirst().orElseThrow(AaiClientTaskTestException::new);

                    Assertions.assertEquals("1005", extractedMetadataEntry.getMetavalue(),
                            "CVLAN in response does not match");
                })
                .verifyComplete();
    }

    private static class AaiClientTaskTestException extends RuntimeException {}
}