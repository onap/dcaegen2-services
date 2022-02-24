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

import static org.onap.bbs.event.processor.config.ApplicationConstants.IN_SERVICE_NAME_IN_ONAP;
import static org.onap.bbs.event.processor.config.ApplicationConstants.OUT_OF_SERVICE_NAME_IN_ONAP;

public enum AuthenticationState {

    IN_SERVICE(IN_SERVICE_NAME_IN_ONAP),
    OUT_OF_SERVICE(OUT_OF_SERVICE_NAME_IN_ONAP);

    private String nameInOnap;

    AuthenticationState(String nameInOnap) {
        this.nameInOnap = nameInOnap;
    }

    public String getNameInOnap() {
        return nameInOnap;
    }
}