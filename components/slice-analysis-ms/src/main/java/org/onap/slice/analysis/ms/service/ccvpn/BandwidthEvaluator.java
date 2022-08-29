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

import com.google.gson.JsonObject;
import lombok.NonNull;
import org.onap.slice.analysis.ms.aai.AaiService;

import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.service.PolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


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
    private static final int DEFAULT_EVAL_INTERVAL = 5;
    private static final String SERVICE_INSTANCE_LOCATION_ID = "service-instance-location-id";
    private static final String BANDWIDTH_TOTAL = "bandwidth-total";

    /**
     * Interval of each round of evaluation, defined in config_all.json
     */
    private static int evaluationInterval;

    /**
     * Percentage threshold of bandwidth adjustment.
     */
    private static double threshold;

    /**
     * Precision of bandwidth evaluation and adjustment.
     */
    private static double precision; // in Mbps;
    private final ScheduledExecutorService executorPool = Executors.newScheduledThreadPool(1);

    /**
     * Initialize and start the bandwidth evaluator process, schedule a periodic service bandwidth usage check
     */
    @PostConstruct
    public void init() {
        loadConfig();
        /**
         * Evalution main loop
         */
        evaluationEventLoop = new Loop("EvaluationLoop"){
            @Override
            public void process(Event event) {
                if (event.type() == SimpleEvent.Type.PERIODIC_CHECK && isPeriodicCheckOn()){
                    log.debug("=== Processing new periodic check request: {} ===", event.time());
                    Map<Endpointkey, CCVPNPmDatastore.EvictingQueue<Integer>> usedBwMap = ccvpnPmDatastore.getUsedBwMap();
                    Map<String, Integer> candidate = new TreeMap<>();
                    for(Map.Entry<Endpointkey, CCVPNPmDatastore.EvictingQueue<Integer>> entry: usedBwMap.entrySet()) {
                        String serviceId = entry.getKey().getCllId();
                        Object[] usedBws = entry.getValue().tryReadToArray();
                        if (!ccvpnPmDatastore.getClosedloopStatus(serviceId)) {
                            log.debug("CCVPN Evaluator Output: service {}, closed loop bw modification is off.", serviceId);
                            continue;
                        }
                        if (usedBws == null) {
                            // No enough data for evaluating
                            log.debug("CCVPN Evaluator Output: service {}, not enough data to evaluate", serviceId);
                            continue;
                        }
                        if (ccvpnPmDatastore.getProvBwOfSvc(serviceId) == 0) {
                            // Max bandwidth not cached yet
                            log.debug("CCVPN Evaluator Output: service {}, max bandwidth not cached, wait for next round", serviceId);
                            post(new SimpleEvent(SimpleEvent.Type.AAI_BW_REQ, serviceId));
                            continue;
                        }
                        double avg = Arrays.stream(usedBws)

                                           .mapToInt(o -> (int) o)
                                           .summaryStatistics()
                                           .getAverage();
                        int provBw = ccvpnPmDatastore.getProvBwOfSvc(serviceId);
                        int upperBw = ccvpnPmDatastore.getUpperBoundBwOfSvc(serviceId);
                        int originalBw = ccvpnPmDatastore.getOriginalBw(serviceId);

                        if(needIncrease(serviceId, avg, provBw, upperBw)){
                            int newBw = (int) (Math.ceil((avg / threshold) * 1.2 / precision) * precision);
                            log.info("For cll {}, going to increase bw to {}", serviceId, newBw);
                            candidate.put(serviceId, Math.max(candidate.getOrDefault(serviceId, 0), newBw));
                        } else {
                            if(needDecrease(serviceId, avg, provBw, originalBw)) {
                                int newBw = originalBw == 0 ? (int) (Math.ceil(upperBw * 0.5) ): originalBw;
                                log.info("For cll {}, going to decrease bw to {}", serviceId, newBw);
                                candidate.put(serviceId, Math.max(candidate.getOrDefault(serviceId, 0), newBw));
                            }
                        }
                    }
                    // check svc under maintenance
                    Map<String , ServiceState> svcUnderMaintenance = getServicesUnderMaintenance();
                    for (Map.Entry<String, ServiceState> entry: svcUnderMaintenance.entrySet()){
                        candidate.putIfAbsent(entry.getKey(), 0);
                    }
                    // fetch the provisioned bandwidth info if underMaintenance; otherwise send modification request
                    for(Map.Entry<String, Integer> entry: candidate.entrySet()) {
                        String cllId = entry.getKey();
                        Integer newBw = entry.getValue();
                        if(!ccvpnPmDatastore.getClosedloopStatus(cllId)) {
                            log.debug("CCVPN Evaluator Output: service {}, closed loop triggered bw modification is off", cllId);
                            continue;
                        }
                        if (isServiceUnderMaintenance(cllId)) {
                            if (newBw == 0){
                                log.debug("CCVPN Evaluator Output: service {}," +
                                    " are in maintenance state, fetching bandwidth info from AAI", cllId);
                            } else {
                                log.debug("CCVPN Evaluator Output: candidate {}," +
                                    " need adjustment, but skipped due to maintenance state", cllId);
                            }
                            post(new SimpleEvent(SimpleEvent.Type.AAI_BW_REQ, cllId));
                            continue;
                        }
                        log.debug("CCVPN Evaluator Output: candidate {}," +
                            " need adjustment, sending request to policy", cllId);
                        ccvpnPmDatastore.updateSvcState(cllId, ServiceState.UNDER_MAINTENANCE);
                        sendModifyRequest(cllId, newBw, RequestOwner.DCAE);
                    }
                    log.debug("=== Processing periodic check complete ===");

                }

                if (event.type() == SimpleEvent.Type.ONDEMAND_CHECK && isOnDemandCheckOn()) {
                    log.debug("=== Processing upperbound adjustment request: {} ===", event.time());
                    JsonObject payload = (JsonObject) event.subject();
                    String serviceId = payload.get(SERVICE_INSTANCE_LOCATION_ID).getAsString();

                    int newBandwidth = payload.get(BANDWIDTH_TOTAL).getAsInt();
                    log.info("Update service {} bandwidth upperbound to {} ", serviceId, newBandwidth);
                    ccvpnPmDatastore.updateUpperBoundBw(serviceId, newBandwidth);
                    log.debug("=== Processing upperbound adjustment complete ===");
                }
            }

            // send modification requestion
            private void sendModifyRequest(String cllId, Integer newBandwidth, RequestOwner owner) {
                log.info("Sending modification request to policy. RequestOwner: {} - Service: {} change to bw: {}",
                    owner, cllId, newBandwidth);
                policyService.sendOnsetMessageToPolicy(
                    policyService.formPolicyOnsetMessageForCCVPN(cllId, newBandwidth, owner)
                );
            }

            private boolean needIncrease(String serviceId, double currAvgUsage, int provBw, int upperBoundBw) {
                log.info("cll {} judge whether increase", serviceId);
                if ( currAvgUsage > 0.9 * provBw && ( provBw * 1.2 < upperBoundBw)) {
                    log.info("decide to increase, original bw {}, currAvg bw {}, maxBw {}", currAvgUsage, provBw, upperBoundBw);
                    return true;
                }
                return false;
            }

            private boolean needDecrease(String serviceId, double currAvgUsage, int provBw, int originalBw) {
                log.info("cll {} judge whether decrease", serviceId);
                if(originalBw == 0 ? currAvgUsage < 0.3 * provBw : currAvgUsage < 0.3 * originalBw) {
                    log.info("decide to decrease, original bw {}, currAvg bw {}, maxBw {}", originalBw, currAvgUsage, provBw);
                    return true;
                }
                return false;
            }

            // check is service under main
            private boolean isServiceUnderMaintenance(String serivceId) {
                return ccvpnPmDatastore.getStatusOfSvc(serivceId) == ServiceState.UNDER_MAINTENANCE;
            }
            // get a collection of service under maint
            private Map<String, ServiceState> getServicesUnderMaintenance(){
                return ccvpnPmDatastore.getSvcStatusMap().entrySet()
                    .stream()
                    .filter(e -> e.getValue() == ServiceState.UNDER_MAINTENANCE)
                    .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
            }
        };

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
                        log.info("Successfully retrieved bandwidth info from AAI; service: {}, bandwidth: {}",
                                serviceId, maxBandwidthData.get("maxBandwidth"));
                        int bwValue = maxBandwidthData.get("maxBandwidth").intValue();
                        if (ccvpnPmDatastore.getProvBwOfSvc(serviceId) == 0){
                            ccvpnPmDatastore.updateProvBw(serviceId, bwValue, true);
                        } else if (ccvpnPmDatastore.getProvBwOfSvc(serviceId) != bwValue) {
                            log.info("Service modification complete; serviceId: {} with new bandwidth: {}", serviceId, bwValue);
                            ccvpnPmDatastore.updateProvBw(serviceId, bwValue, true);
                            ccvpnPmDatastore.updateSvcState(serviceId, ServiceState.RUNNING);
                            ccvpnPmDatastore.updateClosedloopStatus(serviceId, true);
                        }
                    }
                    log.debug("=== Processing AAI network policy query complete ===");
                }
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
        }, 0, (evaluationInterval == 0? DEFAULT_EVAL_INTERVAL : evaluationInterval), TimeUnit.SECONDS);
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
    public void post(@NonNull Event event){
        log.debug("A new event triggered, type: {}, subject: {}, at time: {}",
            event.type(), event.subject(), event.time());
        if (event.type() == SimpleEvent.Type.AAI_BW_REQ) {
            aaiEventLoop.add(event);
        } else if (event.type() == SimpleEvent.Type.PERIODIC_CHECK) {
            evaluationEventLoop.add(event);
        } else if (event.type() == SimpleEvent.Type.ONDEMAND_CHECK) {
            evaluationEventLoop.add(event);
        }
    }

    private void loadConfig() {
        configuration = Configuration.getInstance();
        evaluationInterval = configuration.getCcvpnEvalInterval();
        threshold = configuration.getCcvpnEvalThreshold();
        precision = configuration.getCcvpnEvalPrecision(); // in Mbps;
    }

    private boolean isPeriodicCheckOn() {
        configuration = Configuration.getInstance();
        return configuration.isCcvpnEvalPeriodicCheckOn();
    }

    private boolean isOnDemandCheckOn() {
        configuration = Configuration.getInstance();
        return configuration.isCcvpnEvalOnDemandCheckOn();
    }

    private boolean isFlexibleThresholdOn() {
        configuration = Configuration.getInstance();
        return configuration.isCcvpnEvalFlexibleThreshold();
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
