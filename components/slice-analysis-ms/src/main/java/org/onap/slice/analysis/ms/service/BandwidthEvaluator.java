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
package org.onap.slice.analysis.ms.service;

import org.onap.slice.analysis.ms.configdb.AaiService;
import org.onap.slice.analysis.ms.models.ccvpnnotification.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

@Component
public class BandwidthEvaluator {
    private static Logger log = LoggerFactory.getLogger(BandwidthEvaluator.class);

    @Autowired
    AaiService aaiService;

    @Autowired
    CCVPNPmDatastore ccvpnPmDatastore;

    @Autowired
    PolicyService policyService;

    private Loop evalEvtLoop;
    private Loop aaiEvtLoop;

    private static final Event KILL_PILL = new SimpleEvent(null, 0);
    private static final int EVAL_INTERVAL = 5;
    private static final double THRESHOLD = 0.8;
    private static final double PRECISION = 100.0; // in Mbps; 100Mbps, 200Mbps, 1000Mbps...

    @PostConstruct
    public void init() {
        // AAI Event Actor
        aaiEvtLoop = new Loop("AAIEventLoop"){

            @Override
            public void process(Event event) {
                log.info("Received new AAI network policy query at: {}", event.time());
                String cllId = (String) event.subject();
                Map<String, Integer> maxBandwidthData = aaiService.fetchMaxBandwidthofService(cllId);
                int bwVal = maxBandwidthData.get("maxBandwidth");
                if (maxBandwidthData != null){
                    ccvpnPmDatastore.updateMaxBw(cllId, bwVal);
                }
            }
        };
        // Evaluation Event Actor
        evalEvtLoop = new Loop("EvaluationLoop"){
            @Override
            public void process(Event event) {
                if (event.type() == SimpleEvent.Type.PERIODIC_CHECK){
                    log.info("Received new periodic check request: {}", event.time());
                    ConcurrentMap<String, ServiceState> statusMap =  ccvpnPmDatastore.getSvcStatusMap();
                    for(Map.Entry<String, ServiceState>entry: statusMap.entrySet()){
                        if (entry.getValue() == ServiceState.RUNNING) {
                            // Service is running, used bw data syncing
                            String cllId = entry.getKey();
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
                                            sendModifyRequest(cllId, newBw);
                                            break;
                                        }
                                    }
                                }

                            } else {
                                post(new SimpleEvent(SimpleEvent.Type.AAI_BW_REQ, cllId));
                            }
                        }
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

    @PreDestroy
    public void deactive(){
        aaiEvtLoop.stop();
        evalEvtLoop.stop();
    }

    public void scheduleEvaluation(){
        ScheduledExecutorService executorPool = Executors.newScheduledThreadPool(1);
        executorPool.scheduleAtFixedRate(new Runnable() {
                                             @Override
                                             public void run() {
                                                 post(new SimpleEvent(SimpleEvent.Type.PERIODIC_CHECK, 1));
                                             }
                                         }, 0, EVAL_INTERVAL,
                TimeUnit.SECONDS);
    }

    public void post(Event event){
        if (event.type() == SimpleEvent.Type.AAI_BW_REQ){
            aaiEvtLoop.add(event);
        } else if (event.type() == SimpleEvent.Type.PERIODIC_CHECK
                 || event.type() == SimpleEvent.Type.ONDEMAND_CHECK ){
            evalEvtLoop.add(event);
        }
    }

    private abstract class Loop implements Runnable {
        private final String name;
        private volatile boolean running;
        private final BlockingQueue<Event> eventsQueue;
        private final ExecutorService executor;
        private volatile Future<?> dispatchFuture;

        Loop(String name){
            this.name = name;
            executor = Executors.newSingleThreadExecutor();
            eventsQueue = new LinkedBlockingQueue<>();
            dispatchFuture = executor.submit(this);
        }

        public boolean add(Event evt) {
            return eventsQueue.add(evt);
        }

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
                    log.warn("Process loop hit an error");
                }

            }
        }
        // Actions for two different event processing
        abstract public void process(Event event);

        void stop(){
            running = false;
            add(KILL_PILL);
        }
    }
}
