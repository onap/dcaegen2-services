/*
 * ============LICENSE_START=======================================================
 * BBS-RELOCATION-CPE-AUTHENTICATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.bbs.event.processor.pipelines;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.config.ConfigurationChangeObserver;
import org.onap.bbs.event.processor.config.ConsulConfigurationGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class Scheduler implements ConfigurationChangeObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

    private static final int PIPELINES_INITIAL_DELAY_IN_SECONDS = 10;
    private static final int DEFAULT_PIPELINES_POLLING_INTERVAL = 15;
    private static final Duration CBS_INITIAL_DELAY = Duration.ofSeconds(1);
    private static final int DEFAULT_CBS_POLLING_INTERVAL = 30;

    private final ConsulConfigurationGateway configurationGateway;
    private final TaskScheduler taskScheduler;
    private final ReRegistrationPipeline reRegistrationPipeline;
    private final CpeAuthenticationPipeline cpeAuthenticationPipeline;
    private ApplicationConfiguration configuration;
    private Map<String, ScheduledFuture> processingScheduledTasks;
    private ScheduledFuture cbsScheduledTask;
    private int currentPipelinesPollingInterval;
    private int currentCbsPollingInterval;

    @Autowired
    Scheduler(ApplicationConfiguration configuration,
                     ConsulConfigurationGateway configurationGateway,
                     TaskScheduler taskScheduler,
                     ReRegistrationPipeline reRegistrationPipeline,
                     CpeAuthenticationPipeline cpeAuthenticationPipeline) {
        this.configuration = configuration;
        this.configurationGateway = configurationGateway;
        this.taskScheduler = taskScheduler;
        this.reRegistrationPipeline = reRegistrationPipeline;
        this.cpeAuthenticationPipeline = cpeAuthenticationPipeline;
        processingScheduledTasks = new ConcurrentHashMap<>();
    }

    /**
     * Sets up Scheduler.
     *
     * <p>Initiates CBS configuration fetch periodic task and DMaaP PNF re-registration and CPE
     * authentication event polling tasks.</p>
     *
     * <p>It also registers for application configuration changes notifications.</p>
     */
    @PostConstruct
    public void setupScheduler() {

        // Initiate periodic configuration fetching from CBS
        currentCbsPollingInterval = verifyCbsPollingInterval();
        cbsScheduledTask =
                taskScheduler.schedule(() -> configurationGateway.periodicallyFetchConfigFromCbs(CBS_INITIAL_DELAY,
                Duration.ofSeconds(currentCbsPollingInterval)), Instant.now());

        // Initiate Processing tasks
        currentPipelinesPollingInterval = validatePipelinesPollingInterval();
        LOGGER.info("BBS event processing pipelines will start in {} seconds "
                + "and will run periodically every {} seconds", PIPELINES_INITIAL_DELAY_IN_SECONDS,
                currentPipelinesPollingInterval);
        var desiredStartTime = Instant.now().plusSeconds(PIPELINES_INITIAL_DELAY_IN_SECONDS);
        scheduleProcessingTasks(desiredStartTime, currentPipelinesPollingInterval);

        // Register for configuration changes
        configuration.register(this);
    }

    /**
     * Un-registers from getting application configuration changes notifications.
     */
    @PreDestroy
    public void unRegisterAsConfigChangeObserver() {
        configuration.unRegister(this);
    }

    @Override
    public void updateConfiguration() {
        if (configuration.getPipelinesPollingIntervalInSeconds() != currentPipelinesPollingInterval) {
            LOGGER.info("Pipelines Polling interval has changed. Re-scheduling processing pipelines");
            cancelScheduledProcessingTasks();
            reScheduleProcessingTasks();
        }
        var newCbsPollingInterval = configuration.getCbsPollingInterval();
        if (newCbsPollingInterval != currentCbsPollingInterval) {
            if (newCbsPollingInterval < DEFAULT_CBS_POLLING_INTERVAL) {
                LOGGER.warn("CBS Polling interval is too small ({}). Will not re-schedule CBS job",
                        newCbsPollingInterval);
            } else {
                rescheduleCbsScheduledTask(newCbsPollingInterval);
            }
        }
    }

    private void rescheduleCbsScheduledTask(int newCbsPollingInterval) {
        LOGGER.info("CBS Polling interval has changed. Re-scheduling CBS job");
        currentCbsPollingInterval = newCbsPollingInterval;
        if (!cbsScheduledTask.isDone()) {
            if (cbsScheduledTask.cancel(true)) {
                LOGGER.debug("CBS task has been cancelled");
            } else {
                LOGGER.error("Error while cancelling CBS task. Task status (isDone/isCanceled) is ({}/{})",
                        cbsScheduledTask.isDone(), cbsScheduledTask.isCancelled());
            }
        }
        cbsScheduledTask = taskScheduler.schedule(() ->
                        configurationGateway.rescheduleCbsConfigurationRetrieval(CBS_INITIAL_DELAY,
                Duration.ofSeconds(currentCbsPollingInterval)), Instant.now());
    }

    /**
     * Cancels DMaaP polling tasks (PNF re-registration & CPE authentication).
     * @return Tasks cancellation result
     */
    public boolean cancelScheduledProcessingTasks() {

        if (processingScheduledTasks.isEmpty()) {
            LOGGER.debug("No tasks found to cancel");
            return true;
        }

        processingScheduledTasks.forEach((key, value) -> {
            if (value.cancel(false)) {
                LOGGER.debug("Task {} has been cancelled", key);
            } else {
                LOGGER.error("Error while cancelling task {}. Task status (isDone/isCanceled) is ({}/{})",
                        key, value.isDone(), value.isCancelled());
            }
        });
        processingScheduledTasks.entrySet().removeIf(entry -> entry.getValue().isCancelled());
        LOGGER.info("All cancelled tasks have been removed");
        return processingScheduledTasks.isEmpty();
    }

    /**
     * Reschedules DMaaP polling tasks (PNF re-registration & CPE authentication).
     * @return Tasks rescheduling result
     */
    public boolean reScheduleProcessingTasks() {

        if (processingScheduledTasks.size() != 0) {
            // If every task is cancelled, we can remove and re-schedule
            if (processingScheduledTasks.entrySet().stream()
                    .allMatch(e -> e.getValue().isCancelled())) {
                processingScheduledTasks.clear();
                LOGGER.debug("Old cancelled tasks have been removed");
            } else {
                LOGGER.error("Cannot reschedule. There are {} active tasks that must be first cancelled",
                        processingScheduledTasks.entrySet().stream()
                                .filter(e -> !e.getValue().isCancelled())
                                .count()
                );
                return false;
            }
        }
        currentPipelinesPollingInterval = validatePipelinesPollingInterval();
        LOGGER.info("Reschedule tasks");
        scheduleProcessingTasks(Instant.now(), currentPipelinesPollingInterval);
        return true;
    }

    int numberOfTotalTasks() {
        return processingScheduledTasks.size();
    }

    long numberOfActiveTasks() {
        return processingScheduledTasks.entrySet().stream()
                .filter(e -> (!e.getValue().isCancelled()))
                .count();
    }

    long numberOfCancelledTasks() {
        return processingScheduledTasks.entrySet().stream()
                .filter(e -> (e.getValue().isCancelled()))
                .count();
    }

    private void scheduleProcessingTasks(Instant desiredStartTime, int pollingInterval) {
        processingScheduledTasks.put("Re-registration",
                taskScheduler.scheduleAtFixedRate(reRegistrationPipeline::processPnfReRegistrationEvents,
                        desiredStartTime, Duration.ofSeconds(pollingInterval)));
        processingScheduledTasks.put("CPE Authentication",
                taskScheduler.scheduleAtFixedRate(cpeAuthenticationPipeline::processPnfCpeAuthenticationEvents,
                        desiredStartTime, Duration.ofSeconds(pollingInterval)));
    }

    private int validatePipelinesPollingInterval() {
        var pipelinesPollingInterval = configuration.getPipelinesPollingIntervalInSeconds();
        var isSmallInterval = pipelinesPollingInterval < DEFAULT_PIPELINES_POLLING_INTERVAL;
        var verifiedInterval = isSmallInterval ? DEFAULT_PIPELINES_POLLING_INTERVAL : pipelinesPollingInterval;
        if (isSmallInterval) {
            LOGGER.warn("Pipelines Polling interval is too small ({}). Defaulting to {}", pipelinesPollingInterval,
                    DEFAULT_PIPELINES_POLLING_INTERVAL);
        }
        return verifiedInterval;
    }

    private int verifyCbsPollingInterval() {
        var cbsPollingInterval = configuration.getCbsPollingInterval();
        var isSmallInterval = cbsPollingInterval < DEFAULT_CBS_POLLING_INTERVAL;
        var verifiedInterval = isSmallInterval ? DEFAULT_CBS_POLLING_INTERVAL : cbsPollingInterval;
        if (isSmallInterval) {
            LOGGER.warn("CBS Polling interval is too small ({}). Defaulting to {}", cbsPollingInterval,
                    DEFAULT_CBS_POLLING_INTERVAL);
        }
        return verifiedInterval;
    }
}
