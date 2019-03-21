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

package org.onap.bbs.event.processor.config;

public class ApplicationConstants {

    public static final String CONSUME_REREGISTRATION_TASK_NAME = "Consume Re-registration DMaaP message";
    public static final String CONSUME_CPE_AUTHENTICATION_TASK_NAME = "Consume CPE Authentication DMaaP message";
    public static final String RETRIEVE_PNF_TASK_NAME = "PNF Retrieval";
    public static final String RETRIEVE_HSI_CFS_SERVICE_INSTANCE_TASK_NAME = "HSI CFS Service Instance Retrieval";

    public static final String STREAMS_TYPE = "message_router";

    private ApplicationConstants() {}
}