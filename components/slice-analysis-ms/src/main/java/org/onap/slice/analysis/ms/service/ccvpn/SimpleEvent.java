/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Huawei Canada Limited.
 *  =============================================================================
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
 *  ============LICENSE_END=========================================================
 *
 *******************************************************************************/
package org.onap.slice.analysis.ms.service.ccvpn;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * SimpleEvent a generic class implements Event (CCVPN) interface;
 * It is the message entity inside CCVPN Closed-loop
 *
 * @param <T> message type
 * @param <S> message paylaod
 */
public class SimpleEvent<T extends Enum, S> implements Event<T, S> {
    private final T type;
    private final S subject;
    private final long time;

    /**
     * All the event types
     */
    public enum Type {
        PERIODIC_CHECK,
        ONDEMAND_CHECK,
        AAI_BW_REQ
    }

    /**
     * Event contructor
     * @param type event type
     * @param subject event content
     */
    public SimpleEvent(T type, S subject) {
        this.type = type;
        this.subject = subject;
        this.time = System.currentTimeMillis();
    }

    /**
     * Return the epoch time of this event happened.
     * @return long value of epoch time
     */
    @Override
    public long time() {
        return time;
    }

    /**
     * Return the type of this event.
     * @return event type
     */
    @Override
    public T type() {
        return type;
    }

    /**
     * Return the subject of this event
     * @return event content
     */
    @Override
    public S subject() {
        return subject;
    }

    /**
     * toString method
     * @return toString
     */
    @Override
    public String toString() {
        return new StringBuilder()
                .append("time " + LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()))
                .append("type " + type())
                .append("subject " + subject())
                .toString();
    }
}
