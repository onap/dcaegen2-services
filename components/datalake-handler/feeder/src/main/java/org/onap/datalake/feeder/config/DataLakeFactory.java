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

import org.onap.datalake.feeder.util.BeanUtil;
import org.springframework.context.annotation.Description;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 *
 * DataLakeFactory.
 * 
 * @author Kai Lu
 *
 */
@Description(value = "Datalake Factory.")
@Component
public class DataLakeFactory {

    /**
     * Private constructor - can not be instantiated
     */
    private DataLakeFactory() {
    }

    /**
     * getDataLake
     *
     * @param catalog - db catalog
     * @return jdbcTemplate JdbcTemplate
     */
    public JdbcTemplate getDataLake(String catalog) {
        switch (catalog) {
            case ConfigConstant.DATALAKE_CATALOG_MONGODB:
                return (JdbcTemplate) BeanUtil.getBean(ConfigConstant.DATALAKE_PRESTO_JDBCTEMPLATE);

            case ConfigConstant.DATALAKE_CATALOG_MYSQL:
                return new JdbcTemplate();

            default:
                throw new IllegalArgumentException("Provided code not found.");
        }
    }

}