/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
 * ================================================================================
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

package org.onap.datalake.feeder.dto;

import lombok.Getter;
import lombok.Setter;
import org.onap.datalake.feeder.domain.Db;

/**
 * JSON request body for Portal Config.
 *
 * @author guochunmeng
 *
 */
@Setter
@Getter
public class PortalConfig {

    private String name;

    private Boolean enabled;

    private String host;

    private Integer port;

    private String login;

    private String pass;

    private Db db;

}
