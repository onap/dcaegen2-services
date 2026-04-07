/*
* ============LICENSE_START=======================================================
* ONAP : DataLake
* ================================================================================
* Copyright 2026 Deutsche Telekom AG. All rights reserved.
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

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "datalake.pg.enabled", havingValue = "true", matchIfMissing = true)
public class DataSourceConfig {

    @Value("${datalake.pg.host}")
    private String pgHost;

    @Value("${datalake.pg.port}")
    private String pgPort;

    @Value("${datalake.pg.user}")
    private String pgUser;

    @Value("${datalake.pg.password}")
    private String pgPassword;

    @Bean
    public DataSource dataSource() {
        String url = "jdbc:postgresql://" + pgHost.trim() + ":" + pgPort.trim() + "/datalake";
        return DataSourceBuilder.create().url(url).username(pgUser.trim()).password(pgPassword.trim()).build();
    }
}
