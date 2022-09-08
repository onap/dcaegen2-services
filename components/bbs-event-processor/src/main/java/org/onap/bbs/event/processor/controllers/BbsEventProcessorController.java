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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.concurrent.Executors;

import org.onap.bbs.event.processor.pipelines.CpeAuthenticationPipeline;
import org.onap.bbs.event.processor.pipelines.ReRegistrationPipeline;
import org.onap.bbs.event.processor.pipelines.Scheduler;
import org.onap.bbs.event.processor.utilities.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@Api(value = "BbsEventProcessorController", description = "Manage bbs-event-processor microService")
public class BbsEventProcessorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BbsEventProcessorController.class);

    private ReRegistrationPipeline reRegistrationPipeline;
    private CpeAuthenticationPipeline cpeAuthenticationPipeline;
    private Scheduler scheduler;

    /**
     * Constructs BBE event processor REST controller.
     * @param reRegistrationPipeline processing pipeline for polling DMaaP for PNF re-registration events
     * @param cpeAuthenticationPipeline processing pipeline for polling DMaaP for CPE authentication events
     * @param scheduler application scheduler
     */
    @Autowired
    public BbsEventProcessorController(ReRegistrationPipeline reRegistrationPipeline,
                                       CpeAuthenticationPipeline cpeAuthenticationPipeline,
                                       Scheduler scheduler) {
        this.reRegistrationPipeline = reRegistrationPipeline;
        this.cpeAuthenticationPipeline = cpeAuthenticationPipeline;
        this.scheduler = scheduler;
    }

    /**
     * Responds to health-check heartbeats.
     * @return Proper HTTP response based on application health
     */
    @GetMapping("heartbeat")
    @ApiOperation(value = "Returns liveness of bbs-event-processor microService")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "bbs-event-processor microService is alive"),
            @ApiResponse(code = 401, message = "Not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Resource access is forbidden"),
            @ApiResponse(code = 404, message = "Resource is not found")})
    public Mono<ResponseEntity<String>> handleHeartBeat() {
        LOGGER.debug("bbs-event-processor has received a heartbeat request");
        return Mono.defer(() ->
                Mono.just(new ResponseEntity<>("bbs-event-processor is alive\n", HttpStatus.OK))
        );
    }

    /**
     * Polls DMaaP for PNF re-registration events just once.
     * @return Proper HTTP response based on request submission result
     */
    @PostMapping("poll-reregistration-events")
    @ApiOperation(value = "Returns result of request submission. PNF re-registration polling will occur asynchronously")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Polling Re-registration events task submitted successfully"),
            @ApiResponse(code = 401, message = "Not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Resource access is forbidden"),
            @ApiResponse(code = 404, message = "Resource is not found")})
    public Mono<ResponseEntity<String>> handleReRegistrationRestCall() {
        LOGGER.debug("bbs-event-processor has received a re-registration handling request");
        Executors.newSingleThreadExecutor().submit(() -> reRegistrationPipeline.processPnfReRegistrationEvents());
        return Mono.defer(() ->
                Mono.just(new ResponseEntity<>("Request submitted\n", HttpStatus.OK))
        );
    }

    /**
     * Polls DMaaP for CPE authentication events just once.
     * @return Proper HTTP response based on request submission result
     */
    @PostMapping("poll-cpe-authentication-events")
    @ApiOperation(value = "Returns result of request submission. CPE authentication polling will occur asynchronously")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "CPE authentication task submitted successfully"),
            @ApiResponse(code = 401, message = "Not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Resource access is forbidden"),
            @ApiResponse(code = 404, message = "Resource is not found")})
    public Mono<ResponseEntity<String>> handleCpeAuthenticationRestCall() {
        LOGGER.debug("bbs-event-processor has received a cpe-authentication handling request");
        Executors.newSingleThreadExecutor().submit(() -> cpeAuthenticationPipeline.processPnfCpeAuthenticationEvents());
        return Mono.defer(() ->
                Mono.just(new ResponseEntity<>("Request submitted\n", HttpStatus.OK))
        );
    }

    /**
     * Reschedules DMaaP polling tasks.
     * @return Proper HTTP response based on rescheduling result
     */
    @PostMapping("start-tasks")
    @ApiOperation(value = "Returns result of request to start microservice tasks")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Tasks were successfully started"),
            @ApiResponse(code = 401, message = "Not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Resource access is forbidden"),
            @ApiResponse(code = 404, message = "Resource is not found"),
            @ApiResponse(code = 406, message = "Task initiation failed. Check logs")})
    public Mono<ResponseEntity<String>> reScheduleTasks() {
        LOGGER.trace("bbs-event-processor has received a request to reschedule all running tasks");
        if (scheduler.reScheduleProcessingTasks()) {
            return Mono.defer(() ->
                    Mono.just(new ResponseEntity<>("Initiation of tasks was successful\n", HttpStatus.OK))
            );
        } else {
            return Mono.defer(() ->
                    Mono.just(new ResponseEntity<>("Initiation of tasks failed\n", HttpStatus.NOT_ACCEPTABLE))
            );
        }
    }

    /**
     * Cancels DMaaP polling tasks.
     * @return Proper HTTP response based on cancellation result
     */
    @PostMapping("cancel-tasks")
    @ApiOperation(value = "Returns result of request to cancel running microservice tasks")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Tasks were successfully cancelled"),
            @ApiResponse(code = 401, message = "Not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Resource access is forbidden"),
            @ApiResponse(code = 404, message = "Resource is not found"),
            @ApiResponse(code = 406, message = "Cancellation failed. Check logs")})
    public Mono<ResponseEntity<String>> cancelTasks() {
        LOGGER.debug("bbs-event-processor has received a request to cancel all running tasks");
        if (scheduler.cancelScheduledProcessingTasks()) {
            return Mono.defer(() ->
                    Mono.just(new ResponseEntity<>("Cancellation was successful\n", HttpStatus.OK))
            );
        } else {
            return Mono.defer(() ->
                    Mono.just(new ResponseEntity<>("Cancellation failed\n", HttpStatus.NOT_ACCEPTABLE))
            );
        }
    }

    /**
     * Change logging level for BBS code.
     * @param level new logging level
     * @return Proper HTTP response based on change logging level result
     */
    @PostMapping("logging/{level}")
    @ApiOperation(value = "Returns result of request to change application logging level")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Application logging level was successfully changed"),
            @ApiResponse(code = 401, message = "Not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Resource access is forbidden"),
            @ApiResponse(code = 404, message = "Resource is not found"),
            @ApiResponse(code = 406, message = "Application logging level change failure. Check logs")})
    public Mono<ResponseEntity<String>> changeLoggingLevel(@PathVariable String level) {
        return Mono.defer(() ->  {
                if (LoggingUtil.changeLoggingLevel(level)) {
                    LOGGER.info("Changed logging level to {}", level);
                    return Mono.just(new ResponseEntity<>("Changed BBS event processor logging level\n",
                            HttpStatus.OK));
                } else {
                    return Mono.just(new ResponseEntity<>("Unacceptable logging level\n",
                            HttpStatus.NOT_ACCEPTABLE));
                }
            }
        );
    }
}
