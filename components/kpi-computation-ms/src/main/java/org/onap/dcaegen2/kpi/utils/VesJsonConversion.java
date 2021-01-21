/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021 China Mobile.
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

package org.onap.dcaegen2.kpi.utils;

import com.google.gson.Gson;
import org.json.JSONObject;
import org.onap.dcaegen2.kpi.models.VesEvent;

/**
 * Ves Json Conversion.
 *
 * @author Kai Lu
 *
 */
public class VesJsonConversion {

    private VesJsonConversion() {
    }

    /**
     * ves event convert.
     *
     * @param strVesEvent strVesEvent
     * @return ves event
     *
     */
    public static VesEvent convertVesEvent(String strVesEvent) {
        Gson gson = new Gson();
        VesEvent vesEvent = gson.fromJson(strVesEvent, VesEvent.class);
        return vesEvent;
    }

    /**
     * ves event convert.
     *
     * @param strVesEvent strVesEvent
     * @return jsonOject JSONObject
     *
     */
    public static JSONObject convertToJsonObject(String strVesEvent) {
        return new JSONObject(strVesEvent);
    }

    /**
     * ves event convert.
     *
     * @param vesEvent vesEvent
     * @return Kpi event string list
     *
     */
    public static String convertVesEventToString(VesEvent vesEvent) {
        Gson gson = new Gson();
        String strVesEvent = gson.toJson(vesEvent);
        return strVesEvent;
    }
}
