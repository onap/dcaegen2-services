/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Huawei Canada Limited.
 *  ==============================================================================
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
package org.onap.slice.analysis.ms.service.ccvpn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class represents the data structure for storing the CCVPN pm data;
 */
@Component
public class CCVPNPmDatastore {

    private static Logger log = LoggerFactory.getLogger(CCVPNPmDatastore.class);
    private static final Pattern pattern = Pattern.compile("([0-9.]+)\\s*(kb|Kb|mb|Mb|Gb|gb)*");
    private static final int WINDOW_SIZE = 5;
    private final ConcurrentMap<String, ServiceState> svcStatus = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Integer> endpointToMaxBw = new ConcurrentHashMap<>();
    private final ConcurrentMap<Endpointkey, EvictingQueue<Integer>> endpointToUsedBw = new ConcurrentHashMap<>();

    /**
     * Given a cllId, return a map between Endpointkey and their corresponding UsedBw Queue.
     * All Endpoints belongs to this same service
     * @param cllId target cll instance id
     * @return a filtered map contains used bandwidth data of endpointkeys whose cllId equals to the given one.
     */
    public Map<Endpointkey, EvictingQueue<Integer>> getUsedBwOfSvc(String cllId){
        return endpointToUsedBw.entrySet().stream()
                .filter(map -> map.getKey().getCllId() == cllId)
                .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
    }

    /**
     * Return the complete used bandwidth map.
     * @return a complete endpoint to bandwidth data map
     */
    public Map<Endpointkey, EvictingQueue<Integer>> getUsedBwMap(){
        return endpointToUsedBw;
    }

    /**
     * Return max bandwidth of cll service. If max bandwidth is null or missing, return 0;
     * @param cllId target cll instance id
     * @return Integer bandwidth value
     */
    public Integer getMaxBwOfSvc(String cllId){
        return endpointToMaxBw.getOrDefault(cllId, 0);
    }

    /**
     * Get Service status of this cll service
     * @param cllId target cll instance id
     * @return ServiceState of this cll
     */
    public ServiceState getStatusOfSvc(String cllId){
        return svcStatus.getOrDefault(cllId, ServiceState.UNKNOWN);
    }

    /**
     * return the complete map of cll service status
     * @return complete map of serviceStatusMap
     */
    public ConcurrentMap<String, ServiceState> getSvcStatusMap(){
        return svcStatus;
    }

    /**
     * Override the service status to provided state
     * @param cllId target cll instance id
     * @param state new state
     */
    public void updateSvcState(String cllId, ServiceState state){
        svcStatus.put(cllId, state);
    }

    /**
     * Update max bandwidth value to given bandwidth string
     * @param cllId target cll instance id
     * @param bw new bandwidth
     */
    public void updateMaxBw(String cllId, String bw){
        double bwvvaldb = Double.parseDouble(bw);
        int bwvval = (int) bwvvaldb;
        updateMaxBw(cllId, bwvval, false);
    }

    /**
     * Update max bandwidth to given bandwidth value;
     * if @param{override} is false, only write the bandwidth if it is absent.
     * Otherwise override the old value no matter if it exists or not
     * Also, when @param{override} is true, compare the provided value with the old value, if equals, return false;
     * otherwise, return true;
     * @param cllId target cll instance id
     * @param bw new bandwidth int value in Mbps
     * @param override override old value or not
     * @return whether bandwidth value is changed or not.
     */
    public boolean updateMaxBw(String cllId, int bw, boolean override){
        ;
        if ( endpointToMaxBw.putIfAbsent(cllId, bw) == null || !override){
            return true;
        } else {
            if (endpointToMaxBw.get(cllId) == bw){
                return false;
            } else {
                endpointToMaxBw.replace(cllId, bw);
                return true;
            }
        }
    }

    /**
     * Append the latest bandwidth data to associated endpoint
     * @param cllId target cll instance id
     * @param uniId target uni id
     * @param bw latest bandwidth usage data
     */
    public void addUsedBwToEndpoint(String cllId, String uniId, String bw){
        Endpointkey enk = new Endpointkey(cllId, uniId);
        Matcher matcher = pattern.matcher(bw.trim());
        //Default input bw unit is kbps;
        String unit = null;
        // Bw in Mbps;
        int result = 0;
        if (matcher.find()) {
            unit = matcher.group(2);
            if (unit == null || unit.isEmpty() || unit.toLowerCase().equals("kb")) {
                double val = Double.parseDouble(matcher.group(1));
                result = (int) Math.ceil((double) val / (int) 1000 ) ;
            } else if (unit.toLowerCase().equals("mb")){
                double val = Double.parseDouble(matcher.group(1));
                result = (int) val ;
            } else if (unit.toLowerCase().equals("gb")){
                double val = Double.parseDouble(matcher.group(1));
                result = (int) val * (int) 1000;
            }
        } else {
            log.warn("Illigal bw string: " + bw);
        }

        EvictingQueue<Integer> dataq = new EvictingQueue<Integer>(WINDOW_SIZE);
        dataq.offer(result);
        EvictingQueue q = endpointToUsedBw.putIfAbsent(enk, dataq);
        if (q != null) {
            q.offer(result);
        }

    }

    /**
     * Copy the used bandwidth queue of specified cllId:uniId to an array and return;
     * @param cllId target cll id
     * @param uniId target uni id
     * @return Object[] contains all the used bandwidth data
     */
    public Object[] readToArray(String cllId, String uniId){
        return endpointToUsedBw.get(new Endpointkey(cllId, uniId)).tryReadToArray();
    }

    /**
     * Inner data structure is logically similar to circular buffer, thread-safe through blocking
     * @param <E> Generic type of data
     */
    public class EvictingQueue<E> {
        private final Queue<E> delegate;
        final int maxSize;

        /**
         * Constructor accept a maxsize param
         * @param maxSize max size
         */
        EvictingQueue(int maxSize){
            if (maxSize < 0){
                throw new IllegalArgumentException("Invalid maxsize for initializing EvictingQueue");
            }
            this.delegate = new ArrayDeque<>(maxSize);
            this.maxSize = maxSize;
        }

        /**
         * Adding new data to this queue
         * @param e new data
         * @return true
         */
        public synchronized boolean offer(E e){
            return add(e);
        }

        /**
         * Try copy data to an array and return, only if data has filled up the whole queue
         * Otherwise, return null
         * @return the data array
         */
        public synchronized Object[] tryReadToArray(){
            if (remainingCapacity() > 0){
                return null;
            }
            return toArray();
        }

        /**
         * Return the size of this queue, and number of data added. It is no larger than the max capacity.
         * @return int value of output
         */
        public int size(){
            return delegate.size();
        }

        /**
         * return the remaining capacity of this queue
         * @return int value of output
         */
        public int remainingCapacity(){
            return maxSize - size();
        }

        private Object[] toArray(){
            return delegate.toArray();
        }

        private boolean add(E e){
            if(null == e){
                throw new IllegalArgumentException("Invalid new item in add method");
            }
            if (maxSize == 0){
                return true;
            }
            if (size() == maxSize){
                delegate.remove();
            }
            delegate.add(e);
            return true;
        }
    }
}