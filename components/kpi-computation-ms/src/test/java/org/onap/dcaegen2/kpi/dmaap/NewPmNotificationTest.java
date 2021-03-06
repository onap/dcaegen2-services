/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 China Mobile.
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

package org.onap.dcaegen2.kpi.dmaap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NewPmNotificationTest {

    @Test
    public void testNewPmNotif() {
        NewPmNotification newPmNotif1 = new NewPmNotification(true);
        NewPmNotification newPmNotif2 = new NewPmNotification();
        newPmNotif2.setNewNotif(true);
        assertTrue(newPmNotif2.getNewNotif());
        newPmNotif2.init();
        assertEquals(false, newPmNotif2.getNewNotif());
        assertTrue(newPmNotif1.getNewNotif());

    }
}
