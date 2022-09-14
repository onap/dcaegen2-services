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

import lombok.NonNull;
import org.onap.slice.analysis.ms.aai.AaiService;

import org.onap.slice.analysis.ms.models.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    StrategyFactory strategyFactory;

    private Loop evaluationEventLoop;
    private Loop aaiEventLoop;

    private static final Event KILL_PILL = new SimpleEvent(null, 0);
    private static final int DEFAULT_EVAL_INTERVAL = 5;
    private static final String DEFAULT_STRATEGY_NAME = "FixedUpperBoundStrategy";

    /**
     * Interval of each round of evaluation, defined in config_all.json
     */
    private static int evaluationInterval;

    /**
     * Bandwidth Evaluation and adjustment strategy.
     */
    private static String strategyName;

    private final ScheduledExecutorService executorPool = Executors.newScheduledThreadPool(1);

    /**
     * Initialize and start the bandwidth evaluator process, schedule a periodic service bandwidth usage check
     */
    @PostConstruct
    public void init() {
        loadConfig();
        strategyName = (strategyName != null)? strategyName : DEFAULT_STRATEGY_NAME;
        evaluationInterval = (evaluationInterval == 0)? DEFAULT_EVAL_INTERVAL : evaluationInterval;
        EvaluationStrategy strategy = strategyFactory.getStrategy(strategyName);

        /**
         * Evalution main loop
         */
        evaluationEventLoop = new Loop("EvaluationLoop"){
            @Override
            public void process(Event event) {
                strategy.execute(event);
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

    // update configuration
    private void loadConfig() {
        configuration = Configuration.getInstance();
        evaluationInterval = configuration.getCcvpnEvalInterval();
        strategyName = configuration.getCcvpnEvalStrategy();
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
