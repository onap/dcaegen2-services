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


    public Map<Endpointkey, EvictingQueue<Integer>> getUsedBwOfSvc(String cllId){
        return endpointToUsedBw.entrySet().stream()
                .filter(map -> map.getKey().getCllId() == cllId)
                .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
    }

    public Integer getMaxBwOfSvc(String cllId){
        return endpointToMaxBw.getOrDefault(cllId, 0);
    }

    public ConcurrentMap<String, ServiceState> getSvcStatusMap(){
        return svcStatus;
    }

    public void updateMaxBw(String cllId, String bw){
        double bwvvaldb = Double.parseDouble(bw);
        int bwvval = (int) bwvvaldb;
        updateMaxBw(cllId, bwvval);
    }
    public void updateMaxBw(String cllId, int bw){
        ServiceState thisState = svcStatus.getOrDefault(cllId, ServiceState.RUNNING);
        if ( thisState == ServiceState.BEING_MAINTAINED){
            endpointToMaxBw.replace(cllId, bw);
            svcStatus.put(cllId, ServiceState.RUNNING );
        } else {
            endpointToMaxBw.put(cllId, bw);
        }
    }

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
        svcStatus.putIfAbsent(cllId, ServiceState.RUNNING);
        EvictingQueue<Integer> dataq = new EvictingQueue<Integer>(WINDOW_SIZE);
        dataq.offer(result);
        EvictingQueue q = endpointToUsedBw.putIfAbsent(enk, dataq);
        if (q != null) {
            q.offer(result);
        }

    }

    public Object[] readToArray(String cllId, String uniId){
        if (svcStatus.containsKey(cllId)) {
            return endpointToUsedBw.get(new Endpointkey(cllId, uniId)).tryReadToArray();
        }
        return null;
    }
    // Inner data structure is logically similar to circular buffer, thread-safe through blocking
    public class EvictingQueue<E> {

        private final Queue<E> delegate;

        final int maxSize;

        EvictingQueue(int maxSize){
            if (maxSize < 0){
                throw new IllegalArgumentException("Invalid maxsize for initializing EvictingQueue");
            }
            this.delegate = new ArrayDeque<>(maxSize);
            this.maxSize = maxSize;
        }

        public synchronized boolean offer(E e){
            return add(e);
        }

        public synchronized Object[] tryReadToArray(){
            if (remainingCapacity() > 0){
                return null;
            }
            return toArray();
        }
        public boolean add(E e){
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

        public int size(){
            return delegate.size();
        }

        public int remainingCapacity(){
            return maxSize - size();
        }

        public Object[] toArray(){
            return delegate.toArray();
        }
    }
}
