/*
 * ============LICENSE_START=======================================================
 * ONAP : DATALAKE
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

package org.onap.datalake.feeder.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * test ApplicationConfiguration
 *
 * @author Guobiao Mo
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest
/*
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class, 
                      initializers = ConfigFileApplicationContextInitializer.class)
*/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApplicationConfiguration.class)
//@ActiveProfiles("test")
public class ApplicationConfigurationTest {

    @Autowired
    private ApplicationConfiguration config;

    @Test
    public void readConfig() {
        
        assertNotNull(config.isAsync());
        assertNotNull(config.isEnableSSL());
        assertNotNull(config.getDefaultTopicName());
        assertNotNull(config.getRawDataLabel());
        assertNotNull(config.getTimestampLabel());
        assertNotNull(config.getElasticsearchType());
        assertNotNull(config.getDatalakeVersion());
        
      //HDFS
        assertTrue(config.getHdfsBatchSize()>0);
        assertTrue(config.getHdfsBufferSize()>0);
        assertTrue(config.getHdfsFlushInterval()>0);

        assertNull(config.getKibanaDashboardImportApi());
        assertNull(config.getKibanaPort());
        assertNull(config.getEsTemplateMappingApi());
        assertNull(config.getEsPort());
        assertTrue(config.getCheckTopicInterval()==0);
    }

}
