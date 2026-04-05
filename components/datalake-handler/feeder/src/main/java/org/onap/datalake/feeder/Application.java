/*
* ============LICENSE_START=======================================================
* ONAP : DataLake
* ================================================================================
* Copyright 2019 China Mobile
* Copyright (C) 2021 Wipro Limited.
* Copyright 2026 Deutsche Telekom AG. All rights reserved.
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

package org.onap.datalake.feeder;

import javax.sql.DataSource;

import org.onap.datalake.feeder.service.PullService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;

import springfox.documentation.oas.annotations.EnableOpenApi;

/**
 * Entry point of the DataLake feeder application
 *
 * @author Guobiao Mo
 *
 */

@SpringBootApplication
@EnableOpenApi
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(PullService pullService) {
        return args -> pullService.start();
    }

    @Bean
    public DataSource dataSource() {

        String url = "jdbc:postgresql://" + System.getenv("PG_HOST").trim() + ":" + System.getenv("PG_PORT").trim()
                + "/datalake";
        return DataSourceBuilder.create().url(url).username(System.getenv("PG_USER").trim())
                .password(System.getenv("PG_PASSWORD").trim()).build();
    }
}
