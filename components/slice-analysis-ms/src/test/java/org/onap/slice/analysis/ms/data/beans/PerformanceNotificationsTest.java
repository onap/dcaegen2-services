/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020 Wipro Limited.
 *   ==============================================================================
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
package org.onap.slice.analysis.ms.data.beans;
import static org.junit.Assert.*;
import java.sql.Timestamp;
import org.junit.Test;
public class PerformanceNotificationsTest {
    private Timestamp createdAt;
    @Test
    public void methodTest() {
        PerformanceNotifications performanceNotifications = new PerformanceNotifications();
        performanceNotifications.setNotification("notification");
        performanceNotifications.setCreatedAt(createdAt);
        assertEquals("notification", performanceNotifications.getNotification());
        assertEquals(createdAt, performanceNotifications.getCreatedAt());
    }
    @Test
    public void constructorTest(){
	PerformanceNotifications performanceNotification = new PerformanceNotifications("notifications",createdAt);
	System.out.println(performanceNotification.getNotification());
	assertEquals("notifications", performanceNotification.getNotification());
	assertEquals(createdAt, performanceNotification.getCreatedAt());
}
}
