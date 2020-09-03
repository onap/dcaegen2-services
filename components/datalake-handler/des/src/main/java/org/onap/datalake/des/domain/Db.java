/*
* ============LICENSE_START=======================================================
* ONAP : DataLake
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonBackReference;

import org.onap.datalake.des.dto.DbConfig;

/**
 * Domain class representing bid data storage.
 * 
 * @author Guobiao Mo
 *
 */
@Setter
@Getter
@Entity
@Table(name = "db")
public class Db {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private int id;

    @Column(name = "`name`")
    private String name;

    @Column(name = "`enabled`", nullable = false)
    private boolean enabled;

    @Column(name = "`host`")
    private String host;

    @Column(name = "`port`")
    private Integer port;

    @Column(name = "`login`")
    private String login;

    @Column(name = "`pass`")
    private String pass;

    @Column(name = "`database_name`")
    private String database;

    @Column(name = "`encrypt`")
    private boolean encrypt;

    @Column(name = "`property1`")
    private String property1;

    @Column(name = "`property2`")
    private String property2;

    @Column(name = "`property3`")
    private String property3;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "db_type_id", nullable = false)
    private DbType dbType;

    @JsonBackReference
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "map_db_topic", joinColumns = { @JoinColumn(name = "db_id") }, inverseJoinColumns = {
            @JoinColumn(name = "topic_id") })

    @Override
    public String toString() {
        return String.format("Db %s (name=%s, enabled=%s)", id, name, enabled);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        return id == ((Db) obj).getId();
    }

    @Override
    public int hashCode() {
        return id;
    }

    /**
     * get db config.
     *
     * @return DbConfig db config
     *
     */    
    public DbConfig getDbConfig() {

        DbConfig dbConfig = new DbConfig();

        dbConfig.setId(getId());
        dbConfig.setName(getName());
        dbConfig.setHost(getHost());
        dbConfig.setPort(getPort());
        dbConfig.setPass(getPass());
        dbConfig.setLogin(getLogin());
        dbConfig.setEncrypt(isEncrypt());
        dbConfig.setEnabled(isEnabled());
        dbConfig.setDatabase(getDatabase());
        dbConfig.setDbTypeId(getDbType().getId());
        return dbConfig;
    }
}
