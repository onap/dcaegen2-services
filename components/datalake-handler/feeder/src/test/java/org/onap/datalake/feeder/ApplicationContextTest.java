/*-
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.onap.datalake.feeder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import org.onap.datalake.feeder.service.PullService;

/**
 * Verifies the Spring application context starts and the embedded web server
 * initializes correctly. Catches classpath conflicts and Swagger/OpenAPI
 * initialisation errors (e.g. Springfox NPE with Spring Boot Actuator).
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "datalake.pg.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.autoconfigure.data.couchbase.CouchbaseDataAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.couchbase.CouchbaseReactiveDataAutoConfiguration," +
            "org.springframework.boot.autoconfigure.couchbase.CouchbaseAutoConfiguration"
    }
)
public class ApplicationContextTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private PullService pullService;

    @Test
    public void contextLoads() {
        // verifies that the Spring context starts up without errors
    }

    @Test
    public void openApiDocumentationEndpointIsReachable() {
        // verifies that springdoc initialises correctly with Spring Boot Actuator
        // on the classpath — this would previously NPE with Springfox 3.0.0
        ResponseEntity<String> response = restTemplate
                .getForEntity("http://localhost:" + port + "/datalake/v1/v3/api-docs", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
