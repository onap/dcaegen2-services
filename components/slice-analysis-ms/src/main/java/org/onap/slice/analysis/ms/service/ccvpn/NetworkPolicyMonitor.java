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

import lombok.NonNull;
import org.onap.slice.analysis.ms.aai.AaiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Actor that processes aai network-policy query request
 */
@Component
public class NetworkPolicyMonitor {
    private static Logger log = LoggerFactory.getLogger(NetworkPolicyMonitor.class);
    private Loop aaiEventLoop;
    private static final Event KILL_PILL = new SimpleEvent(null, 0);

    @Autowired
    AaiService aaiService;

    @Autowired
    CCVPNPmDatastore ccvpnPmDatastore;

    /**
     * Initialize and start the NetworkPolicyMonitor.
     */
    @PostConstruct
    public void init() {
        /**
         * AAI data consumer loop
         */
        aaiEventLoop = new Loop("AAIEventLoop"){
            @Override
            public void process(Event event) {
                if (event.type() == SimpleEvent.Type.AAI_BW_REQ){
                    log.debug("=== Processing new AAI network policy query at: {} ===", event.time());
                    String serviceId = (String) event.subject();
                    Map<String, Integer> maxBandwidthData = aaiService.fetchMaxBandwidthOfService(serviceId);
                    if (maxBandwidthData.get("maxBandwidth") != null){
                        log.debug("Successfully retrieved bandwidth info from AAI; service: {}, bandwidth: {}",
                                serviceId, maxBandwidthData.get("maxBandwidth"));
                        int bwValue = maxBandwidthData.get("maxBandwidth").intValue();
                        if (ccvpnPmDatastore.getProvBwOfSvc(serviceId) == 0){
                            ccvpnPmDatastore.updateProvBw(serviceId, bwValue, true);
                            log.debug("Provision bw of cll {} updated from 0 to {}, max bw is {}", serviceId, ccvpnPmDatastore.getProvBwOfSvc(serviceId), bwValue);
                        } else if (ccvpnPmDatastore.getProvBwOfSvc(serviceId) != bwValue) {
                            log.debug("Service modification complete; serviceId: {} update prov bw from {} to {}", serviceId, ccvpnPmDatastore.getProvBwOfSvc(serviceId), bwValue);
                            ccvpnPmDatastore.updateProvBw(serviceId, bwValue, true);
                            ccvpnPmDatastore.updateSvcState(serviceId, ServiceState.RUNNING);
                            log.debug("Service state of {} is changed to running, {}", serviceId, ccvpnPmDatastore.getStatusOfSvc(serviceId));
                        }
                    }
                    log.debug("=== Processing AAI network policy query complete ===");
                }
            }
        };
    }

    /**
     * Post/broadcast event between Loops
     * @param event event object
     */
    public void post(@NonNull Event event) {
        if (event.type() == SimpleEvent.Type.AAI_BW_REQ) {
            aaiEventLoop.add(event);
        }
    }
    /**
     * Inner loop implementation. Each loop acts like an actor.
     */
    private abstract class Loop implements Runnable {
        private final String name;
        private volatile boolean running;
        private final BlockingQueue<Event> eventsQueue;
        private final ExecutorService executor;
        private volatile Future<?> dispatchFuture;

        /**
         * Constructor that accepts a loop name
         * @param name name of this loop
         */
        Loop(String name){
            this.name = name;
            executor = Executors.newSingleThreadExecutor();
            eventsQueue = new LinkedBlockingQueue<>();
            dispatchFuture = executor.submit(this);
        }

        /**
         * Add new event to this loop
         * @param evt Event
         * @return true
         */
        public boolean add(Event evt) {
            return eventsQueue.add(evt);
        }

        /**
         * Running loop that process event accordingly
         */
        @Override
        public void run(){
            running = true;
            log.info("NetworkPolicyMonitor -- {} initiated", this.name);
            while (running){
                try{
                    Event event = eventsQueue.take();
                    if (event == KILL_PILL){
                        break;
                    }
                    process(event);
                } catch (InterruptedException e){
                    log.warn("Process loop interrupted");
                } catch (Exception | Error e){
                    log.warn("Process loop hit an error {}", e.getMessage());
                }
            }
        }

        /**
         * Operation defined by subclass for different event processing
         * @param event incoming event
         */
        abstract public void process(Event event);

        /**
         * Stop this loop
         */
        public void stop(){
            running = false;
            add(KILL_PILL);
        }
    }
}
