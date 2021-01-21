/*
* ============LICENSE_START=======================================================
* ONAP : DCAE
* ================================================================================
* Copyright 2019 China Mobile
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

package org.onap.dcaegen2.kpi.utils;

import java.util.HashMap;

import lombok.Getter;
import org.json.JSONArray;

import org.json.JSONObject;

/**
 * Utils for JSON.
 *
 * @author Guobiao Mo
 *
 */
public class JsonUtil {

    @Getter
    enum AggregateType {
        ALL("aggregate"), AVEARGE("average"), SUM("sum"), MAX("max"), MIN("min"), COUNT("count");
        private final String name;

        AggregateType(String name) {
            this.name = name;
        }

        public String getLabel(String path) {
            return path.substring(path.lastIndexOf('/') + 1) + "_" + name;
        }
    }

    /**
     * ves event convert.
     *
     * @param path path
     * @param json json
     *
     */
    public static void flattenArray(String path, JSONObject json) {

        int index1 = path.lastIndexOf('/');

        String arrayPath = path.substring(0, index1);

        Object obj;
        try {
            obj = json.query(arrayPath);
        } catch (org.json.JSONPointerException e) {
            return;
        }
        if (obj == null || !(obj instanceof JSONArray)) {
            return;
        }
        Iterable<JSONObject> subjsonaArray = (Iterable<JSONObject>) obj;

        String tagName = path.substring(index1 + 1);// astriInterface

        int index2 = path.lastIndexOf('/', index1 - 1);
        String arrayName = path.substring(index2 + 1, index1);// astriDPMeasurementArray

        String parentPath = path.substring(0, index2);// /event/measurementsForVfScalingFields/astriMeasurement
        JSONObject parent = (JSONObject) json.query(parentPath);

        for (JSONObject element : subjsonaArray) {
            String tagValue = element.get(tagName).toString();
            String label = arrayName + "_" + tagName + "_" + tagValue;

            parent.put(label, element);
        }
    }

    /**
     * Json got modified.
     *
     * @param path path
     * @param json json
     *
     */
    public static void arrayAggregate(String path, JSONObject json) {
        HashMap<String, Double> sumHashMap = new HashMap<>();
        HashMap<String, Double> maxHashMap = new HashMap<>();
        HashMap<String, Double> minHashMap = new HashMap<>();

        Object obj;
        try {
            obj = json.query(path);
        } catch (org.json.JSONPointerException e) {
            return;
        }
        if (obj == null || !(obj instanceof JSONArray)) {
            return;
        }
        Iterable<JSONObject> subjsonaArray = (Iterable<JSONObject>) obj;

        int count = 0;
        for (JSONObject element : subjsonaArray) {
            String[] names = JSONObject.getNames(element);
            for (String name : names) {
                Number value = element.optNumber(name);
                if (value != null) {
                    double existing = sumHashMap.computeIfAbsent(name, k -> 0.0);
                    sumHashMap.put(name, existing + value.doubleValue());

                    existing = maxHashMap.computeIfAbsent(name, k -> Double.MIN_VALUE);
                    maxHashMap.put(name, Math.max(existing, value.doubleValue()));

                    existing = minHashMap.computeIfAbsent(name, k -> Double.MAX_VALUE);
                    minHashMap.put(name, Math.min(existing, value.doubleValue()));
                }
            }
            count++;
        }

        if (count == 0) {
            return;
        }

        JSONObject parentJson = (JSONObject) json.query(path.substring(0, path.lastIndexOf('/')));

        // sum
        JSONObject aggJson = new JSONObject(sumHashMap);
        parentJson.put(AggregateType.SUM.getLabel(path), aggJson);

        // AVEARGE
        int counter = count;// need to be Effectively Final
        sumHashMap.replaceAll((key, value) -> value / counter);
        aggJson = new JSONObject(sumHashMap);
        parentJson.put(AggregateType.AVEARGE.getLabel(path), aggJson);

        // Count
        parentJson.put(AggregateType.COUNT.getLabel(path), count);

        // Max
        aggJson = new JSONObject(maxHashMap);
        parentJson.put(AggregateType.MAX.getLabel(path), aggJson);

        // Min
        aggJson = new JSONObject(minHashMap);
        parentJson.put(AggregateType.MIN.getLabel(path), aggJson);

    }

}
