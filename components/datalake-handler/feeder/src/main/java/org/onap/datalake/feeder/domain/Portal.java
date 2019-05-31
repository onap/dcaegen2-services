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

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import org.onap.datalake.feeder.dto.PortalConfig;

import javax.persistence.*;

/**
 * Domain class representing portal
 *
 * @author guochunmeng
 */

@Getter
@Setter
@Entity
@Table(name = "portal")
public class Portal {

    @Id
    @Column(name = "`name`")
    private String name;

    @Column(name = "`enabled`")
    private Boolean enabled;

    @Column(name = "`host`")
    private String host;

    @Column(name = "`port`")
    private Integer port;

    @Column(name = "`login`")
    private String login;

    @Column(name = "`pass`")
    private String pass;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "related_db")
    @JsonBackReference
    private Db db;

    public PortalConfig getPortalConfig() {
        PortalConfig portalConfig = new PortalConfig();

        portalConfig.setName(getName());
        portalConfig.setLogin(getLogin());
        portalConfig.setPass(getPass());
        portalConfig.setEnabled(getEnabled());
        portalConfig.setHost(getHost());
        portalConfig.setPort(getPort());
        portalConfig.setDb(getDb().getName());

        return portalConfig;
    }
}
