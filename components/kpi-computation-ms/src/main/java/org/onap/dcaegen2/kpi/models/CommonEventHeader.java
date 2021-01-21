/*
 * ================================================================================
 * Copyright (c) 2020-2021 China Mobile. All rights reserved.
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
 *
 */

package org.onap.dcaegen2.kpi.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.onap.dcaegen2.kpi.config.BaseModule;

/**
 * Fields common to all Events.
 *
 * @author Kai Lu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommonEventHeader extends BaseModule {

    private static final long serialVersionUID = 1L;

    /**
     * The eventing domain associated with this event.
     */
    private String domain;

    /**
     * Event key that is unique to the event source.
     */
    private String eventId;

    /**
     * Unique event name.
     */
    private String eventName;

    /**
     * Event type e.g. applicationVnf, guestOS, hostOS, platform.
     */
    private String eventType;

    /**
     * The latest unix time aka epoch time associated with the event from any
     * component--as microseconds elapsed since 1 Jan 1970 not including leap
     * seconds.
     */
    private Long lastEpochMicrosec;

    /**
     * Three character network function component type as aligned with vfc naming
     * standards.
     */
    private String nfcNamingCode;

    /**
     * Four character network function type as aligned with vnf naming standards.
     */
    private String nfNamingCode;

    /**
     * Processing Priority.
     */
    private Priority priority;

    /**
     * UUID identifying the entity reporting the event, for example an OAM VM; must
     * be populated by the enrichment process.
     */
    private String reportingEntityId;

    /**
     * Name of the entity reporting the event, for example, an EMS name; may be the
     * same as sourceName.
     */
    private String reportingEntityName;

    /**
     * Ordering of events communicated by an event source instance or 0 if not
     * needed.
     */
    private Integer sequence;

    /**
     * UUID identifying the entity experiencing the event issue; must be populated
     * by the enrichment process.
     */
    private String sourceId;

    /**
     * Name of the entity experiencing the event issue.
     */
    private String sourceName;

    /**
     * the earliest unix time aka epoch time associated with the event from any
     * component--as microseconds elapsed since 1 Jan 1970 not including leap
     * seconds.
     */
    private Long startEpochMicrosec;

    /**
     * Version of the event header.
     */
    private Float version;

    /**
     * vesEventListenerVersion.
     */
    private String vesEventListenerVersion;

    /**
     * timeZoneOffset.
     */
    private String timeZoneOffset;

}
