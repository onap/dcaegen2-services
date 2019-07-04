/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2019 China Mobile
 *=================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.datalake.feeder.controller;

import io.swagger.annotations.ApiOperation;
import org.onap.datalake.feeder.controller.domain.PostReturnBody;
import org.onap.datalake.feeder.domain.Kafka;
import org.onap.datalake.feeder.dto.KafkaConfig;
import org.onap.datalake.feeder.repository.KafkaRepository;
import org.onap.datalake.feeder.service.KafkaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * This controller manages kafka settings
 *
 * @author guochunmeng
 */
@RestController
@RequestMapping(value = "/kafkas", produces = { MediaType.APPLICATION_JSON_VALUE })
public class KafkaController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private KafkaRepository kafkaRepository;

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value="Create a kafka.")
    public PostReturnBody<KafkaConfig> createKafka(@RequestBody KafkaConfig kafkaConfig, BindingResult result, HttpServletResponse response) throws IOException {

        if (result.hasErrors()) {
            sendError(response, 400, "Error parsing KafkaConfig : "+result.toString());
            return null;
        }

        Kafka oldKafka = kafkaService.getKafkaById(kafkaConfig.getId());

        if (oldKafka != null) {
            sendError(response, 400, "kafka is exist "+kafkaConfig.getId());
            return null;
        } else {
            Kafka kafka = null;
            try {
                kafka = kafkaService.fillKafkaConfiguration(kafkaConfig);
            } catch (Exception e) {
                log.debug("FillKafkaConfiguration failed", e.getMessage());
                sendError(response, 400, "Error FillKafkaConfiguration: "+e.getMessage());
                return null;
            }
            kafkaRepository.save(kafka);
            log.info("Kafka save successed");
            return mkPostReturnBody(200, kafka);
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    @ApiOperation(value="Update a kafka.")
    public PostReturnBody<KafkaConfig> updateKafka(@RequestBody KafkaConfig kafkaConfig, BindingResult result, @PathVariable String id, HttpServletResponse response) throws IOException {

        if (result.hasErrors()) {
            sendError(response, 400, "Error parsing KafkaConfig : "+result.toString());
            return null;
        }

        Kafka oldKafka = kafkaService.getKafkaById(id);

        if (oldKafka == null) {
            sendError(response, 400, "Kafka not found: "+id);
            return null;
        } else {
            try {
                kafkaService.fillKafkaConfiguration(kafkaConfig, oldKafka);
            } catch (Exception e) {
                log.debug("FillKafkaConfiguration failed", e.getMessage());
                sendError(response, 400, "Error FillKafkaConfiguration: "+e.getMessage());
                return null;
            }
            kafkaRepository.save(oldKafka);
            log.info("kafka update successed");
            return mkPostReturnBody(200, oldKafka);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @ApiOperation(value="delete a kafka.")
    public void deleteKafka(@PathVariable("id") String id, HttpServletResponse response) throws IOException{

        Kafka oldKafka = kafkaService.getKafkaById(id);
        if (oldKafka == null) {
            sendError(response, 400, "kafka not found "+id);
        } else {
            kafkaRepository.delete(oldKafka);
            response.setStatus(204);
        }
    }

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value="List all Kafkas")
    public List<KafkaConfig> queryAllKafka(){
        return kafkaService.getAllKafka();
    }

    private PostReturnBody<KafkaConfig> mkPostReturnBody(int statusCode, Kafka kafka) {
        PostReturnBody<KafkaConfig> retBody = new PostReturnBody<>();
        retBody.setStatusCode(statusCode);
        retBody.setReturnBody(kafka.getKafkaConfig());
        return retBody;
    }

    private void sendError(HttpServletResponse response, int sc, String msg) throws IOException {
        log.info(msg);
        response.sendError(sc, msg);
    }

}
