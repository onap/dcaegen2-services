/*******************************************************************************
 *  ============LICENSE_START=======================================================
 *  slice-analysis-ms
 *  ================================================================================
 *   Copyright (C) 2021 Wipro Limited.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {


    
    /**
     * Rest Template bean.
     */
    @Bean
    public static RestTemplate initRestTemplateForPdfAsByteArrayAndSelfSignedHttps() {
        RestTemplate restTemplate = new RestTemplate(useApacheHttpClientWithSelfSignedSupport());
        restTemplate.getMessageConverters().add(generateByteArrayHttpMessageConverter());
        return restTemplate;
    }

    private static HttpComponentsClientHttpRequestFactory useApacheHttpClientWithSelfSignedSupport() {
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        HttpComponentsClientHttpRequestFactory useApacheHttpClient = new HttpComponentsClientHttpRequestFactory();
        useApacheHttpClient.setHttpClient(httpClient);
        return useApacheHttpClient;
    }

    private static ByteArrayHttpMessageConverter generateByteArrayHttpMessageConverter() {
        ByteArrayHttpMessageConverter byteArrayHttpMessageConverter = new ByteArrayHttpMessageConverter();

        List<MediaType> supportedApplicationTypes = new ArrayList<>();
        supportedApplicationTypes.add(new MediaType("application", "pdf"));
        byteArrayHttpMessageConverter.setSupportedMediaTypes(supportedApplicationTypes);
        return byteArrayHttpMessageConverter;
    }
}
