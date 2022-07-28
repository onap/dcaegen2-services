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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.JsonObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = RelationshipTest.class)
public class RelationshipTest {

    @Test
    public void RelationshipTest() throws JsonProcessingException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("related-to", "related-to");
        jsonObject.addProperty("relationship-label", "relationship-label");
        jsonObject.addProperty("related-link", "related-link");

        JsonArray relationshipData = new JsonArray();
        JsonObject relationshipDataObj = new JsonObject();
        relationshipDataObj.addProperty("a","1");
        relationshipData.add(relationshipDataObj);
        JsonArray relatedToProperty = new JsonArray();
        JsonObject relatedToPropertyObj = new JsonObject();
        relatedToPropertyObj.addProperty("a","1");
        relatedToProperty.add(relatedToPropertyObj);

        jsonObject.add("relationship-data", relationshipData);
        jsonObject.add("related-to-property", relatedToProperty);

        Relationship relationship = new ObjectMapper().readValue(jsonObject.toString(), Relationship.class);
        assertEquals("related-to", relationship.getRelatedTo());
        assertEquals("relationship-label", relationship.getRelationshipLabel());
        assertEquals("related-link", relationship.getRelatedLink());
        assertEquals(1, relationship.getRelationshipData().size());
        assertEquals(1, relationship.getRelatedToProperty().size());
    }

    @Test
    public void RelationshipEqualsTest() throws JsonProcessingException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("related-to", "related-to");
        jsonObject.addProperty("relationship-label", "relationship-label");
        jsonObject.addProperty("related-link", "related-link");

        JsonArray relationshipData = new JsonArray();
        JsonObject relationshipDataObj = new JsonObject();
        relationshipDataObj.addProperty("a","1");
        relationshipData.add(relationshipDataObj);
        JsonArray relatedToProperty = new JsonArray();
        JsonObject relatedToPropertyObj = new JsonObject();
        relatedToPropertyObj.addProperty("a","1");
        relatedToProperty.add(relatedToPropertyObj);

        jsonObject.add("relationship-data", relationshipData);
        jsonObject.add("related-to-property", relatedToProperty);

        Relationship relationship = new ObjectMapper().readValue(jsonObject.toString(), Relationship.class);
        Relationship relationship1 = new ObjectMapper().readValue(jsonObject.toString(), Relationship.class);

        assertTrue(relationship1.equals(relationship));
        assertTrue(StringUtils.isNotBlank(relationship1.toString()));
    }
}
