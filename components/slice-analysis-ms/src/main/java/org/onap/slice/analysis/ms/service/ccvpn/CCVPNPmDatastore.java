/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Huawei Canada Limited.
 *   Copyright (C) 2022 Huawei Technologies Co., Ltd.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import lombok.Getter;
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
    @Getter
    private final ConcurrentMap<String, ServiceState> svcStatus = new ConcurrentHashMap<>();
    // Provisioned bandwidth of each endpoint
    @Getter
    private final ConcurrentMap<String, Integer> endpointToProvBw = new ConcurrentHashMap<>();
    // Max bandwidth (upper-bound) of each endpoint
    @Getter
    private final ConcurrentMap<String, Integer> upperBoundBw = new ConcurrentHashMap<>();
    // Current bandwidth usage data list from customers
    @Getter
    private final ConcurrentMap<Endpointkey, EvictingQueue<Integer>> endpointToUsedBw = new ConcurrentHashMap<>();
    // Original bandwidth of each endpoint
    @Getter
    private final ConcurrentMap<String, Integer> endpointToOriginalBw = new ConcurrentHashMap<>();
    // Assurance Status of each endpoint
    @Getter
    private final ConcurrentMap<String, Boolean> closedLoopBwAssuranceStatus = new ConcurrentHashMap<>();

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
     * Return provisioned bandwidth of cll service. If provisioned bandwidth is null or missing, return 0;
     * @param cllId target cll instance id
     * @return Integer bandwidth value
     */
    public Integer getProvBwOfSvc(String cllId){
        return endpointToProvBw.getOrDefault(cllId, 0);
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
     * If ccvpn flexible threshold is on, then bandwidth can be assured within scope.
     * @param cllId
     * @return
     */
    public Integer getUpperBoundBwOfSvc(String cllId){
        return upperBoundBw.getOrDefault(cllId, Integer.MAX_VALUE);
    }

    /**
     * Get closed loop check status of this cll service
     * @param cllId
     * @return
     */
    public Boolean getClosedloopStatus(String cllId){
        return closedLoopBwAssuranceStatus.getOrDefault(cllId,true);
    }

    public int getOriginalBw(String cllId) {
        return endpointToOriginalBw.getOrDefault(cllId, 0);
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
     * Update provisioned bandwidth value to given bandwidth string
     * @param cllId target cll instance id
     * @param bw new bandwidth
     */
    public void updateProvBw(String cllId, String bw){
        double bwvvaldb = Double.parseDouble(bw);
        int bwvval = (int) bwvvaldb;
        updateProvBw(cllId, bwvval, false);
    }

    /**
     * Update the status, whether close loop bw modification of this cll service is on.
     * @param cllId
     * @param status
     */
    public void updateClosedloopStatus(String cllId, Boolean status){
        closedLoopBwAssuranceStatus.put(cllId, status);
    }

    /**
     * Update cll original bw, which will not influenced by closed loop bw assurance
     * @param cllId
     * @param originalBw
     */
    public void updateOriginalBw(String cllId, int originalBw){
        endpointToOriginalBw.put(cllId, originalBw);
    }

    /**
     * Update runtime configurations;
     * @param cllId
     * @param closedLoopBwAssuranceStatus
     * @param originalBw
     */
    public void updateConfigFromPolicy(String cllId, Boolean closedLoopBwAssuranceStatus, int originalBw) {
        updateClosedloopStatus(cllId, closedLoopBwAssuranceStatus);
        updateOriginalBw(cllId, originalBw);
    }

    /**
     * Update upper bound bandwidth value to given bandwidth
     * @param cllId target cll instance id
     * @param bw new bandwidth
     */
    public void updateUpperBoundBw(String cllId, int bw){
        upperBoundBw.put(cllId, bw);
    }

    /**
     * Update local service related variables in case cll is deleted.
     * @param allValidCllInstances
     */
    public void updateCllInstances(Set<String> allValidCllInstances){
        Set<String> invalidCllIds;
        invalidCllIds= filterInvalidCllIds(allValidCllInstances, svcStatus.keySet());
        for(String invalidCllId : invalidCllIds) {
            log.debug("drop {} from endpointToUsedBw", invalidCllId);
            endpointToUsedBw.entrySet().stream().dropWhile(map -> map.getKey().getCllId().equalsIgnoreCase(invalidCllId));
            Iterator<Map.Entry<Endpointkey, EvictingQueue<Integer>>> iterator = endpointToUsedBw.entrySet().iterator();
            while(iterator.hasNext()) {
                Endpointkey endpointkey = iterator.next().getKey();
                if(endpointkey.getCllId().equalsIgnoreCase(invalidCllId)) {
                    endpointToUsedBw.remove(endpointkey);
                }
            }
        }
        svcStatus.keySet().removeAll(invalidCllIds);
        invalidCllIds = filterInvalidCllIds(allValidCllInstances, endpointToProvBw.keySet());
        endpointToProvBw.keySet().removeAll(invalidCllIds);
        invalidCllIds = filterInvalidCllIds(allValidCllInstances, upperBoundBw.keySet());
        upperBoundBw.keySet().removeAll(invalidCllIds);
        invalidCllIds = filterInvalidCllIds(allValidCllInstances, endpointToOriginalBw.keySet());
        endpointToOriginalBw.keySet().removeAll(invalidCllIds);
        invalidCllIds = filterInvalidCllIds(allValidCllInstances, closedLoopBwAssuranceStatus.keySet());
        closedLoopBwAssuranceStatus.keySet().removeAll(invalidCllIds);
    }

    /**
     * Filter out cllId to be deleted
     * @param allValidCllInstances
     * @param currentCllInstances
     * @return
     */
    public Set<String> filterInvalidCllIds(Set<String> allValidCllInstances, Set<String> currentCllInstances) {
        Set<String> invalidCllInstances = new HashSet<>(currentCllInstances);
        invalidCllInstances.removeAll(allValidCllInstances);
        return invalidCllInstances;
    }

    /**
     * Update provisioned bandwidth to given bandwidth value;
     * if @param{override} is false, only write the bandwidth if it is absent.
     * Otherwise override the old value no matter if it exists or not
     * Also, when @param{override} is true, compare the provided value with the old value, if equals, return false;
     * otherwise, return true;
     * @param cllId target cll instance id
     * @param bw new bandwidth int value in Mbps
     * @param override override old value or not
     * @return whether bandwidth value is changed or not.
     */
    public boolean updateProvBw(String cllId, int bw, boolean override){
        if ( endpointToProvBw.putIfAbsent(cllId, bw) == null || !override){
            return true;
        } else {
            if (endpointToProvBw.get(cllId) == bw){
                return false;
            } else {
                endpointToProvBw.replace(cllId, bw);
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

        endpointToUsedBw.computeIfAbsent(enk, k -> new EvictingQueue<Integer>(WINDOW_SIZE)).offer(result);
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
