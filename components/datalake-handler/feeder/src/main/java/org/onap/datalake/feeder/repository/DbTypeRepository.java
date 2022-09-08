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

import org.onap.datalake.feeder.domain.DbType;
import org.springframework.data.repository.CrudRepository;

/**
 * DbTypeEnum Repository
 *
 * @author Guobiao Mo
 */

public interface DbTypeRepository extends CrudRepository<DbType, String> {

    Iterable<DbType> findByTool(boolean tool);
}
