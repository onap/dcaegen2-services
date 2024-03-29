/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Wipro Limited.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.dcaegen2.kpi.models;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.mockito.Spy;
public class ConfigPolicyTest {
    @Spy
    ConfigPolicy configPolicy;
    @Test
    public void testGetInstance() {
        ConfigPolicy instance = ConfigPolicy.getInstance();
        assertNotNull(instance);
    }
    @Test
    public void testGetInstanceIfNull() {
        ConfigPolicy instance = ConfigPolicy.getInstance();
        instance = null;
        ConfigPolicy instanceNew = ConfigPolicy.getInstance();
        assertNotNull(instanceNew);
    }
}
