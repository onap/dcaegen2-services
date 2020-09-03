/*
* ============LICENSE_START=======================================================
* ONAP : Data Extraction Service
* ================================================================================
* Copyright 2020 China Mobile
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

package org.onap.datalake.des.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.FetchType;

import org.onap.datalake.des.dto.DataExposureConfig;

/**
 * Domain class representing DataExposure.
 *
 * @author Kai Lu
 */
@Getter
@Setter
@Entity
@Table(name = "data_exposure")
public class DataExposure {

    @Id
    @Column(name = "`id`")
    private String id;
    @Column(name = "`sql_template`", nullable = false)
    private String sqlTemplate;
    @Column(name = "`note`")
    private String note;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "db_id", nullable = false)
    @JsonBackReference
    private Db db;

    public DataExposure() {
    }

    public DataExposure(String id, String sqlTemplate) {
        this.id = id;
        this.sqlTemplate = sqlTemplate;
    }

    /**
     * getDataExposureConfig.
     *
     * @return data exposure config
     *
     */
    public DataExposureConfig getDataExposureConfig() {
        DataExposureConfig dataExposureConfig = new DataExposureConfig();
        dataExposureConfig.setId(getId());
        dataExposureConfig.setSqlTemplate(getSqlTemplate());
        dataExposureConfig.setNote(getNote());
        dataExposureConfig.setDbId(getDb().getId());
        return dataExposureConfig;
    }
}
