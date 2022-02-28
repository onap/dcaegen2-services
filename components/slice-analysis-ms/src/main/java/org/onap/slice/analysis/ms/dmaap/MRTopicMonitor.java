/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2022 Huawei Canada.
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

package org.onap.slice.analysis.ms.dmaap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import org.onap.dmaap.mr.client.impl.MRConsumerImpl;
import org.onap.dmaap.mr.client.response.MRConsumerResponse;
import org.onap.dmaap.mr.test.clients.ProtocolTypeConstants;
import org.onap.slice.analysis.ms.models.Configuration;
import org.onap.slice.analysis.ms.dmaap.MRTopicParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This is a Dmaap message-router topic monitor.
 * It takes advantage of AT&T's dmaap-client's long-polling implementation, this monitor constantly fetch/refetch target msg topic.
 * So that new msg can be notified almost immediately. This is the major different from previous implementation.
 */
public class MRTopicMonitor implements Runnable {

    private final String name;
    private volatile boolean running = false;
    private Configuration configuration;
    private static Logger logger = LoggerFactory.getLogger(MRTopicMonitor.class);
    private static int DEFAULT_TIMEOUT_MS_FETCH = 15000;
    private MRConsumerWrapper consumerWrapper;
    private NotificationCallback callback;

    /**
     * Constructor
     * @param name name of topic subscriber in config
     * @param callback callbackfunction for received message
     */
    public MRTopicMonitor(String name, NotificationCallback callback){
        this.name = name;
        this.callback = callback;
    }

    /**
     * Start the monitoring thread
     */
    public void start(){
        logger.info("Starting Dmaap Bus Monitor");
        configuration = Configuration.getInstance();

        Map<String, Object> streamSubscribes = configuration.getStreamsSubscribes();
        Map<String, Object> topicParamsJson = (Map<String, Object>) streamSubscribes.get(name);
        JsonObject jsonObject = (new Gson()).toJsonTree(topicParamsJson).getAsJsonObject();
        consumerWrapper = buildConsumerWrapper(jsonObject);
        running = true;
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(this);
    }

    /**
     * Main loop that keep fetching and processing
     */
    @Override
    public void run(){
        while (running){
            try {
                Iterable<String> dmaapMsgs = consumerWrapper.fetch();
                for (String msg : dmaapMsgs){
                    process(msg);
                }
            } catch (IOException | RuntimeException e){
                logger.error("fetchMessage encountered error: {}", e);
            }
        }
        logger.info("{}: exiting thread", this);
    }

    /**
     * Stop the monitor
     */
    public void stop(){
        logger.info("{}: exiting", this);
        running = false;
        this.consumerWrapper.close();
        this.consumerWrapper = null;
    }

    private void process(String msg) {
        try {
            callback.activateCallBack(msg);
        } catch (Exception e){
            logger.error("process message encountered error: {}", e);
        }
    }

    private Iterable<String> fetch() throws IOException {
        return this.consumerWrapper.fetch();
    }

    private MRConsumerWrapper buildConsumerWrapper(@NonNull JsonObject topicParamsJson )
            throws IllegalArgumentException {
        MRTopicParams topicParams = MRTopicParams.builder().buildFromConfigJson(topicParamsJson).build();
        return new MRConsumerWrapper(topicParams);
    }

    /**
     * Wrapper class of DmaapClient (package org.onap.dmaap.mr.client)
     * A polling fashion dmaap  consumer, whose #fetch() sleep a certain time when connection fails, otherwise keep retryiny.
     * It supports both https and http protocols.
     */
    private class MRConsumerWrapper {
        /**
         * Name of the "protocol" property.
         */
        protected static final String PROTOCOL_PROP = "Protocol";
        /**
         * Fetch timeout.
         */
        protected int fetchTimeout;

        /**
         * Time to sleep on a fetch failure.
         */
        @Getter
        private final int sleepTime;

        /**
         * Counted down when {@link #close()} is invoked.
         */
        private final CountDownLatch closeCondition = new CountDownLatch(1);

        /**
         * MR Consumer.
         */
        protected MRConsumerImpl consumer;

        /**
         * Constructs the object.
         *
         * @param MRTopicParams parameters for the bus topic
         */
        protected MRConsumerWrapper(MRTopicParams MRTopicParams) {
            this.fetchTimeout = MRTopicParams.getFetchTimeout();

            if (this.fetchTimeout <= 0) {
                this.sleepTime = DEFAULT_TIMEOUT_MS_FETCH;
            } else {
                // don't sleep too long, even if fetch timeout is large
                this.sleepTime = Math.min(this.fetchTimeout, DEFAULT_TIMEOUT_MS_FETCH);
            }

            if (MRTopicParams.isTopicInvalid()) {
                throw new IllegalArgumentException("No topic for DMaaP");
            }

            if (MRTopicParams.isServersInvalid()) {
                throw new IllegalArgumentException("Must provide at least one host for HTTP AAF");
            }

            try{
                this.consumer = new MRConsumerImpl.MRConsumerImplBuilder()
                        .setHostPart(MRTopicParams.getServers())
                        .setTopic(MRTopicParams.getTopic())
                        .setConsumerGroup(MRTopicParams.getConsumerGroup())
                        .setConsumerId(MRTopicParams.getConsumerInstance())
                        .setTimeoutMs(MRTopicParams.getFetchTimeout())
                        .setLimit(MRTopicParams.getFetchLimit())
                        .setApiKey(MRTopicParams.getApiKey())
                        .setApiSecret(MRTopicParams.getApiSecret())
                        .createMRConsumerImpl();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Illegal MrConsumer parameters");
            }


            this.consumer.setUsername(MRTopicParams.getUserName());
            this.consumer.setPassword(MRTopicParams.getPassword());

            if(MRTopicParams.isUserNameValid() && MRTopicParams.isPasswordValid()){
                this.consumer.setProtocolFlag(ProtocolTypeConstants.AAF_AUTH.getValue());
            } else {
                this.consumer.setProtocolFlag(ProtocolTypeConstants.HTTPNOAUTH.getValue());
            }

            Properties props = new Properties();

            if (MRTopicParams.isUseHttps()) {
                props.setProperty(PROTOCOL_PROP, "https");
                this.consumer.setHost(MRTopicParams.getServers().get(0) + ":3905");

            } else {
                props.setProperty(PROTOCOL_PROP, "http");
                this.consumer.setHost(MRTopicParams.getServers().get(0) + ":3904");
            }

            this.consumer.setProps(props);
        }

        /**
         * Try fetch new message. But backoff for some sleepTime when connection fails.
         * @return
         * @throws IOException
         */
        public Iterable<String> fetch() throws IOException {
            final MRConsumerResponse response = this.consumer.fetchWithReturnConsumerResponse();
            if (response == null) {
                logger.warn("{}: DMaaP NULL response received", this);

                sleepAfterFetchFailure();
                return new ArrayList<>();
            } else {
                logger.debug("DMaaP consumer received {} : {}", response.getResponseCode(),
                        response.getResponseMessage());

                if (!"200".equals(response.getResponseCode())) {

                    logger.error("DMaaP consumer received: {} : {}", response.getResponseCode(),
                            response.getResponseMessage());

                    sleepAfterFetchFailure();
                }
            }

            if (response.getActualMessages() == null) {
                return new ArrayList<>();
            } else {
                return response.getActualMessages();
            }
        }

        /**
         * Causes the thread to sleep; invoked after fetch() fails.  If the consumer is closed,
         * or the thread is interrupted, then this will return immediately.
         */
        protected void sleepAfterFetchFailure() {
            try {
                logger.info("{}: backoff for {}ms", this, sleepTime);
                if (this.closeCondition.await(this.sleepTime, TimeUnit.MILLISECONDS)) {
                    logger.info("{}: closed while handling fetch error", this);
                }

            } catch (InterruptedException e) {
                logger.warn("{}: interrupted while handling fetch error", this, e);
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Close the dmaap client and this thread
         */
        public void close() {
            this.closeCondition.countDown();
            this.consumer.close();
        }
    }
}
