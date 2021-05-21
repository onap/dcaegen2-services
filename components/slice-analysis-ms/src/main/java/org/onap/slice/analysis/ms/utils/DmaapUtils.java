/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2020-2021 Wipro Limited.
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

import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import com.att.nsa.cambria.client.CambriaClient;
import com.att.nsa.cambria.client.CambriaClientBuilders;
import com.att.nsa.cambria.client.CambriaClientBuilders.ConsumerBuilder;
import com.att.nsa.cambria.client.CambriaClientBuilders.PublisherBuilder;
import com.att.nsa.cambria.client.CambriaClientBuilders.TopicManagerBuilder;
import com.att.nsa.cambria.client.CambriaConsumer;
import com.att.nsa.cambria.client.CambriaTopicManager;

import java.net.MalformedURLException;
import java.security.GeneralSecurityException;

import org.onap.slice.analysis.ms.models.Configuration;

/**
 * Utility class to perform actions related to Dmaap
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

    /**
     * Build cambriaClient.
     */
    public CambriaTopicManager cambriaCLientBuilder(Configuration configuration) {
        if (configuration.isSecured()) {
            return authenticatedCambriaCLientBuilder(configuration);
        } else {
            return unAuthenticatedCambriaCLientBuilder(configuration);

        }
    }

    private static CambriaTopicManager authenticatedCambriaCLientBuilder(Configuration config) {
        try {
            return buildCambriaClient(new TopicManagerBuilder().usingHosts(config.getDmaapServers())
                    .authenticatedByHttp(config.getAafUsername(), config.getAafPassword()));
        } catch (MalformedURLException | GeneralSecurityException e) {
            return null;
        }
    }

    private static CambriaTopicManager unAuthenticatedCambriaCLientBuilder(Configuration config) {
        try {
            return buildCambriaClient(new TopicManagerBuilder().usingHosts(config.getDmaapServers()));
        } catch (MalformedURLException | GeneralSecurityException e) {
            return null;

        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends CambriaClient> T buildCambriaClient(
            CambriaClientBuilders.AbstractAuthenticatedManagerBuilder<? extends CambriaClient> client)
            throws MalformedURLException, GeneralSecurityException {
        return (T) client.build();
    }

}