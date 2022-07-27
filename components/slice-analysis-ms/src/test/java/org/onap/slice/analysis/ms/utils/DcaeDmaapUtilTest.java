/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020-2021 CTC.
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

package org.onap.slice.analysis.ms.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.onap.slice.analysis.ms.utils.DcaeDmaapUtil.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DcaeDmaapUtilTest.class)
public class DcaeDmaapUtilTest {

    @Test
    public void buildSubscriberTest(){
        MessageRouterSubscriber subscriber = buildSubscriber();
        assertNotNull(subscriber);
    }

    @Test
    public void buildSubscriberRequestTest(){
        MessageRouterSubscribeRequest request = buildSubscriberRequest("name", "url");
        assertNotNull(request);
    }

    @Test
    public void buildPublisherTest(){
        MessageRouterPublisher publisher = buildPublisher();
        assertNotNull(publisher);
    }

    @Test
    public void buildPublisherRequestTest(){
        MessageRouterPublishRequest request = buildPublisherRequest("name", "url");
        assertNotNull(request);
    }

}
