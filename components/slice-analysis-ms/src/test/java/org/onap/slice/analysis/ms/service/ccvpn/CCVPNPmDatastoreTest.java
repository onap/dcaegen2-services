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

package org.onap.slice.analysis.ms.service.ccvpn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = CCVPNPmDatastoreTest.class)
public class CCVPNPmDatastoreTest {

    @Spy
    @InjectMocks
    CCVPNPmDatastore datastore;

    @Test
    public void getUsedBwOfSvcTest() {
        datastore.addUsedBwToEndpoint("cll-test", "uni-01", "100");
        datastore.addUsedBwToEndpoint("cll-test", "uni-01", "100");
        datastore.addUsedBwToEndpoint("cll-test", "uni-02", "100");
        datastore.addUsedBwToEndpoint("cll-test2", "uni-01", "100");
        assertEquals(datastore.getUsedBwOfSvc("cll-test").get(new Endpointkey("cll-test", "uni-01")).size(),
        2);
    }

    @Test
    public void getMaxBwOfSvcTest() {
        datastore.updateMaxBw("cll-test", 100, false);
        assertEquals(datastore.getMaxBwOfSvc("cll-test"), Integer.valueOf(100));
    }

    @Test
    public void getStatusOfSvcTest() {
        datastore.updateSvcState("cll-01", ServiceState.RUNNING);
        assertEquals(datastore.getStatusOfSvc("cll-01"), ServiceState.RUNNING);
    }

    @Test
    public void getSvcStatusMapTest() {
        datastore.updateSvcState("cll-01", ServiceState.RUNNING);
        datastore.getSvcStatusMap();
        Mockito.verify(datastore, Mockito.atLeastOnce()).getSvcStatusMap();
    }

    @Test
    public void getUsedBwMapTest() {
        datastore.updateSvcState("cll-01", ServiceState.RUNNING);
        datastore.getUsedBwMap();
        Mockito.verify(datastore, Mockito.atLeastOnce()).getUsedBwMap();
    }

    @Test
    public void updateSvcStateTest() {
        datastore.updateSvcState("cll-01", ServiceState.RUNNING);
        assertEquals(datastore.getStatusOfSvc("cll-01"), ServiceState.RUNNING);
    }

    @Test
    public void readToArrayTest() {
        for(int i = 0; i < 5; i++){
            datastore.addUsedBwToEndpoint("cll-01", "uni-n1", "300");
        }
        assertTrue(Arrays.stream(datastore.readToArray("cll-01", "uni-n1"))
                .mapToInt(o -> (int)o)
                .allMatch(n -> n == 1));
    }

    @Test
    public void updateMaxBwTest() throws NoSuchFieldException, IllegalAccessException {
        datastore.updateMaxBw("cll-01", "300");
        Mockito.verify(datastore, Mockito.atLeastOnce()).updateMaxBw(Mockito.any(String.class), Mockito.any(String.class));
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
