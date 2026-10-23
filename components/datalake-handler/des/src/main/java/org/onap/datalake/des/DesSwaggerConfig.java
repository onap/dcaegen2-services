/*
* ============LICENSE_START=======================================================
* ONAP : Data Extraction Service
* ================================================================================
* Copyright 2020 China Mobile
* Modifications Copyright (C) 2026 Deutsche Telekom AG
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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * For Swagger integration.
 * 
 * @author Kai Lu
 *
 */

@Configuration
public class DesSwaggerConfig {

    /**
     * des OpenAPI description.
     *
     * @return OpenAPI des api info
     *
     */
    @Bean
    public OpenAPI desOpenAPI() {
        return new OpenAPI().info(new Info().title("DataLake Rest APIs")
                .description("This page lists all the rest apis for DataLake.").version("1.0.0-SNAPSHOT"));
    }
}
