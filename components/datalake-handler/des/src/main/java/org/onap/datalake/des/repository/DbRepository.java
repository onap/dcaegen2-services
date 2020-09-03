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

package org.onap.datalake.des.repository;

import org.onap.datalake.des.domain.Db;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * Db Repository.
 *
 * @author Guobiao Mo
 *
 */

public interface DbRepository extends CrudRepository<Db, Integer> {

    Db findByName(String name);

    Iterable<Db> findByEncrypt(boolean encrypt);

}
