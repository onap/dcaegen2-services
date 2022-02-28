/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Huawei Canada Limited.
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
package org.onap.slice.analysis.ms.models.ccvpnnotification;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class SimpleEvent<T extends Enum, S> implements Event<T, S> {
    private final T type;
    private final S subject;
    private final long time;

    public enum Type {
        PERIODIC_CHECK,
        ONDEMAND_CHECK,
        AAI_BW_REQ
    }


    public SimpleEvent(T type, S subject){
        this.type = type;
        this.subject = subject;
        this.time = System.currentTimeMillis();
    }

    @Override
    public long time(){ return time;}

    @Override
    public T type() { return type;}

    @Override
    public S subject() {
        return subject;
    }

    @Override
    public String toString(){
        return new StringBuilder()
                .append("time " + LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()))
                .append("type "+ type())
                .append("subject " + subject())
                .toString();
    }
}
