/*
* ============LICENSE_START=======================================================
* ONAP : DATALAKE
* ================================================================================
* Copyright 2019 China Mobile
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

package org.onap.datalake.feeder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Predicate;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.oas.annotations.EnableOpenApi;

/**
 * For Swagger integration
 * 
 * @author Guobiao Mo
 *
 */

@Configuration
@EnableOpenApi
public class SwaggerConfig {
	@Bean
	public Docket produceApi() {
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select().apis(RequestHandlerSelectors.basePackage("org.onap.datalake.feeder")).paths(paths()).build();
	}

	// Describe your apis
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("DataLake Rest APIs").description("This page lists all the rest apis for DataLake.").version("1.0.0-SNAPSHOT").build();
	}

	// Only select apis that matches the given Predicates.
	private Predicate<String> paths() {
		// Match all paths except /error
		return PathSelectors.regex("/dbs.*")
				.or(PathSelectors.regex("/topics.*"))
				.or(PathSelectors.regex("/feeder.*"))
				.or(PathSelectors.regex("/kafkas.*"));
	}
}
