/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 China Mobile.
 *  Copyright (C) 2022 Wipro Limited.
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

package org.onap.dcaegen2.kpi.utils;

import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import com.att.nsa.cambria.client.CambriaClientBuilders;
import com.att.nsa.cambria.client.CambriaClientBuilders.ConsumerBuilder;
import com.att.nsa.cambria.client.CambriaClientBuilders.PublisherBuilder;
import com.att.nsa.cambria.client.CambriaConsumer;

import java.net.MalformedURLException;
import java.security.GeneralSecurityException;

import org.onap.dcaegen2.kpi.models.Configuration;

/**
 * Utility class to perform actions related to Dmaap.
 *
 * @author Kai Lu
 *
 */
public class DmaapUtils {

    /**
     * Build publisher.
     */
    public CambriaBatchingPublisher buildPublisher(Configuration config, String topic) {
        try {
            return builder(config, topic).build();
        } catch (MalformedURLException | GeneralSecurityException e) {
            return null;

        }
    }

    /**
     * Build consumer.
     */
    public CambriaConsumer buildConsumer(Configuration config, String topic) {

        try {
            return builderConsumer(config, topic).build();
        } catch (MalformedURLException | GeneralSecurityException e) {
            return null;
        }

    }

    private static PublisherBuilder builder(Configuration config, String topic) {
        if (config.isSecured()) {
            return authenticatedBuilder(config, topic);
        } else {
            return unAuthenticatedBuilder(config, topic);
        }
    }

    private static PublisherBuilder authenticatedBuilder(Configuration config, String topic) {
        return unAuthenticatedBuilder(config, topic).usingHttps().authenticatedByHttp(config.getAafUsername(),
                config.getAafPassword());
    }

    private static PublisherBuilder unAuthenticatedBuilder(Configuration config, String topic) {
        return new CambriaClientBuilders.PublisherBuilder().usingHosts(config.getDmaapServers()).onTopic(topic)
                .logSendFailuresAfter(5);
    }

    private static ConsumerBuilder builderConsumer(Configuration config, String topic) {
        if (config.isSecured()) {
            return authenticatedConsumerBuilder(config, topic);
        } else {
            return unAuthenticatedConsumerBuilder(config, topic);
        }
    }

    private static ConsumerBuilder unAuthenticatedConsumerBuilder(Configuration config, String topic) {
        return new CambriaClientBuilders.ConsumerBuilder().usingHosts(config.getDmaapServers()).onTopic(topic)
                .knownAs(config.getCg(), config.getCid()).withSocketTimeout(config.getPollingTimeout() * 1000);
    }

    private static ConsumerBuilder authenticatedConsumerBuilder(Configuration config, String topic) {
        return unAuthenticatedConsumerBuilder(config, topic).usingHttps().authenticatedByHttp(config.getAafUsername(),
                config.getAafPassword());
    }

}
