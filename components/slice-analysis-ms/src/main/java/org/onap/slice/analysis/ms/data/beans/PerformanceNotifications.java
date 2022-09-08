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

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

/**
 * Entity for  PERFORMANCE_NOTIFICATIONS table
 */
@Entity
@Table(name = "PERFORMANCE_NOTIFICATIONS")
public class PerformanceNotifications {

    @Id
    @Column(name = "notification", columnDefinition = "text")
    private String notification;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamp")
    private Timestamp createdAt;

    /**
     * default constructor
     */
    public PerformanceNotifications() {

    }

    /**
     * Constructs PerformanceNotifications instance
     * @param notification
     * @param createdAt
     */
    public PerformanceNotifications(String notification, Timestamp createdAt) {
        this.notification = notification;
        this.createdAt = createdAt;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

}
