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

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Optional;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters(fieldNamingStrategy = true, emptyAsNulls = true)
public interface RelationshipListAaiObject {

    @SerializedName(value = "relationship", alternate = "relationship")
    List<RelationshipEntryAaiObject> getRelationshipEntries();

    @Value.Immutable
    @Gson.TypeAdapters(fieldNamingStrategy = true)
    interface RelationshipEntryAaiObject {

        @SerializedName(value = "related-to", alternate = "related-to")
        String getRelatedTo();

        @SerializedName(value = "relationship-label", alternate = "relationship-label")
        Optional<String> getRelationshipLabel();

        @SerializedName(value = "related-link", alternate = "related-link")
        String getRelatedLink();

        @SerializedName(value = "relationship-data", alternate = "relationship-data")
        List<RelationshipDataEntryAaiObject> getRelationshipData();

        @SerializedName(value = "related-to-property", alternate = "related-to-property")
        Optional<List<PropertyAaiObject>> getRelatedToProperties();

    }

    @Value.Immutable
    @Gson.TypeAdapters(fieldNamingStrategy = true)
    interface RelationshipDataEntryAaiObject {

        @SerializedName(value = "relationship-key", alternate = "relationship-key")
        String getRelationshipKey();

        @SerializedName(value = "relationship-value", alternate = "relationship-value")
        String getRelationshipValue();

    }

    @Value.Immutable
    @Gson.TypeAdapters(fieldNamingStrategy = true)
    interface PropertyAaiObject {

        @SerializedName(value = "property-key", alternate = "property-key")
        String getPropertyKey();

        @SerializedName(value = "property-value", alternate = "property-value")
        Optional<String> getPropertyValue();

    }
}
