/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *  Copyright (C) 2022 CTC, Inc.
 *  ==============================================================================
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     ============LICENSE_END=========================================================
 *
 *******************************************************************************/

package org.onap.slice.analysis.ms.models.aai;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = ServiceInstanceTest.class)
public class ServiceInstanceTest {

    @Test
    public void ServiceInstanceTest() {

        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("service-instance-id");
        serviceInstance.setServiceInstanceName("service-instance-name");
        serviceInstance.setServiceType("service-type");
        serviceInstance.setServiceRole("service-role");
        serviceInstance.setEnvironmentContext("environment-context");
        serviceInstance.setWorkloadContext("workload-context");
        serviceInstance.setOrchestrationStatus("orchestration-status");
        List<Relationship> relationships = new ArrayList<>();
        RelationshipList relationshipList = new RelationshipList();
        relationshipList.setRelationship(relationships);
        serviceInstance.setRelationshipList(relationshipList);


        assertEquals("service-instance-id", serviceInstance.getServiceInstanceId());
        assertEquals("service-instance-name", serviceInstance.getServiceInstanceName());
        assertEquals("service-type", serviceInstance.getServiceType());
        assertEquals("service-role", serviceInstance.getServiceRole());
        assertEquals("environment-context", serviceInstance.getEnvironmentContext());
        assertEquals("workload-context", serviceInstance.getWorkloadContext());
        assertEquals("orchestration-status", serviceInstance.getOrchestrationStatus());
        assertEquals(0, serviceInstance.getRelationshipList().getRelationship().size());
    }

    @Test
    public void ServiceInstanceEqualsTest() {

        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("service-instance-id");
        serviceInstance.setServiceInstanceName("service-instance-name");
        serviceInstance.setServiceType("service-type");
        serviceInstance.setServiceRole("service-role");
        serviceInstance.setEnvironmentContext("environment-context");
        serviceInstance.setWorkloadContext("workload-context");
        serviceInstance.setOrchestrationStatus("orchestration-status");
        List<Relationship> relationships = new ArrayList<>();
        RelationshipList relationshipList = new RelationshipList();
        relationshipList.setRelationship(relationships);
        serviceInstance.setRelationshipList(relationshipList);

        ServiceInstance serviceInstance1 = new ServiceInstance();
        serviceInstance1.setServiceInstanceId("service-instance-id");
        serviceInstance1.setServiceInstanceName("service-instance-name");
        serviceInstance1.setServiceType("service-type");
        serviceInstance1.setServiceRole("service-role");
        serviceInstance1.setEnvironmentContext("environment-context");
        serviceInstance1.setWorkloadContext("workload-context");
        serviceInstance1.setOrchestrationStatus("orchestration-status");
        List<Relationship> relationships1 = new ArrayList<>();
        RelationshipList relationshipList1 = new RelationshipList();
        relationshipList1.setRelationship(relationships1);
        serviceInstance1.setRelationshipList(relationshipList1);

        assertTrue(serviceInstance1.equals(serviceInstance));
        assertTrue(StringUtils.isNotBlank(serviceInstance.toString()));
    }

    @Test
    public void equalsContract() {
        EqualsVerifier.simple().forClass(ServiceInstance.class)
                .verify();
    }
}
