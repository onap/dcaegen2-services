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
import org.onap.datalake.feeder.dto.DesignTypeConfig;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

/**
 * Domain class representing design_type
 *
 * @author guochunmeng
 */
@Getter
@Setter
@Entity
@Table(name = "design_type")
public class DesignType {

    @Id
    @Column(name = "`id`")
    private String id;
    
    @Column(name = "`name`")
    private String name;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="db_type_id", nullable = false)
    @JsonBackReference
    private DbType dbType;    

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "designType")
	protected Set<Design> designs = new HashSet<>();

    @Column(name = "`note`")
    private String note;

    public DesignTypeConfig getDesignTypeConfig() {

        DesignTypeConfig designTypeConfig = new DesignTypeConfig();

        designTypeConfig.setId(getId());
        designTypeConfig.setName(getName());

        return designTypeConfig;
    }

}
