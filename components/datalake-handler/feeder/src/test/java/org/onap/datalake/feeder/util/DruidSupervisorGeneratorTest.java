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
package org.onap.datalake.feeder.util;

import org.apache.velocity.VelocityContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.datalake.feeder.config.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Test DruidSupervisorGenerator
 *
 * @author Guobiao Mo
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApplicationConfiguration.class)

public class DruidSupervisorGeneratorTest {

    @Autowired
    private ApplicationConfiguration config;

    @Test
    public void testConstructor() throws IOException {
        DruidSupervisorGenerator gen = new DruidSupervisorGenerator();
        VelocityContext context = gen.getContext();

        assertNotNull(context);
        assertNotNull(gen.getDimensions());
        assertNotNull(gen.getTemplate());

        String host = (String) context.get("host");
        assertEquals(host, config.getDmaapKafkaHostPort());

        String[] strArray2 = {"test1", "test2", "test3"};

        DruidSupervisorGenerator.main(strArray2);
    }
}
