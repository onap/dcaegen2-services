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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import org.onap.datalake.feeder.dto.DesignConfig;

/**
 * Domain class representing design
 *
 * @author guochunmeng
 */

@Getter
@Setter
@Entity
@Table(name = "design")
public class Design {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private Integer id;

    @Column(name = "`name`")
    private String name;

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_name_id", nullable = false)
	private TopicName topicName;//topic name 
	
    @Column(name = "`submitted`")
    private Boolean submitted;

    @Column(name = "`body`")
    private String body;

    @Column(name = "`note`")
    private String note;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "design_type_id", nullable = false)
    @JsonBackReference
    private DesignType designType;

	//@ManyToMany(mappedBy = "topics", cascade=CascadeType.ALL)
	@JsonBackReference
	//@JsonManagedReference
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "map_db_design", joinColumns = { @JoinColumn(name = "design_id") }, inverseJoinColumns = { @JoinColumn(name = "db_id") })
	protected Set<Db> dbs;

    public DesignConfig getDesignConfig() {
    	
    	DesignConfig designConfig = new DesignConfig();

        designConfig.setId(getId());
		designConfig.setBody(getBody());
		designConfig.setName(getName());
		designConfig.setNote(getNote());
		designConfig.setSubmitted(getSubmitted());
		designConfig.setTopicName(getTopicName().getId());
        designConfig.setDesignType(getDesignType().getId());
        designConfig.setDesignTypeName(getDesignType().getName());

        Set<Db> designDb = getDbs();
        List<Integer> dbList = new ArrayList<>();
        if (designDb != null) {
            for (Db item : designDb) {
                    dbList.add(item.getId());
            }
        }
        designConfig.setDbs(dbList);

		return designConfig;
    }
}
