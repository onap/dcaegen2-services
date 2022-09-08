/*
 * ============LICENSE_START=======================================================
 * ONAP : DataLake
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

package org.onap.datalake.feeder.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * JSON request body for portalDesign Config.
 *
 * @author guochunmeng
 */

@Getter
@Setter
public class DesignConfig {

    private Integer id;
    private String name;
    private Boolean submitted;
    private String body;
    private String note;
    private String topicName;
    private String designType;
    private String designTypeName;//UI show name
    private List<Integer> dbs;

}
