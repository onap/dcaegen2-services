package com.mongodb.internal.validator;

//copy from https://github.com/mongodb/mongo-java-driver/blob/master/driver-core/src/main/com/mongodb/internal/validator/CollectibleDocumentFieldNameValidator.java
//allow inserting name with dot
/*
* ============LICENSE_START=======================================================
* ONAP : DataLake
* ================================================================================
* Copyright 2018 China Mobile
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License")
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
import org.bson.FieldNameValidator;

import java.util.Arrays;
import java.util.List;

/**
 * A field name validator for document that are meant for storage in MongoDB collections.  It ensures that no fields contain a '.',
 * or start with '$' (with the exception of "$db", "$ref", and "$id", so that DBRefs are not rejected).
 *
 * <p>This class should not be considered a part of the public API.</p>
 */
public class CollectibleDocumentFieldNameValidator implements FieldNameValidator {
    // Have to support DBRef fields
    private static final List<String> EXCEPTIONS = Arrays.asList("$db", "$ref", "$id");

    @Override
    public boolean validate(final String fieldName) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name can not be null");
        }

        return !fieldName.startsWith("$") || EXCEPTIONS.contains(fieldName);
    }

    @Override
    public FieldNameValidator getValidatorForField(final String fieldName) {
        return this;
    }
}
