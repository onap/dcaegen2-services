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

package org.onap.datalake.feeder.repository;

import org.onap.datalake.feeder.domain.DashboardTemplate;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Template Repository
 *
 * @author guochunmeng
 */

public interface TemplateRepository extends CrudRepository<DashboardTemplate, Integer> {


    @Query(nativeQuery = true, value = "select d1.address, d1.port from dashboard d1, dashboard_type d2, dashboard_template d3 where d1.name = d2.dashboard and d2.name = d3.type and d3.id = ?1")
    String deployById(Integer id);

    @Query(nativeQuery = true, value = "update dashboard_template d set d.name = ?1, d.body = ?2, d.note = ?3, d.topic = ?4, d.type = ?5 where d.id = ?6")
    void updateTemplate(String name, String body, String note, String topic, String type, Integer id);

}
