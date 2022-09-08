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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.config.ConsulConfigurationGateway;
import org.springframework.scheduling.TaskScheduler;

// We can safely suppress unchecked assignment warnings for the ScheduledFuture mock
@SuppressWarnings("unchecked")
@DisplayName("Application Task Scheduler Unit-Tests")
class SchedulerTest {

    private Scheduler applicationScheduler;
    private ApplicationConfiguration configuration;
    private TaskScheduler taskScheduler;

    SchedulerTest() {
        configuration = Mockito.mock(ApplicationConfiguration.class);
        taskScheduler = Mockito.mock(TaskScheduler.class);
        var reRegistrationPipeline = Mockito.mock(ReRegistrationPipeline.class);
        var cpeAuthenticationPipeline = Mockito.mock(CpeAuthenticationPipeline.class);
        var configurationGateway = Mockito.mock(ConsulConfigurationGateway.class);
        this.applicationScheduler = new Scheduler(configuration, configurationGateway, taskScheduler,
                reRegistrationPipeline, cpeAuthenticationPipeline);
    }

    @Test
    void scheduleTasksWithValidSchedulingPeriod_Succeeds() {
        when(configuration.getPipelinesPollingIntervalInSeconds()).thenReturn(20);
        var scheduledFuture = Mockito.mock(ScheduledFuture.class);
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any(Instant.class), any(Duration.class)))
               .thenReturn(scheduledFuture);

        applicationScheduler.setupScheduler();
        assertAll("Scheduler with valid Scheduling period",
            () -> assertEquals(2, applicationScheduler.numberOfTotalTasks(), "Total tasks"),
            () -> assertEquals(2, applicationScheduler.numberOfActiveTasks(), "Active tasks"),
            () -> assertEquals(0, applicationScheduler.numberOfCancelledTasks(), "Cancelled tasks")
        );
    }

    @Test
    void cancellingRunningTasksSucceeds_tasksAreDeleted() {
        when(configuration.getPipelinesPollingIntervalInSeconds()).thenReturn(20);
        var scheduledFuture1 = Mockito.mock(ScheduledFuture.class);
        var scheduledFuture2 = Mockito.mock(ScheduledFuture.class);
        when(scheduledFuture1.cancel(false)).thenReturn(true);
        when(scheduledFuture2.cancel(false)).thenReturn(true);
        when(scheduledFuture1.isCancelled()).thenReturn(true);
        when(scheduledFuture2.isCancelled()).thenReturn(true);
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any(Instant.class), any(Duration.class)))
                .thenReturn(scheduledFuture1).thenReturn(scheduledFuture2);

        applicationScheduler.setupScheduler();
        var result = applicationScheduler.cancelScheduledProcessingTasks();
        assertAll("Successfully cancelling tasks",
            () -> assertTrue(result, "Result of cancellation task"),
            () -> assertEquals(0, applicationScheduler.numberOfTotalTasks(), "Total tasks"),
            () -> assertEquals(0, applicationScheduler.numberOfActiveTasks(), "Active tasks"),
            () -> assertEquals(0, applicationScheduler.numberOfCancelledTasks(), "Cancelled tasks")
        );
    }

    @Test
    void cancellingRunningTasksPartiallyFailing_tasksAreNotDeleted() {
        when(configuration.getPipelinesPollingIntervalInSeconds()).thenReturn(20);
        var scheduledFuture1 = Mockito.mock(ScheduledFuture.class);
        var scheduledFuture2 = Mockito.mock(ScheduledFuture.class);
        when(scheduledFuture1.cancel(false)).thenReturn(true);
        when(scheduledFuture2.cancel(false)).thenReturn(false);
        when(scheduledFuture1.isCancelled()).thenReturn(true);
        when(scheduledFuture2.isCancelled()).thenReturn(false);
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any(Instant.class), any(Duration.class)))
                .thenReturn(scheduledFuture1).thenReturn(scheduledFuture2);

        applicationScheduler.setupScheduler();
        var result = applicationScheduler.cancelScheduledProcessingTasks();
        assertAll("Partially cancelling tasks",
            () -> assertFalse(result, "Result of cancellation task"),
            () -> assertEquals(1, applicationScheduler.numberOfTotalTasks(), "Total tasks"),
            () -> assertEquals(1, applicationScheduler.numberOfActiveTasks(), "Active tasks"),
            () -> assertEquals(0, applicationScheduler.numberOfCancelledTasks(), "Cancelled tasks")
        );
    }

    @Test
    void cancellingRunningTasksFailingForAllOfThem_noTasksAreDeleted() {
        when(configuration.getPipelinesPollingIntervalInSeconds()).thenReturn(20);
        var scheduledFuture1 = Mockito.mock(ScheduledFuture.class);
        var scheduledFuture2 = Mockito.mock(ScheduledFuture.class);
        when(scheduledFuture1.cancel(false)).thenReturn(false);
        when(scheduledFuture2.cancel(false)).thenReturn(false);
        when(scheduledFuture1.isCancelled()).thenReturn(false);
        when(scheduledFuture2.isCancelled()).thenReturn(false);
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any(Instant.class), any(Duration.class)))
                .thenReturn(scheduledFuture1).thenReturn(scheduledFuture2);

        applicationScheduler.setupScheduler();
        var result = applicationScheduler.cancelScheduledProcessingTasks();
        assertAll("Failing in cancelling tasks",
            () -> assertFalse(result, "Result of cancellation task"),
            () -> assertEquals(2, applicationScheduler.numberOfTotalTasks(), "Total tasks"),
            () -> assertEquals(2, applicationScheduler.numberOfActiveTasks(), "Active tasks"),
            () -> assertEquals(0, applicationScheduler.numberOfCancelledTasks(), "Cancelled tasks")
        );
    }

    @Test
    void reSchedulingWithExistingActiveTasks_Fails() {
        when(configuration.getPipelinesPollingIntervalInSeconds()).thenReturn(20);
        var scheduledFuture1 = Mockito.mock(ScheduledFuture.class);
        var scheduledFuture2 = Mockito.mock(ScheduledFuture.class);
        when(scheduledFuture1.isCancelled()).thenReturn(false);
        when(scheduledFuture2.isCancelled()).thenReturn(false);
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any(Instant.class), any(Duration.class)))
                .thenReturn(scheduledFuture1).thenReturn(scheduledFuture2);

        applicationScheduler.setupScheduler();
        var result = applicationScheduler.reScheduleProcessingTasks();
        assertAll("Rescheduling with active tasks",
            () -> assertFalse(result, "Result of re-scheduling"),
            () -> assertEquals(2, applicationScheduler.numberOfTotalTasks(), "Total tasks"),
            () -> assertEquals(2, applicationScheduler.numberOfActiveTasks(), "Active tasks"),
            () -> assertEquals(0, applicationScheduler.numberOfCancelledTasks(), "Cancelled tasks")
        );
    }

    @Test
    void reSchedulingWithExistingCancelledTasks_Succeeds() {
        when(configuration.getPipelinesPollingIntervalInSeconds()).thenReturn(20);
        // Initial tasks
        var scheduledFuture1 = Mockito.mock(ScheduledFuture.class);
        var scheduledFuture2 = Mockito.mock(ScheduledFuture.class);
        // Re-scheduled tasks
        var scheduledFuture3 = Mockito.mock(ScheduledFuture.class);
        var scheduledFuture4 = Mockito.mock(ScheduledFuture.class);
        when(scheduledFuture1.isCancelled()).thenReturn(true);
        when(scheduledFuture2.isCancelled()).thenReturn(true);
        when(scheduledFuture3.isCancelled()).thenReturn(false);
        when(scheduledFuture4.isCancelled()).thenReturn(false);
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any(Instant.class), any(Duration.class)))
                .thenReturn(scheduledFuture1)
                .thenReturn(scheduledFuture2)
                .thenReturn(scheduledFuture3)
                .thenReturn(scheduledFuture4);

        applicationScheduler.setupScheduler();
        var result = applicationScheduler.reScheduleProcessingTasks();
        assertAll("Rescheduling with cancelled tasks",
            () -> assertTrue(result, "Result of re-scheduling"),
            () -> assertEquals(2, applicationScheduler.numberOfTotalTasks(), "Total tasks"),
            () -> assertEquals(2, applicationScheduler.numberOfActiveTasks(), "Active tasks"),
            () -> assertEquals(0, applicationScheduler.numberOfCancelledTasks(), "Cancelled tasks")
        );
    }
}