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

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters(fieldNamingStrategy = true, emptyAsNulls = true)
public interface MetadataListAaiObject {

    @SerializedName(value = "metadatum", alternate = "metadatum")
    List<MetadataEntryAaiObject> getMetadataEntries();

    @Value.Immutable
    @Gson.TypeAdapters(fieldNamingStrategy = true)
    interface MetadataEntryAaiObject {

        @SerializedName(value = "metaname", alternate = "metaname")
        String getMetaname();

        @SerializedName(value = "metaval", alternate = "metaval")
        String getMetavalue();

    }
}
