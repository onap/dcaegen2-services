/*
 * ============LICENSE_START=======================================================
 * BBS-RELOCATION-CPE-AUTHENTICATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.bbs.event.processor.utilities;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLException;

import org.onap.bbs.event.processor.config.AaiClientConfiguration;
import org.onap.bbs.event.processor.config.ApplicationConfiguration;
import org.onap.bbs.event.processor.config.ConfigurationChangeObserver;
import org.onap.bbs.event.processor.exceptions.AaiTaskException;
import org.onap.bbs.event.processor.model.PnfAaiObject;
import org.onap.bbs.event.processor.model.ServiceInstanceAaiObject;
import org.onap.dcaegen2.services.sdk.rest.services.ssl.SslFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Component
public class AaiReactiveClient implements ConfigurationChangeObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(AaiReactiveClient.class);

    private final Gson gson;
    private WebClient webClient;
    private SslFactory sslFactory;
    private final ApplicationConfiguration configuration;
    private AaiClientConfiguration aaiClientConfiguration;

    @Autowired
    public AaiReactiveClient(ApplicationConfiguration configuration, Gson gson) throws SSLException {
        this.configuration = configuration;
        this.gson = gson;
        this.sslFactory = new SslFactory();

        aaiClientConfiguration = configuration.getAaiClientConfiguration();
        setupWebClient();
    }

    @PostConstruct
    public void registerForConfigChanges() {
        configuration.register(this);
    }

    @PreDestroy
    public void unRegisterForConfigChanges() {
        configuration.unRegister(this);
    }

    @Override
    public void updateConfiguration(ApplicationConfiguration configuration) {
        AaiClientConfiguration newConfiguration = configuration.getAaiClientConfiguration();
        if (aaiClientConfiguration.equals(newConfiguration)) {
            LOGGER.debug("No Configuration changes necessary for AAI Reactive client");
        } else {
            LOGGER.debug("AAI Reactive client must be re-configured");
            aaiClientConfiguration = newConfiguration;
            try {
                setupWebClient();
            } catch (SSLException e) {
                LOGGER.error("AAI Reactive client error while re-configuring WebClient");
            }
        }
    }

    private synchronized void setupWebClient() throws SSLException {
        SslContext sslContext = createSslContext();

        ClientHttpConnector reactorClientHttpConnector = new ReactorClientHttpConnector(
                HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext)));

        this.webClient = WebClient.builder()
                .baseUrl(aaiClientConfiguration.aaiProtocol() + "://" + aaiClientConfiguration.aaiHost()
                        + ":" + aaiClientConfiguration.aaiPort())
                .clientConnector(reactorClientHttpConnector)
                .defaultHeaders(httpHeaders -> httpHeaders.setAll(aaiClientConfiguration.aaiHeaders()))
                .filter(basicAuthentication(aaiClientConfiguration.aaiUserName(),
                        aaiClientConfiguration.aaiUserPassword()))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    public Mono<PnfAaiObject> getPnfObjectDataFor(String url) {

        return performReactiveHttpGet(url, PnfAaiObject.class);
    }

    public Mono<ServiceInstanceAaiObject> getServiceInstanceObjectDataFor(String url) {

        return performReactiveHttpGet(url, ServiceInstanceAaiObject.class);
    }

    private synchronized <T> Mono<T> performReactiveHttpGet(String url, Class<T> responseType) {
        LOGGER.debug("Will issue Reactive GET request to URL ({}) for object ({})", url, responseType.getName());
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        response -> Mono.error(createExceptionObject(url, response)))
                .onStatus(HttpStatus::is5xxServerError,
                        response -> Mono.error(createExceptionObject(url, response)))
                .bodyToMono(String.class)
                .flatMap(body -> extractMono(body, responseType));
    }

    private AaiTaskException createExceptionObject(String url, ClientResponse response) {
        return new AaiTaskException(String.format("A&AI Request for (%s) failed with HTTP status code %d", url,
                response.statusCode().value()));
    }

    private <T> Mono<T> extractMono(String body, Class<T> responseType) {
        LOGGER.debug("Response body \n{}", body);
        try {
            return Mono.just(parseFromJsonReply(body, responseType));
        } catch (JsonSyntaxException | IllegalStateException e) {
            return Mono.error(e);
        }
    }

    private <T> T parseFromJsonReply(String body, Class<T> responseType) {
        return gson.fromJson(body, responseType);
    }

    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            LOGGER.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> LOGGER.debug("{}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            LOGGER.debug("Response status {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    private SslContext createSslContext() throws SSLException {
        if (aaiClientConfiguration.enableAaiCertAuth()) {
            return sslFactory.createSecureContext(
                    aaiClientConfiguration.keyStorePath(),
                    aaiClientConfiguration.keyStorePasswordPath(),
                    aaiClientConfiguration.trustStorePath(),
                    aaiClientConfiguration.trustStorePasswordPath()
            );
        }
        return sslFactory.createInsecureContext();
    }
}
