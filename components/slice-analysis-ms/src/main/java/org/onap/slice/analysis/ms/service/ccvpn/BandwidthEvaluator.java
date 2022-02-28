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
package org.onap.slice.analysis.ms.service.ccvpn;

import org.onap.slice.analysis.ms.aai.AaiService;

import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.service.PolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * This class implements the CCVPN PM Closed-loop logical function.
 * A simple actor model design is implemented here.
 */
@Component
public class BandwidthEvaluator {
    private static Logger log = LoggerFactory.getLogger(BandwidthEvaluator.class);
    private Configuration configuration;

    @Autowired
    AaiService aaiService;

    @Autowired
    CCVPNPmDatastore ccvpnPmDatastore;

    @Autowired
    PolicyService policyService;

    private Loop evaluationEventLoop;
    private Loop aaiEventLoop;

    private static final Event KILL_PILL = new SimpleEvent(null, 0);
    private static int EVAL_INTERVAL;
    private static double THRESHOLD;
    private static double PRECISION; // in Mbps;
    private final ScheduledExecutorService executorPool = Executors.newScheduledThreadPool(1);

    /**
     * Initialize and start the bandwidth evaluator process, schedule a periodic service bandwidth usage check
     */
    @PostConstruct
    public void init() {
        loadConfig();
        // AAI Event Actor
        aaiEventLoop = new Loop("AAIEventLoop"){

            @Override
            public void process(Event event) {
                if (event.type() == SimpleEvent.Type.AAI_BW_REQ){
                    log.info("Received new AAI network policy query at: {}", event.time());
                    String cllId = (String) event.subject();
                    Map<String, Integer> maxBandwidthData = aaiService.fetchMaxBandwidthOfService(cllId);
                    int bwVal = maxBandwidthData.get("maxBandwidth");
                    if (maxBandwidthData != null){
                        ServiceState state = ccvpnPmDatastore.getStatusOfSvc(cllId);
                        if (state == ServiceState.BEING_MAINTAINED){
                            if (ccvpnPmDatastore.updateMaxBw(cllId, bwVal, true)){
                                ccvpnPmDatastore.updateSvcState(cllId, ServiceState.RUNNING);
                            }
                        } else if (state == ServiceState.RUNNING){
                            ccvpnPmDatastore.updateMaxBw(cllId, bwVal, false);
                        }

                    }
                }

            }
        };
        // Evaluation Event Actor
        evaluationEventLoop = new Loop("EvaluationLoop"){
            @Override
            public void process(Event event) {
                if (event.type() == SimpleEvent.Type.PERIODIC_CHECK){
                    log.info("Received new periodic check request: {}", event.time());
                    ConcurrentMap<String, ServiceState> statusMap =  ccvpnPmDatastore.getSvcStatusMap();
                    List<String> svcBeingMaintained = new ArrayList<>();
                    for(Map.Entry<String, ServiceState>entry: statusMap.entrySet()){
                        String cllId = entry.getKey();
                        if (entry.getValue() == ServiceState.RUNNING) {
                            // Service is running, used bw data syncing

                            if (ccvpnPmDatastore.getMaxBwOfSvc(cllId) != 0){
                                // Max bw cached
                                Map<Endpointkey, CCVPNPmDatastore.EvictingQueue<Integer>> usedBwMap
                                        = ccvpnPmDatastore.getUsedBwOfSvc(cllId);
                                for(Map.Entry<Endpointkey, CCVPNPmDatastore.EvictingQueue<Integer>> entry2
                                        : usedBwMap.entrySet()){
                                    Object [] usedBws = entry2.getValue().tryReadToArray();
                                    if (usedBws != null){
                                        double avg =
                                                Arrays.stream(usedBws)
                                                        .mapToInt(o -> (int)o)
                                                        .summaryStatistics()
                                                        .getAverage();
                                        if (avg > THRESHOLD * ccvpnPmDatastore.getMaxBwOfSvc(cllId)){
                                            int newBw = (int) (Math.ceil((avg / THRESHOLD) / PRECISION) * PRECISION);
                                            svcBeingMaintained.add(cllId);
                                            sendModifyRequest(cllId, newBw);
                                            break;
                                        }
                                    }
                                }

                            } else {
                                post(new SimpleEvent(SimpleEvent.Type.AAI_BW_REQ, cllId));
                            }
                        } else if (entry.getValue() == ServiceState.BEING_MAINTAINED){
                            post(new SimpleEvent(SimpleEvent.Type.AAI_BW_REQ, cllId));
                        }
                    }
                    // Put svc being maintained to BEING_MAINTAINED STATE
                    for (String svc: svcBeingMaintained){
                        ccvpnPmDatastore.updateSvcState(svc, ServiceState.BEING_MAINTAINED);
                    }

                } else if (event.type() == SimpleEvent.Type.ONDEMAND_CHECK) {
                    log.info("Received new on-demand check request: {}", event.time());
                }
            }

            public void sendModifyRequest(String cllId, Integer newBandwidth){
                policyService.sendOnsetMessageToPolicy(
                        policyService.formPolicyOnsetMessageForCCVPN(cllId, newBandwidth)
                );
            }
        };
        scheduleEvaluation();
    }

    /**
     * Stop the bandwidth evaluator process including two actors and periodic usage check
     */
    @PreDestroy
    public void stop(){
        stopScheduleEvaluation();
        aaiEventLoop.stop();
        evaluationEventLoop.stop();
    }

    /**
     * Start to schedule periodic usage check at fixed rate
     */
    private void scheduleEvaluation(){
        executorPool.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    post(new SimpleEvent(SimpleEvent.Type.PERIODIC_CHECK, 1));
                }
            }, 0, (EVAL_INTERVAL == 0? 5 : EVAL_INTERVAL), TimeUnit.SECONDS);
    }

    /**
     * Stop periodic bandwidth usage check
     */
    private void stopScheduleEvaluation(){
        executorPool.shutdownNow();
    }

    /**
     * Post/broadcast event between Loops
     * @param event event object
     */
    public void post(Event event){
        if (event.type() == SimpleEvent.Type.AAI_BW_REQ){
            aaiEventLoop.add(event);
        } else if (event.type() == SimpleEvent.Type.PERIODIC_CHECK
                || event.type() == SimpleEvent.Type.ONDEMAND_CHECK ){
            evaluationEventLoop.add(event);
        }
    }

    private void loadConfig(){
        configuration = Configuration.getInstance();
        EVAL_INTERVAL = configuration.getCcvpnEvalInterval();
        THRESHOLD = configuration.getCcvpnEvalThreshold();
        PRECISION = configuration.getCcvpnEvalPrecision(); // in Mbps;
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
            log.info("BandwidthEvaluator -- {} initiated", this.name);
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
