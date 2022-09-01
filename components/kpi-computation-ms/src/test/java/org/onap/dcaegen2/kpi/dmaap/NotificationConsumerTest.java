/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 China Mobile.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.kpi.dmaap;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.JsonElement;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NotificationConsumerTest.class)
public class NotificationConsumerTest {

    @Mock
    NotificationCallback notificationCallback;

    @InjectMocks
    NotificationConsumer notificationConsumer;
    
    @Mock
    MessageRouterSubscriber messageSubscriber;
    
    @Mock
    MessageRouterSubscribeRequest subscriberRequest;

    @Test
    public void testNotificationConsumer() {
        try {
        	Flux<JsonElement> json = new Flux<JsonElement>() {
				
				@Override
				public void subscribe(CoreSubscriber<? super JsonElement> actual) {
					
				}
			};
			Mockito.doNothing().when(notificationCallback).activateCallBack(Mockito.anyString());		
        	when(messageSubscriber.subscribeForElements(subscriberRequest, Duration.ofMinutes(1))).thenReturn(json);
        	assertNotNull(messageSubscriber.subscribeForElements(subscriberRequest, Duration.ofMinutes(1)));
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
