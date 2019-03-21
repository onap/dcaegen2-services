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

import java.util.concurrent.Executors;

import org.onap.bbs.event.processor.pipelines.CpeAuthenticationPipeline;
import org.onap.bbs.event.processor.pipelines.ReRegistrationPipeline;
import org.onap.bbs.event.processor.pipelines.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import reactor.core.publisher.Mono;

@RestController
@Api(value = "BbsEventProcessorController", description = "Manage bbs-event-processor microService")
public class BbsEventProcessorController {

    private ReRegistrationPipeline reRegistrationPipeline;
    private CpeAuthenticationPipeline cpeAuthenticationPipeline;
    private Scheduler scheduler;

    @Autowired
    public BbsEventProcessorController(ReRegistrationPipeline reRegistrationPipeline,
                                       CpeAuthenticationPipeline cpeAuthenticationPipeline,
                                       Scheduler scheduler) {
        this.reRegistrationPipeline = reRegistrationPipeline;
        this.cpeAuthenticationPipeline = cpeAuthenticationPipeline;
        this.scheduler = scheduler;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BbsEventProcessorController.class);

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
}
