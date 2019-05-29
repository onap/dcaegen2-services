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

import org.onap.datalake.feeder.domain.Portal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Portal Repository
 *
 * @author guochunmeng
 */

public interface PortalRepository extends CrudRepository<Portal, String> {

    @Query(nativeQuery = true, value = "select p.* from portal p where p.enabled = 1")
    List<Portal> queryAll();

    @Query(nativeQuery = true, value = "select p.name from portal p")
    List<String> getName();

    @Query(nativeQuery = true, value = "select p.related_db from portal p where p.name = ?1")
    String getRelatedDb(String name);

    @Query(nativeQuery = true, value = "update portal p set p.enabled = 0, p.host = null, p.port = null, p.login = null, p.pass = null where p.name = ?1")
    void updateEnabled(String name);
}
