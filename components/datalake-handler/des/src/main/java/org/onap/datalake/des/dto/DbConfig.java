/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
 * Copyright 2020 China Mobile
 * Copyright 2019 QCT
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
package org.onap.datalake.des.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * JSON request body for DB manipulation.
 *
 * @author Kate Hsuan
 *
 */

@Getter
@Setter
public class DbConfig {
    private int id;
    private String name;
    private String host;
    private boolean enabled;
    private String login;
    private String pass;
    private boolean encrypt;
    private String database;
    private Integer port;
    private String poperties;
    private String dbTypeId;
}
