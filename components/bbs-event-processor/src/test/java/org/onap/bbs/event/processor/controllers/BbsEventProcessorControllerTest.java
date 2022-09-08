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

package org.onap.bbs.event.processor.controllers;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.pipelines.CpeAuthenticationPipeline;
import org.onap.bbs.event.processor.pipelines.ReRegistrationPipeline;
import org.onap.bbs.event.processor.pipelines.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BbsEventProcessorController.class)
@DisplayName("BBS Event Processor Controllers MVC Unit-Tests")
class BbsEventProcessorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReRegistrationPipeline reRegistrationPipeline;
    @Autowired
    private CpeAuthenticationPipeline cpeAuthenticationPipeline;
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private ApplicationConfiguration configuration;

    @BeforeEach
    void resetInteractions() {
        Mockito.reset(scheduler);
    }

    @Test
    void sendingHeartBeatRestCall_RespondsWithAlive() throws Exception {
        var heartBeatResult = mockMvc.perform(get("/heartbeat")).andReturn();

        mockMvc.perform(asyncDispatch(heartBeatResult))
                .andExpect(status().isOk())
                .andExpect(content().string("bbs-event-processor is alive\n"));
    }

    @Test
    void sendingReRegistrationSubmissionRestCall_RespondsWithOk() throws Exception {
        var reregistrationSubmissionResult = mockMvc.perform(post("/poll-reregistration-events")).andReturn();

        mockMvc.perform(asyncDispatch(reregistrationSubmissionResult))
                .andExpect(status().isOk())
                .andExpect(content().string("Request submitted\n"));
        verify(reRegistrationPipeline, timeout(500)).processPnfReRegistrationEvents();
    }

    @Test
    void sendingCpeAuthenticationSubmissionRestCall_RespondsWithOk() throws Exception {
        var reregistrationSubmissionResult = mockMvc.perform(post("/poll-cpe-authentication-events")).andReturn();

        mockMvc.perform(asyncDispatch(reregistrationSubmissionResult))
                .andExpect(status().isOk())
                .andExpect(content().string("Request submitted\n"));
        verify(cpeAuthenticationPipeline, timeout(500)).processPnfCpeAuthenticationEvents();
    }

    @Test
    void sendingStartTasksRestCall_ifItSucceeds_RespondsWithOk() throws Exception {
        when(scheduler.reScheduleProcessingTasks()).thenReturn(true);
        var startTasksResult = mockMvc.perform(post("/start-tasks")).andReturn();

        mockMvc.perform(asyncDispatch(startTasksResult))
                .andExpect(status().isOk())
                .andExpect(content().string("Initiation of tasks was successful\n"));
        verify(scheduler).reScheduleProcessingTasks();
    }

    @Test
    void sendingStartTasksRestCall_ifItFails_RespondsWithNotAcceptable() throws Exception {
        when(scheduler.reScheduleProcessingTasks()).thenReturn(false);
        var startTasksResult = mockMvc.perform(post("/start-tasks")).andReturn();

        mockMvc.perform(asyncDispatch(startTasksResult))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().string("Initiation of tasks failed\n"));
        verify(scheduler).reScheduleProcessingTasks();
    }

    @Test
    void sendingCancelTasksRestCall_ifItSucceeds_RespondsWithOk() throws Exception {
        when(scheduler.cancelScheduledProcessingTasks()).thenReturn(true);
        var cancellationResult = mockMvc.perform(post("/cancel-tasks")).andReturn();

        mockMvc.perform(asyncDispatch(cancellationResult))
                .andExpect(status().isOk())
                .andExpect(content().string("Cancellation was successful\n"));
        verify(scheduler).cancelScheduledProcessingTasks();
    }

    @Test
    void sendingCancelTasksRestCall_ifItFails_RespondsWithNotAcceptable() throws Exception {
        when(scheduler.cancelScheduledProcessingTasks()).thenReturn(false);
        var cancellationResult = mockMvc.perform(post("/cancel-tasks")).andReturn();

        mockMvc.perform(asyncDispatch(cancellationResult))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().string("Cancellation failed\n"));
        verify(scheduler).cancelScheduledProcessingTasks();
    }

    @TestConfiguration
    static class ControllerTestConfiguration {
        @Bean
        ReRegistrationPipeline reRegistrationPipeline() {
            return Mockito.mock(ReRegistrationPipeline.class);
        }

        @Bean
        CpeAuthenticationPipeline cpeAuthenticationPipeline() {
            return Mockito.mock(CpeAuthenticationPipeline.class);
        }

        @Bean
        Scheduler scheduler() {
            return Mockito.mock(Scheduler.class);
        }

        @Bean
        ApplicationConfiguration configuration() {
            return Mockito.mock(ApplicationConfiguration.class);
        }
    }
}