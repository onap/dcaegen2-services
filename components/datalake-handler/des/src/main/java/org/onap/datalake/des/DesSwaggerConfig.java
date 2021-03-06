/*
* ============LICENSE_START=======================================================
* ONAP : Data Extraction Service
* ================================================================================
* Copyright 2020 China Mobile
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

package org.onap.datalake.des;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * For Swagger integration.
 * 
 * @author Kai Lu
 *
 */

@Configuration
@EnableSwagger2
public class DesSwaggerConfig {

    /**
     * des produceApi.
     *
     * @return Docket Docket
     *
     */
    @Bean
    public Docket desProduceApi() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(desApiInfo()).select()
                .apis(RequestHandlerSelectors.basePackage("org.onap.datalake.des")).paths(PathSelectors.any()).build();
    }

    /**
     * des Api description.
     *
     * @return ApiInfo des api Info
     *
     */
    private ApiInfo desApiInfo() {
        return new ApiInfoBuilder().title("DataLake Rest APIs")
                .description("This page lists all the rest apis for DataLake.").version("1.0.0-SNAPSHOT").build();
    }
}
