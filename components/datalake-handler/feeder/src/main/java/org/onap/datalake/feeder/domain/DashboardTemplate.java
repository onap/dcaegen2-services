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

package org.onap.datalake.feeder.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Domain class representing dashboard_template
 *
 * @author guochunmeng
 */

@Getter
@Setter
@Entity
@Table(name = "dashboard_template")
public class DashboardTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private Integer id;

    @Column(name = "`name`")
    private String name;

    @Column(name = "`body`")
    private String body;

    @Column(name = "`note`")
    private String note;

    @Column(name = "`topic`")
    private String topic;

    @Column(name = "`type`")
    private String type;

}
