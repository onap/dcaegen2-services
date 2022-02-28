/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2022 Huawei Canada Limited.
 *   ==============================================================================
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     ============LICENSE_END=========================================================
 *
 *******************************************************************************/

package org.onap.slice.analysis.ms.models.ccvpnnotification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = CCVPNPmDatastoreTest.class)
public class CCVPNPmDatastoreTest {

    @InjectMocks
    CCVPNPmDatastore datastore;

    @Test
    public void updateMaxBwTest() throws NoSuchFieldException, IllegalAccessException {
        datastore.updateMaxBw("cll-01", "300");
        Field field = datastore.getClass().getDeclaredField("endpointToMaxBw");
        field.setAccessible(true);
        ConcurrentMap<String, Integer> value = (ConcurrentMap<String, Integer>)field.get(datastore);
        assertEquals(1, value.size());
    }

    @Test
    public void addUsedBwToEndpointTest() {
        datastore.addUsedBwToEndpoint("cll-01", "uni-n1", "300Mb");
        datastore.addUsedBwToEndpoint("cll-01", "uni-n1", "300mb");
        datastore.addUsedBwToEndpoint("cll-01", "uni-n1", "300Gb");
        datastore.addUsedBwToEndpoint("cll-01", "uni-n1", "300kb");
        assertTrue(datastore.readToArray("cll-01", "uni-n1") == null);
        datastore.addUsedBwToEndpoint("cll-01", "uni-n1", "300.00");
        assertTrue(Arrays.stream(datastore.readToArray("cll-01", "uni-n1"))
                .mapToInt(o -> (int)o)
                .sum() == 300602 );
    }
}
